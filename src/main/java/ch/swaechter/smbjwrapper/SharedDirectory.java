package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.AbstractSharedItem;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.auth.AuthenticationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SharedDirectory extends AbstractSharedItem {

    public SharedDirectory(String serverName, String shareName, String pathName, AuthenticationContext authenticationContext) throws IOException {
        super(serverName, shareName, pathName, authenticationContext);
    }

    public SharedDirectory(String serverName, String shareName, AuthenticationContext authenticationContext) throws IOException {
        super(serverName, shareName, "", authenticationContext); // TODO: Add share root path
    }

    protected SharedDirectory(AbstractSharedItem abstractSharedItem) {
        super(abstractSharedItem, ""); // TODO: Add share root path
    }

    protected SharedDirectory(AbstractSharedItem abstractSharedItem, String pathName) {
        super(abstractSharedItem, pathName);
    }

    @Override
    public boolean isExisting() {
        return isDirectory();
    }

    @Override
    public SharedDirectory getParentPath() {
        if (!getName().equals(smbPath.getPath())) {
            String parentPath = smbPath.getPath().substring(0, smbPath.getPath().length() - getName().length() - 1);
            return new SharedDirectory(this, parentPath);
        } else {
            return getRootPath();
        }
    }

    @Override
    public SharedDirectory getRootPath() {
        return new SharedDirectory(this);
    }

    public SharedDirectory createDirectoryInCurrentDirectory(String name) {
        SharedDirectory sharedDirectory = new SharedDirectory(this, smbPath.getPath() + "\\" + name);
        sharedDirectory.createDirectory();
        return sharedDirectory;
    }

    public SharedFile createFileInCurrentDirectory(String name) {
        SharedFile sharedFile = new SharedFile(this, smbPath.getPath() + "\\" + name);
        sharedFile.createFile();
        return sharedFile;
    }

    public void createDirectory() {
        diskShare.mkdir(smbPath.getPath());
    }

    public void deleteDirectoryRecursively() {
        diskShare.rmdir(smbPath.getPath(), true);
    }

    public List<SharedDirectory> getDirectories() {
        String smbDirectoryPath = smbPath.getPath();
        List<SharedDirectory> sharedDirectories = new ArrayList<>();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : diskShare.list(smbDirectoryPath)) {
            String fileName = fileIdBothDirectoryInformation.getFileName();
            if (isValidSharedItemName(fileName)) {
                String filePath = (smbDirectoryPath.isEmpty()) ? fileName : smbDirectoryPath + "\\" + fileName;
                FileAllInformation fileAllInformation = diskShare.getFileInformation(filePath);
                if (fileAllInformation.getStandardInformation().isDirectory()) {
                    sharedDirectories.add(new SharedDirectory(this, filePath));
                }
            }
        }
        return sharedDirectories;
    }

    public List<SharedFile> getFiles() {
        String smbDirectoryPath = smbPath.getPath();
        List<SharedFile> sharedFiles = new ArrayList<>();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : diskShare.list(smbDirectoryPath)) {
            String fileName = fileIdBothDirectoryInformation.getFileName();
            if (isValidSharedItemName(fileName)) {
                String filePath = (smbDirectoryPath.isEmpty()) ? fileName : smbDirectoryPath + "\\" + fileName;
                FileAllInformation fileAllInformation = diskShare.getFileInformation(filePath);
                if (!fileAllInformation.getStandardInformation().isDirectory()) {
                    sharedFiles.add(new SharedFile(this, filePath));
                }
            }
        }
        return sharedFiles;
    }

    private boolean isValidSharedItemName(String fileName) {
        return !fileName.equals(".") && !fileName.equals("..");
    }
}
