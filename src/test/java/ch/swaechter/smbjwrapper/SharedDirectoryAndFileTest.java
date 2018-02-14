package ch.swaechter.smbjwrapper;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.share.DiskShare;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.UUID;

public class SharedDirectoryAndFileTest {

    @Test
    public void testCoreFunctionality() throws Exception {
        // Create the entry point directory
        String baseName = "Transfer_" + UUID.randomUUID();
        AuthenticationContext authenticationContext = AuthenticationContext.anonymous();
        SharedDirectory rootDirectory = new SharedDirectory("127.0.0.1", "Share", baseName, authenticationContext);

        // Create the directory
        Assertions.assertFalse(rootDirectory.isExisting());
        rootDirectory.createDirectory();
        Assertions.assertTrue(rootDirectory.isExisting());
        SharedDirectory shareDirectory = rootDirectory.getParentPath();
        Assertions.assertEquals(shareDirectory.getRootPath().getPath(), shareDirectory.getPath());
        Assertions.assertEquals("", shareDirectory.getName());
        Assertions.assertTrue(shareDirectory.isRootPath());

        // Create a root file
        SharedFile rootFile = new SharedFile("127.0.0.1", "Share", "Text_" + baseName + ".txt", authenticationContext);
        rootFile.createFile();
        Assertions.assertTrue(rootFile.isExisting());
        shareDirectory = rootFile.getParentPath();
        Assertions.assertEquals(shareDirectory.getRootPath().getPath(), shareDirectory.getPath());
        Assertions.assertEquals("", shareDirectory.getName());
        Assertions.assertTrue(shareDirectory.isRootPath());

        // Create a sub directory
        SharedDirectory subDirectory1 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
        Assertions.assertTrue(subDirectory1.isExisting());
        SharedDirectory rootDirectory2 = subDirectory1.getParentPath();
        Assertions.assertEquals(rootDirectory.getPath(), rootDirectory2.getPath());

        // Create a sub directory
        SharedDirectory subDirectory2 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory2");
        Assertions.assertTrue(subDirectory2.isExisting());

        // Create a sub file in the sub directory
        SharedFile subFile1_1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
        Assertions.assertTrue(subFile1_1.isExisting());
        SharedDirectory rootDirectory3 = subFile1_1.getParentPath();
        Assertions.assertEquals(subDirectory1.getPath(), rootDirectory3.getPath());

        // Create a sub file in the sub directory
        SharedFile subFile1_2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
        Assertions.assertTrue(subFile1_2.isExisting());

        // Check the sub items
        Assertions.assertEquals(2, rootDirectory.getDirectories().size());
        Assertions.assertEquals(0, rootDirectory.getFiles().size());
        Assertions.assertEquals(0, subDirectory1.getDirectories().size());
        Assertions.assertEquals(2, subDirectory1.getFiles().size());

        // Recreate the directory
        rootDirectory.deleteDirectoryRecursively();
        rootDirectory.createDirectory();

        // Create a sub directory
        subDirectory1 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
        Assertions.assertTrue(subDirectory1.isExisting());

        // Create a sub directory
        subDirectory2 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory2");
        Assertions.assertTrue(subDirectory2.isExisting());

        // Create a sub file in the sub directory
        subFile1_1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
        Assertions.assertTrue(subFile1_1.isExisting());

        // Create a sub file in the sub directory
        subFile1_2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
        Assertions.assertTrue(subFile1_2.isExisting());

        // Recreate the directory
        rootDirectory.deleteDirectoryRecursively();
        rootDirectory.createDirectory();

        // Create a sub directory
        subDirectory1 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
        Assertions.assertTrue(subDirectory1.isExisting());

        // Create a sub directory
        subDirectory2 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory2");
        Assertions.assertTrue(subDirectory2.isExisting());

        // Create a sub file in the sub directory
        subFile1_1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
        Assertions.assertTrue(subFile1_1.isExisting());

        // Create a sub file in the sub directory
        subFile1_2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
        Assertions.assertTrue(subFile1_2.isExisting());

        // Show all directories
        for (SharedDirectory sharedDirectory : rootDirectory.getDirectories()) {
            String fileName = sharedDirectory.getName();
            Assertions.assertTrue(fileName.equals("Subdirectory1") || fileName.equals("Subdirectory2"));
        }

        // Show all files
        for (SharedFile sharedFile : subDirectory1.getFiles()) {
            String fileName = sharedFile.getName();
            Assertions.assertTrue(fileName.equals("Subfile1.txt") || fileName.equals("Subfile2.txt"));
        }

        // Delete a sub file in the sub directory
        subFile1_2.deleteFile();
        Assertions.assertFalse(subFile1_2.isExisting());

        // Delete the sub directory
        subDirectory1.deleteDirectoryRecursively();
        Assertions.assertFalse(subDirectory1.isExisting());
        Assertions.assertFalse(subFile1_1.isExisting());
        Assertions.assertFalse(subFile1_2.isExisting());

        // Get and check the root paths
        String directoryRootPath1 = rootDirectory.getRootPath().getPath();
        String directoryRootPath2 = subDirectory1.getRootPath().getPath();
        String fileRootPath1 = subFile1_1.getRootPath().getPath();
        Assertions.assertEquals(directoryRootPath1, directoryRootPath2);
        Assertions.assertEquals(directoryRootPath1, fileRootPath1);
        Assertions.assertEquals("\\\\127.0.0.1\\Share\\" + baseName, rootDirectory.getPath());

        // Upload a file
        InputStream inputStream = new FileInputStream(new File("src/test/resources/Screenshot.png"));
        Assertions.assertNotNull(inputStream);

        SharedFile subFile2_1 = subDirectory2.createFileInCurrentDirectory("Subfile1.txt");
        OutputStream outputStream = subFile2_1.getOutputStream();
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();

        // Transfer/download a file
        SharedFile subFile2_2 = subDirectory2.createFileInCurrentDirectory("Subfile2.txt");
        inputStream = subFile2_1.getInputStream();
        outputStream = subFile2_2.getOutputStream();

        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();

        // Clean up
        rootDirectory.deleteDirectoryRecursively();
        rootFile.deleteFile();
    }

