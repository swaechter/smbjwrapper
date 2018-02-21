package ch.swaechter.smbjwrapper.core;

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
     * Get the full SMB path including the server, share and path, separated by backslashes.
     *
     * @return Full SMB path
     */
    String getSmbPath();

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
}
