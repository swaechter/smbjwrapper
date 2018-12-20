package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.AbstractSharedItem;
import ch.swaechter.smbjwrapper.core.SharedItem;
import ch.swaechter.smbjwrapper.utils.ShareUtils;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.Directory;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        List<SharedItem> sharedItems = sortItems(listItems(SharedItem::isDirectory, false));
        return sharedItems.stream().map(item -> (SharedDirectory) item).collect(Collectors.toList());
    }

    /**
     * Get all files of the current directory.
     *
     * @return List with all files
     */
    public List<SharedFile> getFiles() {
        List<SharedItem> sharedItems = sortItems(listItems(SharedItem::isFile, false));
        return sharedItems.stream().map(item -> (SharedFile) item).collect(Collectors.toList());
    }

    /**
     * List all files and directories that match the search predicate. A recursive search is possible and will result
     * in a flat list with all matching elements.
     *
     * @param searchPredicate Search predicate that is used for testing
     * @param searchRecursive Flag to search recursive
     * @return Flat list with all matching files and directories
     */
    public List<SharedItem> listFiles(Predicate<SharedItem> searchPredicate, boolean searchRecursive) {
        return sortItems(listItems(searchPredicate, searchRecursive));
    }

    /**
     * List all files and directories that match the search pattern. A recursive search is possible and will result
     * in a flat list with all matching elements.
     *
     * @param searchPattern   Search regex pattern that is used for matching
     * @param searchRecursive Flag to search recursive
     * @return Flat list with all matching files and directories
     */
    public List<SharedItem> listFiles(String searchPattern, boolean searchRecursive) {
        Pattern pattern = Pattern.compile(searchPattern);
        return sortItems(listItems((sharedItem -> pattern.matcher(sharedItem.getName()).matches()), searchRecursive));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SharedDirectory renameTo(String newDirectoryName, boolean replaceIfExist) {
        try (Directory directory = getDiskShare().openDirectory(getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)) {
            String newDirectoryPath = getParentPath().getPath() + PATH_SEPARATOR + newDirectoryName;
            directory.rename(newDirectoryPath, replaceIfExist);
            return new SharedDirectory(getSharedConnection(), newDirectoryPath);
        }
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
     * List all files and directories based on the predicate and return them as flat list.
     *
     * @param searchPredicate Search predicate each file object is checked against
     * @param searchRecursive Flag to search recursive
     * @return Flat listh with all matching share items
     */
    private List<SharedItem> listItems(Predicate<SharedItem> searchPredicate, boolean searchRecursive) {
        String smbDirectoryPath = getPath();
        List<SharedItem> sharedItems = new LinkedList<>();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getDiskShare().list(smbDirectoryPath)) {
            String fileName = fileIdBothDirectoryInformation.getFileName();
            String filePath = (smbDirectoryPath.isEmpty()) ? fileName : smbDirectoryPath + PATH_SEPARATOR + fileName;
            if (ShareUtils.isValidSharedItemName(fileName)) {
                FileAllInformation fileAllInformation = getDiskShare().getFileInformation(filePath);
                if (fileAllInformation.getStandardInformation().isDirectory()) {
                    SharedDirectory sharedDirectory = new SharedDirectory(getSharedConnection(), filePath);
                    filterItem(sharedItems, sharedDirectory, searchPredicate);
                    if (searchRecursive) {
                        sharedItems.addAll(sharedDirectory.listFiles(searchPredicate, true));
                    }
                } else {
                    SharedFile sharedFile = new SharedFile(getSharedConnection(), filePath);
                    filterItem(sharedItems, sharedFile, searchPredicate);
                }
            }
        }
        return sharedItems;
    }

    /**
     * Filter a shared item against the valid item names and a test predicate, used for filtering.
     *
     * @param sharedItems     List with all succeeded share items
     * @param sharedItem      Current share item to check
     * @param searchPredicate Test predicate for the check
     */
    private void filterItem(List<SharedItem> sharedItems, SharedItem sharedItem, Predicate<SharedItem> searchPredicate) {
        if (searchPredicate.test(sharedItem)) {
            sharedItems.add(sharedItem);
        }
    }

    /**
     * Sort all items alphabetically.
     *
     * @param sharedItems List with the shared items to be sorted
     * @return Sorted list (Sorting is done in-place, value is returned for improved readability)
     */
    private List<SharedItem> sortItems(List<SharedItem> sharedItems) {
        sharedItems.sort(Comparator.comparing(SharedItem::getPath));
        return sharedItems;
    }
}
