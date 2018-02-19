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

public final class SharedDirectory extends AbstractSharedItem<SharedDirectory> {

    public SharedDirectory(String serverName, String shareName, String pathName, AuthenticationContext authenticationContext) throws IOException {
        super(serverName, shareName, pathName, authenticationContext);
    }

    public SharedDirectory(String serverName, String shareName, AuthenticationContext authenticationContext) throws IOException {
        super(serverName, shareName, "", authenticationContext);
    }

    protected SharedDirectory(AbstractSharedItem abstractSharedItem, String pathName) {
        super(abstractSharedItem, pathName);
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
        List<SharedDirectory> sharedDirectories = new ArrayList<>();
        Predicate<FileAllInformation> predicate = (fileAllInformation) -> fileAllInformation.getStandardInformation().isDirectory();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getFileIdBothDirectoryInformations(predicate)) {
            sharedDirectories.add(new SharedDirectory(this, fileIdBothDirectoryInformation.getFileName()));
        }
        return sharedDirectories;
    }

    public List<SharedFile> getFiles() {
        List<SharedFile> sharedFiles = new ArrayList<>();
        Predicate<FileAllInformation> predicate = (fileAllInformation) -> !fileAllInformation.getStandardInformation().isDirectory();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getFileIdBothDirectoryInformations(predicate)) {
            sharedFiles.add(new SharedFile(this, fileIdBothDirectoryInformation.getFileName()));
        }
        return sharedFiles;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof SharedDirectory) {
            SharedDirectory sharedDirectory = (SharedDirectory) object;
            return getSmbPath().equals(sharedDirectory.getSmbPath());
        } else {
            return false;
        }
    }

    @Override
    protected SharedDirectory createSharedNodeItem(String pathName) {
        return new SharedDirectory(this, pathName);
    }

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
