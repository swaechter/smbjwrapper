package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.utils.SmbUtils;
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
 * This class represents a SMB directory.
 *
 * @author Simon Wächter
 */
public final class SmbDirectory extends SmbItem {

    /**
     * Create a new SMB directory based on the SMB connection and the path name.
     *
     * @param smbConnection SMB connection
     * @param pathName      Path name
     */
    public SmbDirectory(SmbConnection smbConnection, String pathName) {
        super(smbConnection, pathName);
    }

    /**
     * Create a new SMB directory based on the SMB connection. As path name, the root path is used.
     *
     * @param smbConnection SMB connection
     */
    public SmbDirectory(SmbConnection smbConnection) {
        super(smbConnection, ROOT_PATH);
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
    public SmbDirectory createDirectoryInCurrentDirectory(String directoryName) {
        String pathSuffix = !getPath().isEmpty() ? getPath() + PATH_SEPARATOR : ROOT_PATH;
        SmbDirectory smbDirectory = new SmbDirectory(getSmbConnection(), pathSuffix + directoryName);
        smbDirectory.createDirectory();
        return smbDirectory;
    }

    /**
     * Create a file in the current directory.
     *
     * @param fileName Name of the new file
     * @return Newly created file
     */
    public SmbFile createFileInCurrentDirectory(String fileName) {
        String pathSuffix = !getPath().isEmpty() ? getPath() + PATH_SEPARATOR : ROOT_PATH;
        SmbFile smbFile = new SmbFile(getSmbConnection(), pathSuffix + fileName);
        smbFile.createFile();
        return smbFile;
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
    public List<SmbDirectory> getDirectories() {
        List<SmbItem> smbItems = sortItems(internallyListItems(SmbItem::isDirectory, false));
        return smbItems.stream().map(item -> (SmbDirectory) item).collect(Collectors.toList());
    }

    /**
     * Get all files of the current directory.
     *
     * @return List with all files
     */
    public List<SmbFile> getFiles() {
        List<SmbItem> smbItems = sortItems(internallyListItems(SmbItem::isFile, false));
        return smbItems.stream().map(item -> (SmbFile) item).collect(Collectors.toList());
    }

    /**
     * List all files and directories of the current directory. No recursive search or filtering is performed.
     *
     * @return Flat list with all files and directories of the current directory
     */
    public List<SmbItem> listItems() {
        return sortItems(internallyListItems(smbItem -> true, false));
    }

    /**
     * List all files and directories that match the search predicate. A recursive search is possible and will result
     * in a flat list with all matching elements.
     *
     * @param searchPredicate Search predicate that is used for testing
     * @param searchRecursive Flag to search recursive
     * @return Flat list with all matching files and directories
     */
    public List<SmbItem> listItems(Predicate<SmbItem> searchPredicate, boolean searchRecursive) {
        return sortItems(internallyListItems(searchPredicate, searchRecursive));
    }

    /**
     * List all files and directories that match the search pattern. A recursive search is possible and will result
     * in a flat list with all matching elements.
     *
     * @param searchPattern   Search regex pattern that is used for matching
     * @param searchRecursive Flag to search recursive
     * @return Flat list with all matching files and directories
     */
    public List<SmbItem> listItems(String searchPattern, boolean searchRecursive) {
        Pattern pattern = Pattern.compile(searchPattern);
        return sortItems(internallyListItems((smbItem -> pattern.matcher(smbItem.getName()).matches()), searchRecursive));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SmbDirectory renameTo(String newDirectoryName, boolean replaceIfExist) {
        try (Directory directory = getDiskShare().openDirectory(getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)) {
            String newDirectoryPath = buildProperItemPath(getParentPath().getPath(), newDirectoryName);
            directory.rename(newDirectoryPath, replaceIfExist);
            return new SmbDirectory(getSmbConnection(), newDirectoryPath);
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
        if (object instanceof SmbDirectory) {
            SmbDirectory smbDirectory = (SmbDirectory) object;
            return getSmbPath().equals(smbDirectory.getSmbPath());
        } else {
            return false;
        }
    }

    /**
     * List all files and directories based on the predicate and return them as flat list.
     *
     * @param searchPredicate Search predicate each file object is checked against
     * @param searchRecursive Flag to search recursive
     * @return Flat list with all matching share items
     */
    private List<SmbItem> internallyListItems(Predicate<SmbItem> searchPredicate, boolean searchRecursive) {
        String smbDirectoryPath = getPath();
        List<SmbItem> smbItems = new LinkedList<>();
        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : getDiskShare().list(smbDirectoryPath)) {
            String fileName = fileIdBothDirectoryInformation.getFileName();
            String filePath = (smbDirectoryPath.isEmpty()) ? fileName : smbDirectoryPath + PATH_SEPARATOR + fileName;
            if (SmbUtils.isValidSmbItemName(fileName)) {
                FileAllInformation fileAllInformation = getDiskShare().getFileInformation(filePath);
                if (fileAllInformation.getStandardInformation().isDirectory()) {
                    SmbDirectory smbDirectory = new SmbDirectory(getSmbConnection(), filePath);
                    filterItem(smbItems, smbDirectory, searchPredicate);
                    if (searchRecursive) {
                        smbItems.addAll(smbDirectory.listItems(searchPredicate, true));
                    }
                } else {
                    SmbFile smbFile = new SmbFile(getSmbConnection(), filePath);
                    filterItem(smbItems, smbFile, searchPredicate);
                }
            }
        }
        return smbItems;
    }

    /**
     * Filter a SMB item against the valid item names and a test predicate, used for filtering.
     *
     * @param smbItems        List with all succeeded SMB items
     * @param smbItem         Current SMB item to check
     * @param searchPredicate Test predicate for the check
     */
    private void filterItem(List<SmbItem> smbItems, SmbItem smbItem, Predicate<SmbItem> searchPredicate) {
        if (searchPredicate.test(smbItem)) {
            smbItems.add(smbItem);
        }
    }

    /**
     * Sort all items alphabetically.
     *
     * @param smbItems List with the SMB items to be sorted
     * @return Sorted list (Sorting is done in-place, value is returned for improved readability)
     */
    private List<SmbItem> sortItems(List<SmbItem> smbItems) {
        smbItems.sort(Comparator.comparing(SmbItem::getPath));
        return smbItems;
    }
}
