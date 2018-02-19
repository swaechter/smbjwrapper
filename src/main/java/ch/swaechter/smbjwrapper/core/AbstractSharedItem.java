package ch.swaechter.smbjwrapper.core;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.io.IOException;

public abstract class AbstractSharedItem<T extends SharedItem> implements SharedItem {

    protected final SMBClient smbClient;

    protected final Connection connection;

    protected final AuthenticationContext authenticationContext;

    protected final Session session;

    protected final DiskShare diskShare;

    protected final SmbPath smbPath;

    public AbstractSharedItem(String serverName, String shareName, String pathName, AuthenticationContext authenticationContext) throws IOException {
        this.smbClient = new SMBClient();
        this.connection = smbClient.connect(serverName);
        this.authenticationContext = authenticationContext;
        this.session = connection.authenticate(authenticationContext);
        this.diskShare = (DiskShare) session.connectShare(shareName);
        this.smbPath = new SmbPath(serverName, shareName, pathName);
    }

    protected AbstractSharedItem(AbstractSharedItem abstractSharedItem, String pathName) {
        SmbPath otherSmbPath = abstractSharedItem.smbPath;
        this.smbClient = abstractSharedItem.smbClient;
        this.connection = abstractSharedItem.connection;
        this.authenticationContext = abstractSharedItem.authenticationContext;
        this.session = abstractSharedItem.session;
        this.diskShare = abstractSharedItem.diskShare;
        this.smbPath = new SmbPath(otherSmbPath.getHostname(), otherSmbPath.getShareName(), pathName);
    }

    @Override
    public boolean isExisting() {
        return isDirectory() || isFile();
    }

    @Override
    public boolean isDirectory() {
        return diskShare.folderExists(smbPath.getPath());
    }

    @Override
    public boolean isFile() {
        return diskShare.fileExists(smbPath.getPath());
    }

    @Override
    public String getName() {
        if (!smbPath.getPath().isEmpty()) {
            String[] parameters = smbPath.getPath().split("\\\\");
            return parameters[parameters.length - 1];
        } else {
            return "";
        }
    }

    @Override
    public String getServerName() {
        return smbPath.getHostname();
    }

    @Override
    public String getShareName() {
        return smbPath.getShareName();
    }

    @Override
    public String getSmbPath() {
        return smbPath.toUncPath();
    }

    @Override
    public String getPath() {
        return smbPath.getPath();
    }

    @Override
    public T getParentPath() {
        if (!getName().equals(smbPath.getPath())) {
            String parentPath = smbPath.getPath().substring(0, smbPath.getPath().length() - getName().length() - 1);
            return createSharedNodeItem(parentPath);
        } else {
            return getRootPath();
        }
    }

    @Override
    public T getRootPath() {
        return createSharedNodeItem("");
    }

    @Override
    public boolean isRootPath() {
        return getRootPath().equals(getParentPath());
    }

    public abstract boolean equals(Object object);

    protected abstract T createSharedNodeItem(String pathName);
}
