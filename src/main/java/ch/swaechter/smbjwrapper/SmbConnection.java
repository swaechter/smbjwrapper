package ch.swaechter.smbjwrapper;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.share.DiskShare;

import java.io.IOException;

/**
 * This class is responsible for managing the SMB connection and session.
 *
 * @author Simon Wächter
 */
public class SmbConnection implements AutoCloseable {

    /**
     * Authentication context with the potential credentials.
     */
    private final AuthenticationContext authenticationContext;

    /**
     * SMB configuration.
     */
    private final SmbConfig smbConfig;

    /**
     * Server name of the server.
     */
    private final String serverName;

    /**
     * Share name of the server
     */
    private final String shareName;

    /**
     * New or reused connection.
     */
    private Connection connection;

    /**
     * Disk share for the SMB access.
     */
    private DiskShare diskShare;

    /**
     * Create a new SMB connection to the server with the server name, share name and the authentication context.
     * <p>
     * Note: The connection itself is not thread safe. Create a connection for each thread communicating with the SMB server.
     *
     * @param serverName            Server name of the server
     * @param shareName             Share name of the server
     * @param authenticationContext Authentication used to authenticate against
     * @throws IOException Exception in case of a problem
     */
    public SmbConnection(String serverName, String shareName, AuthenticationContext authenticationContext) throws IOException {
        this(serverName, shareName, authenticationContext, SmbConfig.builder().build(), false);
    }

    /**
     * Create a new SMB connection like normal, but with the ability to pass a custom smbj SMB configuration.
     * <p>
     * Note: The connection itself is not thread safe. Create a connection for each thread communicating with the SMB server.
     *
     * @param serverName            Server name of the server
     * @param shareName             Share name of the server
     * @param authenticationContext Authentication used to authenticate against
     * @param smbConfig             Custom SMB/smbj configuration
     * @throws IOException Exception in case of a problem
     */
    public SmbConnection(String serverName, String shareName, AuthenticationContext authenticationContext, SmbConfig smbConfig) throws IOException {
        this(serverName, shareName, authenticationContext, smbConfig, false);
    }


    /**
     * Create a new SMB connection like normal, but with the ability to pass a custom smbj SMB configuration and to delay the internal creation of the
     * connection. Delaying the connection creation until the first real connection call can be useful if you create many connections at once or
     * in parallel (The internal creation is a resource and time consuming operation). Please not that an invalid connection can only be detected on
     * the first call if the delayed creation is used.
     * <p>
     * Note: The connection itself is not thread safe. Create a connection for each thread communicating with the SMB server.
     *
     * @param serverName            Server name of the server
     * @param shareName             Share name of the server
     * @param authenticationContext Authentication used to authenticate against
     * @param smbConfig             Custom SMB/smbj configuration
     * @param delayedInitialization Delay the initialization of the connection until the first access. This can be
     * @throws IOException Exception in case of a problem
     */
    public SmbConnection(String serverName, String shareName, AuthenticationContext authenticationContext, SmbConfig smbConfig, boolean delayedInitialization) throws IOException {
        this.authenticationContext = authenticationContext;
        this.smbConfig = smbConfig;
        this.serverName = serverName;
        this.shareName = shareName;

        // Connect directly or not (Delay)
        if (!delayedInitialization) {
            connectToServer();
        }
    }

    /**
     * Get the disk share to access the server. If the connection initialization is delayed, this call will initialize the internal connection.
     *
     * @return Disk share to access the server
     * @throws RuntimeException Exception in case of a delayed initialization problem
     */
    public DiskShare getDiskShare() {
        if (!isConnectionAlive()) {
            try {
                ensureConnectionIsAlive();
            } catch (IOException exception) {
                throw new RuntimeException("Unable to initialize the delayed connection", exception);
            }
        }

        return diskShare;
    }

    /**
     * Get the server name of the server.
     *
     * @return Name of the server
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get the share name of the server.
     *
     * @return Name of the share
     */
    public String getShareName() {
        return shareName;
    }

    /**
     * Check if the connection to the SMB server is alive.
     *
     * @return Status of the check
     */
    public boolean isConnectionAlive() {
        if (connection == null || diskShare == null) {
            return false;
        }

        return connection.isConnected() && diskShare.isConnected();
    }

    /**
     * Ensure that the connection is alive. If the connection is not alive, the internal connection to the SMB server will be recreated. This method
     * is useful for long living SmbConnections with the same object (Like create the connection, do some work, wait multiple hours etc., do another
     * work etc.).
     *
     * @throws IOException Exception in case of a problem
     */
    public void ensureConnectionIsAlive() throws IOException {
        if (!isConnectionAlive()) {
            close();
            connectToServer();
        }
    }

    /**
     * Close the connection.
     *
     * @throws IOException Exception in case of a problem
     */
    @Override
    public void close() throws IOException {
        if (connection != null && connection.isConnected()) {
            connection.close(true);
        }
    }

    /**
     * Connect to the SMB server and set up the internal connection.
     *
     * @throws IOException Exception in case of a problem
     */
    private void connectToServer() throws IOException {
        SMBClient smbClient = new SMBClient(smbConfig);
        this.connection = smbClient.connect(serverName);
        this.diskShare = (DiskShare) connection.authenticate(authenticationContext).connectShare(shareName);
    }
}
