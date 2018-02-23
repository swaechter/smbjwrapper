package ch.swaechter.smbjwrapper.core;

import ch.swaechter.smbjwrapper.SharedConnection;
import com.hierynomus.smbj.common.SmbPath;

import java.io.IOException;

/**
 * This class provides a common abstracted class that represents a directory/file like node.
 *
 * @param <T> Generic type that defined a directory/file like node that is used for item creation
 * @author Simon Wächter
 */
public abstract class AbstractSharedItem<T extends SharedItem> implements SharedItem {

    /**
     * Shared connection to access the server.
     */
    protected final SharedConnection sharedConnection;

    /**
     * Path name of the abstract shared item.
     */
    protected final String pathName;

    /**
     * Create a new abstract shared item based on the shared connection and the path name.
     *
     * @param sharedConnection Shared connection
     * @param pathName         Path name
     * @throws IOException Exception in case of a problem
     */
    public AbstractSharedItem(SharedConnection sharedConnection, String pathName) throws IOException {
        this.sharedConnection = sharedConnection;
        this.pathName = pathName;
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
    @Override
    public String getName() {
        if (!pathName.isEmpty()) {
            int lastIndex = pathName.lastIndexOf("/");
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
        SmbPath smbPath = new SmbPath(getServerName(), getShareName(), pathName.replace("/", "\\"));
        return smbPath.toUncPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getParentPath() throws IOException {
        if (!isRootPath()) {
            int lastIndex = pathName.lastIndexOf("/");
            return createSharedNodeItem(pathName.substring(0, lastIndex));
        } else {
            return getRootPath();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getRootPath() throws IOException {
        return createSharedNodeItem("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootPath() {
        // TODO: Fix /Directory
        int lastIndex = getPath().lastIndexOf("/");
        return lastIndex == 0 || lastIndex == -1;
    }

    /**
     * Check if the current and the given objects are equals.
     *
     * @param object Given object to compare against
     * @return Status of the check
     */
    public abstract boolean equals(Object object);

    /**
     * Create a new shared item. This factory method is defined to enable directory/file like decoupling.
     *
     * @param pathName Path name of the shared item
     * @return Shared item
     * @throws IOException Exception in case of a problem
     */
    protected abstract T createSharedNodeItem(String pathName) throws IOException;
}
