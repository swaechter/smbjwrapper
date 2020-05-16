package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.utils.SmbUtils;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;

/**
 * This class provides a common abstracted class that represents a SMB directory/file like node.
 *
 * @author Simon Wächter
 */
public abstract class SmbItem {

    /**
     * String used to separate paths.
     */
    protected static final String PATH_SEPARATOR = "/";

    /**
     * String used to represent the root path.
     */
    protected static final String ROOT_PATH = "";

    /**
     * SMB connection to access the server.
     */
    private final SmbConnection smbConnection;

    /**
     * Path name of the abstract SMB item.
     */
    private final String pathName;

    /**
     * Create a new abstract SMB item based on the SMB connection and the path name.
     *
     * @param smbConnection SMB connection
     * @param pathName      Path name
     * @throws RuntimeException Exception in case of an invalid path name
     */
    public SmbItem(SmbConnection smbConnection, String pathName) {
        this.smbConnection = smbConnection;
        if (SmbUtils.isValidSmbItemName(pathName)) {
            this.pathName = pathName;
        } else {
            throw new RuntimeException("The given path name is not a valid SMB path");
        }
    }

    /**
     * Check if the SMB item does exist.
     *
     * @return Status of the check
     */
    public boolean isExisting() {
        return isDirectory() || isFile();
    }

    /**
     * Check if the SMB item is a directory.
     *
     * @return Status of the check
     */
    public boolean isDirectory() {
        return smbConnection.getDiskShare().folderExists(pathName);
    }

    /**
     * Check if the SMB item is a file.
     *
     * @return Status of the check
     */
    public boolean isFile() {
        return smbConnection.getDiskShare().fileExists(pathName);
    }

    /**
     * Check if the SMB item is hidden on an SMB level. Depending on the server configuration, dotted
     * UNIX files can also be interpreted as hidden files ("hide dot files = yes").
     *
     * @return Status of the check (Depending on the server configuration)
     * @see <a href="https://www.samba.org/samba/docs/current/man-html/smb.conf.5.html">Hide dot files option</a>
     */
    public boolean isHidden() {
        long attributeMask = getDiskShare().getFileInformation(pathName).getBasicInformation().getFileAttributes();
        return FileAttributes.EnumUtils.isSet(attributeMask, FileAttributes.FILE_ATTRIBUTE_HIDDEN);
    }

    /**
     * Get the server name of the connected server.
     *
     * @return Name of the server
     */
    public String getServerName() {
        return smbConnection.getServerName();
    }

    /**
     * Get the share name of the connected server.
     *
     * @return Name of the share
     */
    public String getShareName() {
        return smbConnection.getShareName();
    }

    /**
     * Get the name of the SMB item. If the SMB item is a root item, an empty string will be returned.
     *
     * @return Real name or empty string
     */
    public String getName() {
        if (!pathName.isEmpty()) {
            int lastIndex = pathName.lastIndexOf(PATH_SEPARATOR);
            return pathName.substring(lastIndex + 1);
        } else {
            return pathName;
        }
    }

    /**
     * Get the path of the SMB item. If the SMB item is a root item, an empty string will be returned.
     *
     * @return Real path or empty string
     */
    public String getPath() {
        return pathName;
    }

    /**
     * Get the full SMB path including the server, share and path, separated by backslashes.
     *
     * @return Full SMB path
     */
    public String getSmbPath() {
        SmbPath smbPath = new SmbPath(getServerName(), getShareName(), pathName.replace(PATH_SEPARATOR, "\\"));
        return smbPath.toUncPath();
    }

    /**
     * Get the parent SMB item. If the SMB item us already the root item, the root item will be re-returned.
     *
     * @return Parent SMB item
     */
    public SmbDirectory getParentPath() {
        if (!isRootPath()) {
            int lastIndex = pathName.lastIndexOf(PATH_SEPARATOR);
            return new SmbDirectory(getSmbConnection(), pathName.substring(0, lastIndex));
        } else {
            return getRootPath();
        }
    }

    /**
     * Get the root SMB item.
     *
     * @return Root SMB item
     */
    public SmbDirectory getRootPath() {
        return new SmbDirectory(smbConnection, ROOT_PATH);
    }

    /**
     * Check if the current SMB item is the root SMB item.
     *
     * @return Status of the check
     */
    public boolean isRootPath() {
        int lastIndex = getPath().lastIndexOf(PATH_SEPARATOR);
        return lastIndex == 0 || lastIndex == -1;
    }

    /**
     * Get the creation time of the SMB item.
     *
     * @return Creation time of the SMB item
     */
    public FileTime getCreationTime() {
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getCreationTime();
    }

    /**
     * Get the last access time of the SMB item.
     *
     * @return Last access time of the SMB item
     */
    public FileTime getLastAccessTime() {
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getLastAccessTime();
    }

    /**
     * Get the last write time of the SMB item.
     *
     * @return Last write time of the SMB item
     */
    public FileTime getLastWriteTime() {
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getLastWriteTime();
    }

    /**
     * Get the change time of the SMB item.
     *
     * @return Change time of the SMB item
     */
    public FileTime getChangeTime() {
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getChangeTime();
    }

    /**
     * Check if the current and the given objects are equals.
     *
     * @param object Given object to compare against
     * @return Status of the check
     */
    public abstract boolean equals(Object object);

    /**
     * Get the SMB connection.
     *
     * @return SMB connection
     */
    protected SmbConnection getSmbConnection() {
        return smbConnection;
    }

    /**
     * Get the disk share of the SMB connection.
     *
     * @return Disk share of the SMB connection
     */
    protected DiskShare getDiskShare() {
        return getSmbConnection().getDiskShare();
    }

    /**
     * Rename the current item and return it as newly renamed item. Renames are only possible from/to the same type. For example it's not possible to
     * rename a file to an existing directory.
     *
     * @param newItemName    New item name
     * @param replaceIfExist Flag to replace an existing path of the same type (File/Directory)
     * @return Newly renamed SMB item with the new path
     */
    public abstract SmbItem renameTo(String newItemName, boolean replaceIfExist);
}
