package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.AbstractSharedItem;
import ch.swaechter.smbjwrapper.utils.ShareUtils;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.auth.AuthenticationContext;

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
     * Create a shared directory based on the server name, share name, path name and the authentication.
     *
     * @param serverName            Name of the server
     * @param shareName             Name of the share
     * @param pathName              Path name
     * @param authenticationContext Authentication for the connection
     * @throws IOException Exception in case of a problem
     */
    public SharedDirectory(String serverName, String shareName, String pathName, AuthenticationContext authenticationContext) throws IOException {
        super(serverName, shareName, pathName, authenticationContext);
    }

    /**
     * Create a shared root directory based on the server name, share name and the authentication.
     *
     * @param serverName            Name of the server
     * @param shareName             Name of the share
     * @param authenticationContext Authentication for the connection
     * @throws IOException Exception in case of a problem
     */
    public SharedDirectory(String serverName, String shareName, AuthenticationContext authenticationContext) throws IOException {
        super(serverName, shareName, "", authenticationContext);
    }

    /**
     * Create shared directory via copy constructor and a new path name.
     *
     * @param abstractSharedItem Shared item that will be reused
     * @param pathName           New path name
     */
    public SharedDirectory(AbstractSharedItem abstractSharedItem, String pathName) {
        super(abstractSharedItem, pathName);
    }

    /**
     * Create a directory in the current directory.
     *
     * @param directoryName Name of the new directory
     * @return Newly created directory
     */
    public SharedDirectory createDirectoryInCurrentDirectory(String directoryName) {
        SharedDirectory sharedDirectory = new SharedDirectory(this, smbPath.getPath() + "\\" + directoryName);
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
        SharedFile sharedFile = new SharedFile(this, smbPath.getPath() + "\\" + fileName);
        sharedFile.createFile();
        return sharedFile;
    }

    /**
     * Create the current directory.
     */
    public void createDirectory() {
        diskShare.mkdir(smbPath.getPath());
    }

    /**
     * Delete the current directory with all its subdirectories and subfiles.
     */
    public void deleteDirectoryRecursively() {
        diskShare.rmdir(smbPath.getPath(), true);
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
            sharedDirectories.add(new SharedDirectory(this, fileIdBothDirectoryInformation.getFileName()));
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
            sharedFiles.add(new SharedFile(this, fileIdBothDirectoryInformation.getFileName()));
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
     */
    @Override
    protected SharedDirectory createSharedNodeItem(String pathName) {
        return new SharedDirectory(this, pathName);
    }

    /**
     * Get all file information that can be tested for the given predicate.
     *
     * @param predicate Predicate that will be tested
     * @return List of all valid file information
     */
    private List<FileIdBothDirectoryInformation> getFileIdBothDirectoryInformations(Predicate<FileAllInformation> predicate) {
        String smbDirectoryPath = smbPath.getPath();
        List<FileIdBothDirectoryInformation> fileIdBothDirectoryInformations = new ArrayList<>();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : diskShare.list(smbDirectoryPath)) {
            String fileName = fileIdBothDirectoryInformation.getFileName();
            if (ShareUtils.isValidSharedItemName(fileName)) {
                String filePath = (smbDirectoryPath.isEmpty()) ? fileName : smbDirectoryPath + "\\" + fileName;
                FileAllInformation fileAllInformation = diskShare.getFileInformation(filePath);
                if (predicate.test(fileAllInformation)) {
                    fileIdBothDirectoryInformations.add(fileIdBothDirectoryInformation);
                }
            }
        }
        return fileIdBothDirectoryInformations;
    }
}