    @Test
    public void testCreationAndDeletion() throws Exception {
        /*String baseName = "Transfer10";//_" + UUID.randomUUID();
        AuthenticationContext authenticationContext = AuthenticationContext.anonymous();

        SMBClient smbClient = new SMBClient();
        Connection connection = smbClient.connect("127.0.0.1");
        Session session = connection.authenticate(authenticationContext);
        DiskShare diskShare = (DiskShare) session.connectShare("Share");

        createDirectory(baseName, diskShare);
        deleteDirectory(baseName, diskShare);
        createDirectory(baseName, diskShare);

        createFile(baseName + "/foo", diskShare);
        deleteFile(baseName + "/foo", diskShare);
        createFile(baseName + "/foo", diskShare);

        createDirectory(baseName + "/dir", diskShare);
        createFile(baseName + "/dir/data.txt", diskShare);
        deleteFile(baseName + "/dir/data.txt", diskShare);
        deleteDirectory(baseName + "/dir", diskShare);

        createDirectory(baseName + "/dir", diskShare);
        deleteDirectory(baseName + "/dir", diskShare);
        createDirectory(baseName + "/dir", diskShare);

        deleteDirectory(baseName, diskShare);*/
    }

    void createDirectory(String pathName, DiskShare diskShare) {
        diskShare.mkdir(pathName);
        /*Directory directory = diskShare.openDirectory(pathName, EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN_IF, null);
        directory.close();*/
    }

    void deleteDirectory(String pathName, DiskShare diskShare) {
        /*Directory directory = diskShare.openDirectory(pathName, EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN_IF, null);
        directory.deleteOnClose();
        directory.close();*/
        diskShare.rmdir(pathName, true);
    }

    void createFile(String pathName, DiskShare diskShare) {
        com.hierynomus.smbj.share.File file = diskShare.openFile(pathName, EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        file.deleteOnClose();
        file.close();
    }

    void deleteFile(String pathName, DiskShare diskShare) {
        com.hierynomus.smbj.share.File file = diskShare.openFile(pathName, EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        file.deleteOnClose();
        //diskShare.rm(pathName);
    }
}
