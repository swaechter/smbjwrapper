package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.AbstractSharedItem;
import ch.swaechter.smbjwrapper.streams.SharedInputStream;
import ch.swaechter.smbjwrapper.streams.SharedOutputStream;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * This class represents a shared file.
 *
 * @author Simon Wächter
 */
public final class SharedFile extends AbstractSharedItem<SharedDirectory> {

    /**
     * Create a shared file based on the server name, share name, path name and the authentication.
     *
     * @param serverName            Name of the server
     * @param shareName             Name of the share
     * @param pathName    i ha           Path name
     * @param authenticationContext Authentication for the connection
     * @throws IOException Exception in case of a problem
     */
    public SharedFile(String serverName, String shareName, String pathName, AuthenticationContext authenticationContext) throws IOException {
        super(serverName, shareName, pathName, authenticationContext);
    }

    /**
     * Create shared file via copy constructor and a new path name.
     *
     * @param abstractSharedItem Shared item that will be reused
     * @param pathName           New path name
     */
    public SharedFile(AbstractSharedItem abstractSharedItem, String pathName) {
        super(abstractSharedItem, pathName);
    }

    /**
     * Create a new file.
     */
    public void createFile() {
        File file = diskShare.openFile(smbPath.getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        file.close();
    }

    /**
     * Delete the current file.
     */
    public void deleteFile() {
        diskShare.rm(smbPath.getPath());
    }

    /**
     * Get the input stream of the file that can be used to download the file.
     *
     * @return Input stream of the shared file
     */
    public InputStream getInputStream() {
        File file = diskShare.openFile(smbPath.getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
        return new SharedInputStream(file);
    }

    /**
     * Get the output stream of the file that can be used to upload content to this file.
     *
     * @return Output stream of the shared file
     */

    public OutputStream getOutputStream() {
        File file = diskShare.openFile(smbPath.getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        return new SharedOutputStream(file);
    }

    /**
     * Check if the current and the given objects are equals.
     *
     * @param object Given object to compare against
     * @return Status of the check
     */
    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof SharedFile) {
            SharedFile sharedFile = (SharedFile) object;
            return getSmbPath().equals(sharedFile.getSmbPath());
        } else {
            return false;
        }
    }

    /**
     * Create a new shared directory via factory.
     *
     * @param pathName Path name of the shared item
     * @return New shared directory
     */
    @Override
    protected SharedDirectory createSharedNodeItem(String pathName) {
        return new SharedDirectory(this, pathName);
    }
}
