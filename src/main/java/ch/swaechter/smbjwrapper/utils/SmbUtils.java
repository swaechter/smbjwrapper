package ch.swaechter.smbjwrapper.utils;

import ch.swaechter.smbjwrapper.SmbConnection;
import ch.swaechter.smbjwrapper.SmbDirectory;
import ch.swaechter.smbjwrapper.SmbFile;
import ch.swaechter.smbjwrapper.SmbItem;
import com.hierynomus.smbj.auth.AuthenticationContext;

import java.io.IOException;

/**
 * This class provides some SMB util methods.
 *
 * @author Simon Wächter
 */
public class SmbUtils {

    /**
     * Build an SMB item for a full UNC path. If the path does exist, it will auto detect the type (File/directory) and return the matching object.
     * If not, the creation strategy will be used and a file/directory will be returned - or an exception thrown.
     *
     * @param authenticationContext Authentication context
     * @param creationStrategy      Creation strategy that will specify the creation behaviour if the path does not exist
     * @param uncPath               UNC path to parse
     * @return SMB file or directory item
     * @throws IOException Exception in case of a parsing/IO problem or the path does not exist and the creation strategy is set to EXCEPTION
     */
    public static SmbItem buildSmbItemFromUncPath(AuthenticationContext authenticationContext, CreationStrategy creationStrategy, String uncPath) throws IOException {
        // Replace all backslashes with slashes so we don't have to escape everything
        String path = uncPath.replace("\\", "/");

        // Remove the leading \\
        if (!path.startsWith("//")) {
            throw new IOException("A UNC path must start with \\\\ followed by the server IP + \\ + share");
        }
        path = path.substring(2);

        // Split the path to get the server name
        String[] parameters = path.split("/");
        if (parameters.length < 1) {
            throw new IOException("A UNC path must contain a server name after the leading \\\\");
        }
        String serverName = parameters[0];

        // Remove the server name
        path = path.substring(serverName.length());

        // Remove the \
        if (!path.startsWith("/")) {
            throw new IOException("A UNC path must continue with a \\ after the server name followed by the share name");
        }
        path = path.substring(1);

        // Split the path to get the share name
        parameters = path.split("/");
        if (parameters.length < 1) {
            throw new IOException("A UNC path must contain a share name after the server name + \\");
        }
        String shareName = parameters[0];

        // Remove the share name
        path = path.substring(shareName.length());

        // Create the SMB connection
        SmbConnection smbConnection = new SmbConnection(serverName, shareName, authenticationContext);

        // Remove a leading / in the path
        if(path.startsWith("/")) {
            path = path.substring(1);
        }

        // Create a SMB directory to check if the path exists
        SmbItem smbItem = new SmbDirectory(smbConnection, path);

        // Decide what we will return
        if (smbItem.isDirectory()) {
            // Do nothing, SMB item is already a directory
        } else if (smbItem.isFile()) {
            smbItem = new SmbFile(smbConnection, path);
        } else {
            // Fallback to a non existing file/directory or throw an exception
            if (creationStrategy == CreationStrategy.DIRECTORY) {
                smbItem = new SmbDirectory(smbConnection, path);
            } else if (creationStrategy == CreationStrategy.FILE) {
                smbItem = new SmbFile(smbConnection, path);
            } else {
                throw new IOException("The given UNC path does not exist and thus it's not possible to decide if the path is a file/directory");
            }
        }

        // Return the item
        return smbItem;
    }
}
