package ch.swaechter.smbjwrapper.core;

import ch.swaechter.smbjwrapper.SharedConnection;
import ch.swaechter.smbjwrapper.utils.ShareUtils;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;

/**
 * This class provides a common abstracted class that represents a directory/file like node.
 *
 * @param <T> Generic type that defined a directory/file like node that is used for item creation
 * @author Simon Wächter
 */
public abstract class AbstractSharedItem<T extends SharedItem> implements SharedItem {

    /**
     * String used to separate paths.
     */
    protected static final String PATH_SEPARATOR = "/";

    /**
     * String used to represent the root path.
     */
    protected static final String ROOT_PATH = "";

    /**
     * Shared connection to access the server.
     */
    private final SharedConnection sharedConnection;

    /**
     * Path name of the abstract shared item.
     */
    private final String pathName;

    /**
     * Create a new abstract shared item based on the shared connection and the path name.
     *
     * @param sharedConnection Shared connection
     * @param pathName         Path name
     * @throws RuntimeException Exception in case of an invalid path name
     */
    public AbstractSharedItem(SharedConnection sharedConnection, String pathName) {
        this.sharedConnection = sharedConnection;
        if (ShareUtils.isValidSharedItemName(pathName)) {
            this.pathName = pathName;
        } else {
            throw new RuntimeException("The given path name is not a valid share path");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExisting() {
        return isDirectory() || isFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return sharedConnection.getDiskShare().folderExists(pathName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFile() {
        return sharedConnection.getDiskShare().fileExists(pathName);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isHidden() {
        long attributeMask = getDiskShare().getFileInformation(pathName).getBasicInformation().getFileAttributes();
        return FileAttributes.EnumUtils.isSet(attributeMask, FileAttributes.FILE_ATTRIBUTE_HIDDEN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        if (!pathName.isEmpty()) {
            int lastIndex = pathName.lastIndexOf(PATH_SEPARATOR);
            return pathName.substring(lastIndex + 1, pathName.length());
        } else {
            return pathName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        return sharedConnection.getServerName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShareName() {
        return sharedConnection.getShareName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return pathName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSmbPath() {
        SmbPath smbPath = new SmbPath(getServerName(), getShareName(), pathName.replace(PATH_SEPARATOR, "\\"));
        return smbPath.toUncPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getParentPath() {
        if (!isRootPath()) {
            int lastIndex = pathName.lastIndexOf(PATH_SEPARATOR);
            return createSharedNodeItem(pathName.substring(0, lastIndex));
        } else {
            return getRootPath();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getRootPath() {
        return createSharedNodeItem(ROOT_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootPath() {
        int lastIndex = getPath().lastIndexOf(PATH_SEPARATOR);
        return lastIndex == 0 || lastIndex == -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileTime getCreationTime() {
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getCreationTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileTime getLastAccessTime() {
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getLastAccessTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileTime getLastWriteTime() {
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getLastWriteTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * Get the shared connection.
     *
     * @return Shared connection
     */
    protected SharedConnection getSharedConnection() {
        return sharedConnection;
    }

    /**
     * Get the disk share of the shared connection.
     *
     * @return Disk share of the shared connection
     */
    protected DiskShare getDiskShare() {
        return getSharedConnection().getDiskShare();
    }

    /**
     * Create a new shared item. This factory method is defined to enable directory/file like decoupling.
     *
     * @param pathName Path name of the shared item
     * @return Shared item
     */
    protected abstract T createSharedNodeItem(String pathName);
}
