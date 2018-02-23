package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.AbstractSharedItem;
import ch.swaechter.smbjwrapper.utils.ShareUtils;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import java.io.IOException;
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
     * @throws IOException Exception in case of a problem
     */
    public SharedDirectory(SharedConnection sharedConnection, String pathName) throws IOException {
        super(sharedConnection, pathName);
    }

    /**
     * Create a new shared directory based on the shared connection. As path name, the root path is used.
     *
     * @param sharedConnection Shared connection
     * @throws IOException Exception in case of a problem
     */
    public SharedDirectory(SharedConnection sharedConnection) throws IOException {
        super(sharedConnection, "");
    }

    /**
     * Create a directory in the current directory.
     *
     * @param directoryName Name of the new directory
     * @return Newly created directory
     * @throws IOException Exception in case of a problem
     */
    public SharedDirectory createDirectoryInCurrentDirectory(String directoryName) throws IOException {
        String pathSuffix = !getPath().isEmpty() ? getPath() + "/" : "";
        SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, pathSuffix + directoryName);
        sharedDirectory.createDirectory();
        return sharedDirectory;
    }

    /**
     * Create a file in the current directory.
     *
     * @param fileName Name of the new file
     * @return Newly created file
     * @throws IOException Exception in case of a problem
     */
    public SharedFile createFileInCurrentDirectory(String fileName) throws IOException {
        String pathSuffix = !getPath().isEmpty() ? getPath() + "/" : "";
        SharedFile sharedFile = new SharedFile(sharedConnection, pathSuffix + fileName);
        sharedFile.createFile();
        return sharedFile;
    }

    /**
     * Create the current directory.
     */
    public void createDirectory() {
        sharedConnection.getDiskShare().mkdir(getPath());
    }

    /**
     * Delete the current directory with all its subdirectories and subfiles.
     */
    public void deleteDirectoryRecursively() {
        sharedConnection.getDiskShare().rmdir(getPath(), true);
    }

    /**
     * Get all directories of the current directory.
     *
     * @return List with all directories
     * @throws IOException Exception in case of a problem
     */
    public List<SharedDirectory> getDirectories() throws IOException {
        List<SharedDirectory> sharedDirectories = new ArrayList<>();
        Predicate<FileAllInformation> predicate = (fileAllInformation) -> fileAllInformation.getStandardInformation().isDirectory();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getFileIdBothDirectoryInformations(predicate)) {
            sharedDirectories.add(new SharedDirectory(sharedConnection, getPath() + "/" + fileIdBothDirectoryInformation.getFileName()));
        }
        return sharedDirectories;
    }

    /**
     * Get all files of the current directory.
     *
     * @return List with all files
     * @throws IOException Exception in case of a problem
     */
    public List<SharedFile> getFiles() throws IOException {
        List<SharedFile> sharedFiles = new ArrayList<>();
        Predicate<FileAllInformation> predicate = (fileAllInformation) -> !fileAllInformation.getStandardInformation().isDirectory();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getFileIdBothDirectoryInformations(predicate)) {
            sharedFiles.add(new SharedFile(sharedConnection, getPath() + "/" + fileIdBothDirectoryInformation.getFileName()));
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
        if (object != null && object instanceof SharedDirectory) {
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
     * @throws IOException Exception in case of a problem
     */
    @Override
    protected SharedDirectory createSharedNodeItem(String pathName) throws IOException {
        return new SharedDirectory(sharedConnection, pathName);
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
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : sharedConnection.getDiskShare().list(smbDirectoryPath)) {
            String fileName = fileIdBothDirectoryInformation.getFileName();
            if (ShareUtils.isValidSharedItemName(fileName)) {
                String filePath = (smbDirectoryPath.isEmpty()) ? fileName : smbDirectoryPath + "\\" + fileName;
                FileAllInformation fileAllInformation = sharedConnection.getDiskShare().getFileInformation(filePath);
                if (predicate.test(fileAllInformation)) {
                    fileIdBothDirectoryInformations.add(fileIdBothDirectoryInformation);
                }
            }
        }
        return fileIdBothDirectoryInformations;
    }
}
