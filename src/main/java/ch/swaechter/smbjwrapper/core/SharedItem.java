package ch.swaechter.smbjwrapper.core;

import com.hierynomus.msdtyp.FileTime;

/**
 * This class provides an interface for a directory/file like filesystem structure.
 *
 * @author Simon Wächter
 */
public interface SharedItem {

    /**
     * Check if the shared item does exist.
     *
     * @return Status of the check
     */
    boolean isExisting();

    /**
     * Check if the shared item is a directory.
     *
     * @return Status of the check
     */
    boolean isDirectory();

    /**
     * Check if the shared item is a file.
     *
     * @return Status of the check
     */
    boolean isFile();

    /**
     * Check if the shared item is hidden on an SMB level. Depending on the server configuration, dotted
     * UNIX files can also be interpreted as hidden files ("hide dot files = yes").
     *
     * @return Status of the check (Depending on the server configuration)
     * @see <a href="https://www.samba.org/samba/docs/current/man-html/smb.conf.5.html">Hide dot files option</a>
     */
    boolean isHidden();

    /**
     * Get the server name of the connected server.
     *
     * @return Name of the server
     */
    String getServerName();

    /**
     * Get the share name of the connected server.
     *
     * @return Name of the share
     */
    String getShareName();

    /**
     * Get the name of the shared item. If the shared item is a root item, an string name will be returned.
     *
     * @return Real name or empty string
     */
    String getName();

    /**
     * Get the path of the shared item. If the shared item is a root item, an empty string will be returned.
     *
     * @return Real path or empty string
     */
    String getPath();

    /**
     * Get the full SMB path including the server, share and path, separated by backslashes.
     *
     * @return Full SMB path
     */
    String getSmbPath();

    /**
     * Get the parent shared item. If the shared item us already the root item, the root item will be returned.
     *
     * @return Parent shared item
     */
    SharedItem getParentPath();

    /**
     * Get the root shared item. If the shared item us already the root item, the root item will be returned.
     *
     * @return Root shared item
     */
    SharedItem getRootPath();

    /**
     * Check if the current shared item or its parent is the root shared item.
     *
     * @return Status of the check
     */
    boolean isRootPath();

    /**
     * Rename the current item and return it as newly renamed item.
     *
     * @param newFileName    New file name
     * @param replaceIfExist Flag to replace an existing path of the same type (File/Directory)
     * @return Newly renamed shared item with the new path
     */
    SharedItem renameTo(String newFileName, boolean replaceIfExist);

    /**
     * Get the creation time of the shared item.
     *
     * @return Creation time of the shared item
     */
    FileTime getCreationTime();

    /**
     * Get the last access time of the shared item.
     *
     * @return Last access time of the shared item
     */

    FileTime getLastAccessTime();

    /**
     * Get the last write time of the shared item.
     *
     * @return Last write time of the shared item
     */

    FileTime getLastWriteTime();

    /**
     * Get the change time of the shared item.
     *
     * @return Change time of the shared item
     */

    FileTime getChangeTime();
}
