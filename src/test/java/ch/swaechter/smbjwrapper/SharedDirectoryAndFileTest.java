package ch.swaechter.smbjwrapper;

import com.hierynomus.smbj.auth.AuthenticationContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class SharedDirectoryAndFileTest {

    private final String serverName = "127.0.0.1";

    private final String shareName = "Share";

    private final AuthenticationContext authenticationContext = AuthenticationContext.anonymous();

    @Test
    public void testRootPaths() throws Exception {
        // Create a root share directory
        SharedDirectory rootDirectory1 = new SharedDirectory(serverName, shareName, authenticationContext);
        Assertions.assertTrue(rootDirectory1.isExisting());
        Assertions.assertTrue(rootDirectory1.isDirectory());
        Assertions.assertFalse(rootDirectory1.isFile());
        Assertions.assertEquals(serverName, rootDirectory1.getServerName());
        Assertions.assertEquals(shareName, rootDirectory1.getShareName());
        Assertions.assertEquals("\\\\" + serverName + "\\" + shareName + "\\", rootDirectory1.getSmbPath());
        Assertions.assertEquals("", rootDirectory1.getName());
        Assertions.assertEquals("", rootDirectory1.getPath());
        Assertions.assertEquals(rootDirectory1, rootDirectory1.getParentPath());
        Assertions.assertEquals(rootDirectory1, rootDirectory1.getRootPath());
        Assertions.assertTrue(rootDirectory1.isRootPath());

        // Create a root share directory
        SharedDirectory rootDirectory2 = new SharedDirectory(serverName, shareName, "", authenticationContext);
        Assertions.assertTrue(rootDirectory2.isExisting());
        Assertions.assertTrue(rootDirectory2.isDirectory());
        Assertions.assertFalse(rootDirectory2.isFile());
        Assertions.assertEquals(serverName, rootDirectory2.getServerName());
        Assertions.assertEquals(shareName, rootDirectory2.getShareName());
        Assertions.assertEquals("\\\\" + serverName + "\\" + shareName + "\\", rootDirectory2.getSmbPath());
        Assertions.assertEquals("", rootDirectory2.getName());
        Assertions.assertEquals("", rootDirectory2.getPath());
        Assertions.assertEquals(rootDirectory2, rootDirectory2.getParentPath());
        Assertions.assertEquals(rootDirectory2, rootDirectory2.getRootPath());
        Assertions.assertTrue(rootDirectory2.isRootPath());

        // Create a root share file
        SharedFile rootFile1 = new SharedFile(serverName, shareName, "File1.txt", authenticationContext);
        Assertions.assertFalse(rootFile1.isExisting());
        Assertions.assertFalse(rootFile1.isDirectory());
        Assertions.assertFalse(rootFile1.isFile());
        Assertions.assertEquals(serverName, rootFile1.getServerName());
        Assertions.assertEquals(shareName, rootFile1.getShareName());
        Assertions.assertEquals("\\\\" + serverName + "\\" + shareName + "\\File1.txt", rootFile1.getSmbPath());
        Assertions.assertEquals("File1.txt", rootFile1.getName());
        Assertions.assertEquals("File1.txt", rootFile1.getPath());
        Assertions.assertEquals(rootDirectory1, rootFile1.getParentPath());
        Assertions.assertEquals(rootDirectory1, rootFile1.getRootPath());
        Assertions.assertTrue(rootFile1.isRootPath());

        // Create a root share file
        SharedFile rootFile2 = new SharedFile(serverName, shareName, "File1.txt", authenticationContext);

        // Compare all root share directories and files
        Assertions.assertEquals(rootDirectory1, rootDirectory2);
        Assertions.assertNotEquals(rootDirectory1, rootFile1);
        Assertions.assertEquals(rootFile1, rootFile2);
        Assertions.assertNotEquals(rootFile1, rootDirectory1);
    }

    @Test
    public void testRecreation() throws Exception {
        // Create the entry point directory
        String baseName = "Transfer_" + UUID.randomUUID();
        SharedDirectory rootDirectory = new SharedDirectory(serverName, shareName, baseName, authenticationContext);

        // Create the directory
        Assertions.assertFalse(rootDirectory.isExisting());
        rootDirectory.createDirectory();
        Assertions.assertTrue(rootDirectory.isExisting());
        SharedDirectory shareDirectory = rootDirectory.getParentPath();
        Assertions.assertEquals(shareDirectory.getRootPath().getPath(), shareDirectory.getPath());
        Assertions.assertEquals("", shareDirectory.getName());
        Assertions.assertTrue(shareDirectory.isRootPath());

        // Create a root file
        SharedFile rootFile = new SharedFile(serverName, shareName, "Text_" + baseName + ".txt", authenticationContext);
        rootFile.createFile();
        Assertions.assertTrue(rootFile.isExisting());
        shareDirectory = rootFile.getParentPath();
        Assertions.assertEquals(shareDirectory.getRootPath().getPath(), shareDirectory.getPath());
        Assertions.assertEquals("", shareDirectory.getName());
        Assertions.assertTrue(shareDirectory.isRootPath());

        // Recreate the file
        rootFile.deleteFile();
        Assertions.assertFalse(rootFile.isExisting());
        rootFile.createFile();
        Assertions.assertTrue(rootFile.isExisting());

        // Check the share root
        SharedDirectory shareRoot = new SharedDirectory(serverName, shareName, AuthenticationContext.anonymous());
        Assertions.assertTrue(!shareRoot.getDirectories().isEmpty());
        Assertions.assertTrue(!shareRoot.getFiles().isEmpty());

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
    }

    @Test
    public void testGetDirectoriesAndFiles() throws Exception {
        // Create the entry point directory
        String baseName = "Transfer_" + UUID.randomUUID();
        SharedDirectory rootDirectory = new SharedDirectory(serverName, shareName, baseName, authenticationContext);
        rootDirectory.createDirectory();

        // Create the subdirectory and subfiles
        SharedDirectory subDirectory1 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
        SharedFile subFile1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
        SharedFile subFile2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");

        // Show all directories
        for (SharedDirectory sharedDirectory : rootDirectory.getDirectories()) {
            String fileName = sharedDirectory.getName();
            Assertions.assertTrue(fileName.equals(subDirectory1.getName()));
        }

        // Show all files
        for (SharedFile sharedFile : subDirectory1.getFiles()) {
            String fileName = sharedFile.getName();
            Assertions.assertTrue(fileName.equals(subFile1.getName()) || fileName.equals(subFile2.getName()));
        }
    }

    @Test
    public void testDownAndUpload() throws Exception {
        // Create the entry point directory
        String baseName = "Transfer_" + UUID.randomUUID();
        SharedDirectory rootDirectory = new SharedDirectory(serverName, shareName, baseName, authenticationContext);
        rootDirectory.createDirectory();

        // Create the subdirectory and subfiles
        SharedDirectory subDirectory1 = rootDirectory.createDirectoryInCurrentDirectory("Subdirectory1");

        // Upload a file
        InputStream inputStream = new FileInputStream(new File("src/test/resources/Screenshot.png"));
        Assertions.assertNotNull(inputStream);

        SharedFile subFile2_1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
        OutputStream outputStream = subFile2_1.getOutputStream();
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();

        // Transfer/download a file
        SharedFile subFile2_2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
        inputStream = subFile2_1.getInputStream();
        outputStream = subFile2_2.getOutputStream();

        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();

        // Clean up
        rootDirectory.deleteDirectoryRecursively();
        Assertions.assertFalse(rootDirectory.isExisting());
    }
}
