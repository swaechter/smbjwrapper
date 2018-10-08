package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.AbstractSharedItem;
import ch.swaechter.smbjwrapper.utils.ShareUtils;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This class represents a shared directory.
 *
 * @author Simon Wächter
 */
public final class SharedDirectory extends AbstractSharedItem<SharedDirectory> {

    /**
     * Create a new shared directory based on the shared connection and the path name.
     *
     * @param sharedConnection Shared connection
     * @param pathName         Path name
     */
    public SharedDirectory(SharedConnection sharedConnection, String pathName) {
        super(sharedConnection, pathName);
    }

    /**
     * Create a new shared directory based on the shared connection. As path name, the root path is used.
     *
     * @param sharedConnection Shared connection
     */
    public SharedDirectory(SharedConnection sharedConnection) {
        super(sharedConnection, ROOT_PATH);
    }

    /**
     * Create a directory in the current directory.
     *
     * @param directoryName Name of the new directory
     * @return Newly created directory
     */
    public SharedDirectory createDirectoryInCurrentDirectory(String directoryName) {
        String pathSuffix = !getPath().isEmpty() ? getPath() + PATH_SEPARATOR : ROOT_PATH;
        SharedDirectory sharedDirectory = new SharedDirectory(getSharedConnection(), pathSuffix + directoryName);
        sharedDirectory.createDirectory();
        return sharedDirectory;
    }

    /**
     * Create a file in the current directory.
     *
     * @param fileName Name of the new file
     * @return Newly created file
     */
    public SharedFile createFileInCurrentDirectory(String fileName) {
        String pathSuffix = !getPath().isEmpty() ? getPath() + PATH_SEPARATOR : ROOT_PATH;
        SharedFile sharedFile = new SharedFile(getSharedConnection(), pathSuffix + fileName);
        sharedFile.createFile();
        return sharedFile;
    }

    /**
     * Create the current directory.
     */
    public void createDirectory() {
        getDiskShare().mkdir(getPath());
    }

    /**
     * Delete the current directory with all its subdirectories and subfiles.
     */
    public void deleteDirectoryRecursively() {
        getDiskShare().rmdir(getPath(), true);
    }

    /**
     * Get all directories of the current directory.
     *
     * @return List with all directories
     */
    public List<SharedDirectory> getDirectories() {
        List<SharedDirectory> sharedDirectories = new ArrayList<>();
        Predicate<FileAllInformation> predicate = (fileAllInformation) -> fileAllInformation.getStandardInformation().isDirectory();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getFileIdBothDirectoryInformations(predicate)) {
            sharedDirectories.add(new SharedDirectory(getSharedConnection(), getPath() + PATH_SEPARATOR + fileIdBothDirectoryInformation.getFileName()));
        }
        return sharedDirectories;
    }

    /**
     * Get all files of the current directory.
     *
     * @return List with all files
     */
    public List<SharedFile> getFiles() {
        List<SharedFile> sharedFiles = new ArrayList<>();
        Predicate<FileAllInformation> predicate = (fileAllInformation) -> !fileAllInformation.getStandardInformation().isDirectory();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getFileIdBothDirectoryInformations(predicate)) {
            sharedFiles.add(new SharedFile(getSharedConnection(), getPath() + PATH_SEPARATOR + fileIdBothDirectoryInformation.getFileName()));
        }
        return sharedFiles;
    }

    /**
     * Check if the current and the given objects are equals.
     *
     * @param object Given object to compare against
     * @return Status of the check
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedDirectory) {
            SharedDirectory sharedDirectory = (SharedDirectory) object;
            return getSmbPath().equals(sharedDirectory.getSmbPath());
        } else {
            return false;
        }
    }

    /**
     * Create a new shared directory via factory.
     *
     * @param pathName Path name of the shared item
     * @return New shared directory
     */
    @Override
    protected SharedDirectory createSharedNodeItem(String pathName) {
        return new SharedDirectory(getSharedConnection(), pathName);
    }

    /**
     * Ensure that the given directory path does exist - if not, the path is created. In case the path already exists, but is not a directory,
     * an exception will be thrown to ensure application integrity.
     */
    public void ensureExists() {
        if (!isExisting()) {
            createDirectory();
        } else if (!isDirectory()) {
            throw new IllegalStateException("The given path does already exist, but not as directory");
        }
    }

    /**
     * Get all file information that can be tested for the given predicate.
     *
     * @param predicate Predicate that will be tested
     * @return List of all valid file information
     */
    private List<FileIdBothDirectoryInformation> getFileIdBothDirectoryInformations(Predicate<FileAllInformation> predicate) {
        String smbDirectoryPath = getPath();
        List<FileIdBothDirectoryInformation> fileIdBothDirectoryInformations = new ArrayList<>();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getDiskShare().list(smbDirectoryPath)) {
            String fileName = fileIdBothDirectoryInformation.getFileName();
            if (ShareUtils.isValidSharedItemName(fileName)) {
                String filePath = (smbDirectoryPath.isEmpty()) ? fileName : smbDirectoryPath + PATH_SEPARATOR + fileName;
                FileAllInformation fileAllInformation = getDiskShare().getFileInformation(filePath);
                if (predicate.test(fileAllInformation)) {
                    fileIdBothDirectoryInformations.add(fileIdBothDirectoryInformation);
                }
            }
        }
        return fileIdBothDirectoryInformations;
    }
}
