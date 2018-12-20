package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.core.SharedItem;
import ch.swaechter.smbjwrapper.helpers.TestConnection;
import ch.swaechter.smbjwrapper.helpers.TestConnectionFactory;
import ch.swaechter.smbjwrapper.helpers.TestHelpers;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SharedDirectoryTest {

    /**
     * Test all root directories and file including their attribute methods.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testRootPaths(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create a root share directory
            SharedDirectory rootDirectory1 = new SharedDirectory(sharedConnection);
            Assertions.assertTrue(rootDirectory1.isExisting());
            Assertions.assertTrue(rootDirectory1.isDirectory());
            Assertions.assertFalse(rootDirectory1.isFile());
            Assertions.assertEquals(testConnection.getHostName(), rootDirectory1.getServerName());
            Assertions.assertEquals(testConnection.getShareName(), rootDirectory1.getShareName());
            Assertions.assertEquals("", rootDirectory1.getName());
            Assertions.assertEquals("", rootDirectory1.getPath());
            Assertions.assertEquals("\\\\" + testConnection.getHostName() + "\\" + testConnection.getShareName(), rootDirectory1.getSmbPath());
            Assertions.assertEquals(rootDirectory1, rootDirectory1.getParentPath());
            Assertions.assertEquals(rootDirectory1, rootDirectory1.getRootPath());
            Assertions.assertTrue(rootDirectory1.isRootPath());

            // Create a root share directory
            SharedDirectory rootDirectory2 = new SharedDirectory(sharedConnection, "");
            Assertions.assertTrue(rootDirectory2.isExisting());
            Assertions.assertTrue(rootDirectory2.isDirectory());
            Assertions.assertFalse(rootDirectory2.isFile());
            Assertions.assertEquals(testConnection.getHostName(), rootDirectory2.getServerName());
            Assertions.assertEquals(testConnection.getShareName(), rootDirectory2.getShareName());
            Assertions.assertEquals("", rootDirectory2.getName());
            Assertions.assertEquals("", rootDirectory2.getPath());
            Assertions.assertEquals("\\\\" + testConnection.getHostName() + "\\" + testConnection.getShareName(), rootDirectory2.getSmbPath());
            Assertions.assertEquals(rootDirectory2, rootDirectory2.getParentPath());
            Assertions.assertEquals(rootDirectory2, rootDirectory2.getRootPath());
            Assertions.assertTrue(rootDirectory2.isRootPath());

            // Create a root share file
            SharedFile rootFile1 = new SharedFile(sharedConnection, "File1.txt");
            Assertions.assertFalse(rootFile1.isExisting());
            Assertions.assertFalse(rootFile1.isDirectory());
            Assertions.assertFalse(rootFile1.isFile());
            Assertions.assertEquals(testConnection.getHostName(), rootFile1.getServerName());
            Assertions.assertEquals(testConnection.getShareName(), rootFile1.getShareName());
            Assertions.assertEquals("File1.txt", rootFile1.getName());
            Assertions.assertEquals("File1.txt", rootFile1.getPath());
            Assertions.assertEquals("\\\\" + testConnection.getHostName() + "\\" + testConnection.getShareName() + "\\File1.txt", rootFile1.getSmbPath());
            Assertions.assertEquals(rootDirectory1, rootFile1.getParentPath());
            Assertions.assertEquals(rootDirectory1, rootFile1.getRootPath());
            Assertions.assertTrue(rootFile1.isRootPath());

            // Create a root share file
            SharedFile rootFile2 = new SharedFile(sharedConnection, "File1.txt");

            // Compare all root share directories and files
            Assertions.assertEquals(rootDirectory1, rootDirectory2);
            Assertions.assertNotEquals(rootDirectory1, rootFile1);
            Assertions.assertEquals(rootFile1, rootFile2);
            Assertions.assertNotEquals(rootFile1, rootDirectory1);
        }
    }

    /**
     * Test the recreation to prevent any open handle issues during creation/deletion and recreation/redeletion.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testRecreation(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());

            // Repeat the creation/deletion multiple times
            for (int i = 0; i < 5; i++) {
                // Create the directory
                Assertions.assertFalse(transferDirectory.isExisting());
                transferDirectory.createDirectory();
                Assertions.assertTrue(transferDirectory.isExisting());
                SharedDirectory shareDirectory = transferDirectory.getParentPath();
                Assertions.assertEquals(shareDirectory.getRootPath().getPath(), shareDirectory.getPath());
                Assertions.assertEquals("", shareDirectory.getName());
                Assertions.assertTrue(shareDirectory.isRootPath());

                // Create a root file
                SharedFile transferFile = new SharedFile(sharedConnection, "Text_" + TestHelpers.buildUniquePath() + ".txt");
                transferFile.createFile();
                Assertions.assertTrue(transferFile.isExisting());
                shareDirectory = transferFile.getParentPath();
                Assertions.assertEquals(shareDirectory.getRootPath().getPath(), shareDirectory.getPath());
                Assertions.assertEquals("", shareDirectory.getName());
                Assertions.assertTrue(shareDirectory.isRootPath());

                // Recreate the file
                transferFile.deleteFile();
                Assertions.assertFalse(transferFile.isExisting());
                transferFile.createFile();
                Assertions.assertTrue(transferFile.isExisting());

                // Check the share root
                SharedDirectory rootDirectory = new SharedDirectory(sharedConnection);
                Assertions.assertTrue(!rootDirectory.getDirectories().isEmpty());
                Assertions.assertTrue(!rootDirectory.getFiles().isEmpty());

                // Create a sub directory
                SharedDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
                Assertions.assertTrue(subDirectory1.isExisting());
                SharedDirectory rootDirectory2 = subDirectory1.getParentPath();
                Assertions.assertEquals(transferDirectory.getPath(), rootDirectory2.getPath());

                // Create a sub directory
                SharedDirectory subDirectory2 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory2");
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
                Assertions.assertEquals(2, transferDirectory.getDirectories().size());
                Assertions.assertEquals(0, transferDirectory.getFiles().size());
                Assertions.assertEquals(0, subDirectory1.getDirectories().size());
                Assertions.assertEquals(2, subDirectory1.getFiles().size());

                // Recreate the directory
                transferDirectory.deleteDirectoryRecursively();
            }
        }
    }

    /**
     * Test the list directory and file method.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testGetDirectoriesAndFiles(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Create the subdirectory and subfiles
            SharedDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
            SharedFile subFile1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
            SharedFile subFile2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
            Assertions.assertTrue(subDirectory1.isExisting());
            Assertions.assertTrue(subFile1.isExisting());
            Assertions.assertTrue(subFile2.isExisting());

            // Show all directories
            for (SharedDirectory sharedDirectory : transferDirectory.getDirectories()) {
                String fileName = sharedDirectory.getName();
                Assertions.assertEquals(fileName, subDirectory1.getName());
                Assertions.assertTrue(sharedDirectory.isExisting());
            }

            // Show all files
            for (SharedFile sharedFile : subDirectory1.getFiles()) {
                String fileName = sharedFile.getName();
                Assertions.assertTrue(fileName.equals(subFile1.getName()) || fileName.equals(subFile2.getName()));
                Assertions.assertTrue(sharedFile.isExisting());
            }
        }
    }

    /**
     * Test the server side copy.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testRemoteServerCopy(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Create a temporary file
            File contentFile = new File("src/test/resources/Screenshot.png");
            File tempFile = File.createTempFile("smbjwrapper", ".tmp");
            Assertions.assertTrue(tempFile.exists());

            // Create the source file
            SharedFile sourceFile = transferDirectory.createFileInCurrentDirectory("ScreenshotIn.png");
            Assertions.assertTrue(sourceFile.isExisting());

            // Upload a file
            InputStream inputStream2 = new FileInputStream(contentFile);
            OutputStream outputStream2 = sourceFile.getOutputStream();
            IOUtils.copy(inputStream2, outputStream2);
            inputStream2.close();
            outputStream2.close();

            // Copy the file via server side copy
            String path = sourceFile.getParentPath().getPath();
            SharedFile destinationFile = new SharedFile(sharedConnection, path + "/ScreenshotOut.png");
            sourceFile.copyFileViaServerSideCopy(destinationFile);
            Assertions.assertTrue(destinationFile.isExisting());
            Assertions.assertEquals(sourceFile.getFileSize(), destinationFile.getFileSize());
        }
    }

    /**
     * Test the ensure directory functionality.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testEnsureDirectory(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Ensure the directory existence
            SharedDirectory ensureDirectory1 = new SharedDirectory(sharedConnection, transferDirectory.getPath() + "/EnsureDirectory1");
            Assertions.assertFalse(ensureDirectory1.isExisting());
            ensureDirectory1.ensureExists();
            Assertions.assertTrue(ensureDirectory1.isExisting());

            // Ensure an existing directory
            ensureDirectory1.ensureExists();

            // Ensure a non-recoverable directory existence
            try {
                SharedFile ensureFile1 = transferDirectory.createFileInCurrentDirectory("EnsureDirectory2");
                Assertions.assertTrue(ensureFile1.isExisting());
                Assertions.assertTrue(ensureFile1.isFile());

                SharedDirectory ensureDirectory2 = new SharedDirectory(sharedConnection, transferDirectory.getPath() + "/EnsureDirectory2");
                ensureDirectory2.ensureExists();
                Assertions.fail("Ensure exception not failed");
            } catch (IllegalStateException exception) {
                Assertions.assertEquals("The given path does already exist, but not as directory", exception.getMessage());
            }
        }
    }

    /**
     * Test the renaming of directories.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testRenaming(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Create a first directory
            SharedDirectory sharedDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Directory1");
            Assertions.assertTrue(sharedDirectory1.isExisting());

            // Create a second directory
            SharedDirectory sharedDirectory2 = transferDirectory.createDirectoryInCurrentDirectory("Directory2");
            Assertions.assertTrue(sharedDirectory2.isExisting());

            // Create a first file
            SharedFile sharedFile1 = transferDirectory.createFileInCurrentDirectory("File1");
            Assertions.assertTrue(sharedFile1.isExisting());

            // Do a regular rename
            sharedDirectory1 = sharedDirectory1.renameTo("Directory1New", false);
            Assertions.assertEquals("Directory1New", sharedDirectory1.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/Directory1New", sharedDirectory1.getPath());

            // Do a rename and trigger an exception
            try {
                sharedDirectory1 = sharedDirectory1.renameTo("Directory2", false);
                Assertions.fail("Rename without replace flag should fail");
            } catch (Exception exception) {
                Assertions.assertEquals("Directory1New", sharedDirectory1.getName());
                Assertions.assertEquals(transferDirectory.getPath() + "/Directory1New", sharedDirectory1.getPath());
            }

            // Do a replace rename
            sharedDirectory1 = sharedDirectory1.renameTo("Directory2", true);
            Assertions.assertEquals("Directory2", sharedDirectory1.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/Directory2", sharedDirectory1.getPath());

            // Do a rename to a file and trigger an exception
            try {
                sharedDirectory1 = sharedDirectory1.renameTo("File1", true);
                Assertions.fail("Rename a directory to a file should fail");
            } catch (Exception eception) {
                Assertions.assertTrue(sharedDirectory1.isExisting());
                Assertions.assertEquals("Directory2", sharedDirectory1.getName());
                Assertions.assertTrue(sharedFile1.isFile());
            }
        }
    }

    /**
     * Test the directory listing.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testList(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Create the directories
            List<String> directories = Arrays.asList("Dir1", "Dir1/Dir2", "Dir1/Dir2/Dir3", "Dir4", "Dir5", "Dir5/Dir1");
            directories.forEach(path -> new SharedDirectory(sharedConnection, transferDirectory.getPath() + "/" + path).createDirectory());

            // Create the files
            List<String> files = Arrays.asList("File1", "Dir1/Dir2/File2", "Dir1/Dir2/File1", "Dir4/File3", "Dir5/Dir1/File3");
            files.forEach(path -> new SharedFile(sharedConnection, transferDirectory.getPath() + "/" + path).createFile());

            // Search for a few directories
            List<SharedItem> sharedItems1 = transferDirectory.listFiles("Dir1", false);
            Assertions.assertEquals(1, sharedItems1.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1", sharedItems1.get(0).getPath());

            List<SharedItem> sharedItems2 = transferDirectory.listFiles("Dir1", true);
            Assertions.assertEquals(2, sharedItems2.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1", sharedItems2.get(0).getPath());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir5/Dir1", sharedItems2.get(1).getPath());

            List<SharedItem> sharedItems3 = transferDirectory.listFiles("Dir3", true);
            Assertions.assertEquals(1, sharedItems3.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1/Dir2/Dir3", sharedItems3.get(0).getPath());

            // Search for a few files
            List<SharedItem> sharedItems4 = transferDirectory.listFiles("File1", false);
            Assertions.assertEquals(1, sharedItems4.size());
            Assertions.assertEquals("File1", sharedItems4.get(0).getName());

            List<SharedItem> sharedItems5 = transferDirectory.listFiles("File1", true);
            Assertions.assertEquals(2, sharedItems5.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1/Dir2/File1", sharedItems5.get(0).getPath());
            Assertions.assertEquals(transferDirectory.getPath() + "/File1", sharedItems5.get(1).getPath());
        }
    }

    private static Stream getTestConnections() {
        return TestConnectionFactory.getTestConnections();
    }
}
