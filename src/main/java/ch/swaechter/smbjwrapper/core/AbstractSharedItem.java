package ch.swaechter.smbjwrapper.core;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.io.IOException;

/**
 * This class provides a common abstracted class that represents a directory/file like node.
 *
 * @param <T> Generic type that defined a directory/file like node that is used for item creation
 * @author Simon Wächter
 */
public abstract class AbstractSharedItem<T extends SharedItem> implements SharedItem {

    /**
     * Disk share that is used for accessing the Samba share.
     */
    protected final DiskShare diskShare;

    /**
     * Path name information that are used for all operations.
     */
    protected final SmbPath smbPath;

    /**
     * Create a new abstract shared item based on the server name, share name, path name and the authentication.
     *
     * @param serverName            Name of the server
     * @param shareName             Name of the share
     * @param pathName              Path name
     * @param authenticationContext Authentication for the connection
     * @throws IOException Exception in case of a problem
     */
    public AbstractSharedItem(String serverName, String shareName, String pathName, AuthenticationContext authenticationContext) throws IOException {
        SMBClient smbClient = new SMBClient();
        Connection connection = smbClient.connect(serverName);
        Session session = connection.authenticate(authenticationContext);
        this.diskShare = (DiskShare) session.connectShare(shareName);
        this.smbPath = new SmbPath(serverName, shareName, pathName);
    }

    /**
     * Create an abstract shared item via copy constructor and a new path name.
     *
     * @param abstractSharedItem Shared item that will be reused
     * @param pathName           New path name
     */
    protected AbstractSharedItem(AbstractSharedItem abstractSharedItem, String pathName) {
        SmbPath otherSmbPath = abstractSharedItem.smbPath;
        this.diskShare = abstractSharedItem.diskShare;
        this.smbPath = new SmbPath(otherSmbPath.getHostname(), otherSmbPath.getShareName(), pathName);
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
        return diskShare.folderExists(smbPath.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFile() {
        return diskShare.fileExists(smbPath.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        if (!smbPath.getPath().isEmpty()) {
            String[] parameters = smbPath.getPath().split("\\\\");
            return parameters[parameters.length - 1];
        } else {
            return "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        return smbPath.getHostname();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShareName() {
        return smbPath.getShareName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSmbPath() {
        return smbPath.toUncPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return smbPath.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getParentPath() {
        if (!getName().equals(smbPath.getPath())) {
            String parentPath = smbPath.getPath().substring(0, smbPath.getPath().length() - getName().length() - 1);
            return createSharedNodeItem(parentPath);
        } else {
            return getRootPath();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getRootPath() {
        return createSharedNodeItem("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRootPath() {
        return getRootPath().equals(getParentPath());
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
     */
    protected abstract T createSharedNodeItem(String pathName);
}
