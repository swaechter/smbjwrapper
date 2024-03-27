package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.helpers.BaseTest;
import ch.swaechter.smbjwrapper.helpers.TestConnection;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SmbDirectoryTest extends BaseTest {

    /**
     * Test all root directories and file including their attribute methods.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testRootPaths(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create a root directory
            SmbDirectory rootDirectory1 = new SmbDirectory(smbConnection);
            assertTrue(rootDirectory1.isExisting());
            assertTrue(rootDirectory1.isDirectory());
            assertFalse(rootDirectory1.isFile());
            assertEquals(testConnection.getHostName(), rootDirectory1.getServerName());
            assertEquals(testConnection.getShareName(), rootDirectory1.getShareName());
            assertEquals("", rootDirectory1.getName());
            assertEquals("", rootDirectory1.getPath());
            assertEquals("\\\\" + testConnection.getHostName() + "\\" + testConnection.getShareName(), rootDirectory1.getSmbPath());
            assertEquals(rootDirectory1, rootDirectory1.getParentPath());
            assertEquals(rootDirectory1, rootDirectory1.getRootPath());
            assertTrue(rootDirectory1.isRootPath());

            // Create a root directory
            SmbDirectory rootDirectory2 = new SmbDirectory(smbConnection, "");
            assertTrue(rootDirectory2.isExisting());
            assertTrue(rootDirectory2.isDirectory());
            assertFalse(rootDirectory2.isFile());
            assertEquals(testConnection.getHostName(), rootDirectory2.getServerName());
            assertEquals(testConnection.getShareName(), rootDirectory2.getShareName());
            assertEquals("", rootDirectory2.getName());
            assertEquals("", rootDirectory2.getPath());
            assertEquals("\\\\" + testConnection.getHostName() + "\\" + testConnection.getShareName(), rootDirectory2.getSmbPath());
            assertEquals(rootDirectory2, rootDirectory2.getParentPath());
            assertEquals(rootDirectory2, rootDirectory2.getRootPath());
            assertTrue(rootDirectory2.isRootPath());

            // Create a root file
            SmbFile rootFile1 = new SmbFile(smbConnection, "File1.txt");
            assertFalse(rootFile1.isExisting());
            assertFalse(rootFile1.isDirectory());
            assertFalse(rootFile1.isFile());
            assertEquals(testConnection.getHostName(), rootFile1.getServerName());
            assertEquals(testConnection.getShareName(), rootFile1.getShareName());
            assertEquals("File1.txt", rootFile1.getName());
            assertEquals("File1.txt", rootFile1.getPath());
            assertEquals("\\\\" + testConnection.getHostName() + "\\" + testConnection.getShareName() + "\\File1.txt", rootFile1.getSmbPath());
            assertEquals(rootDirectory1, rootFile1.getParentPath());
            assertEquals(rootDirectory1, rootFile1.getRootPath());
            assertTrue(rootFile1.isRootPath());

            // Create a root file
            SmbFile rootFile2 = new SmbFile(smbConnection, "File1.txt");

            // Compare all root directories and files
            assertEquals(rootDirectory1, rootDirectory2);
            assertNotEquals(rootDirectory1, rootFile1);
            assertEquals(rootFile1, rootFile2);
            assertNotEquals(rootFile1, rootDirectory1);
        }
    }

    /**
     * Test the recreation to prevent any open handle issues during creation/deletion and recreation/redeletion.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testRecreation(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());

            // Repeat the creation/deletion multiple times
            for (int i = 0; i < 5; i++) {
                // Create the directory
                assertFalse(transferDirectory.isExisting());
                transferDirectory.createDirectory();
                assertTrue(transferDirectory.isExisting());
                SmbDirectory smbDirectory = transferDirectory.getParentPath();
                assertEquals(smbDirectory.getRootPath().getPath(), smbDirectory.getPath());
                assertEquals("", smbDirectory.getName());
                assertTrue(smbDirectory.isRootPath());

                // Create a root file
                SmbFile transferFile = new SmbFile(smbConnection, "Text_" + buildUniquePath() + ".txt");
                transferFile.createFile();
                assertTrue(transferFile.isExisting());
                smbDirectory = transferFile.getParentPath();
                assertEquals(smbDirectory.getRootPath().getPath(), smbDirectory.getPath());
                assertEquals("", smbDirectory.getName());
                assertTrue(smbDirectory.isRootPath());

                // Recreate the file
                transferFile.deleteFile();
                assertFalse(transferFile.isExisting());
                transferFile.createFile();
                assertTrue(transferFile.isExisting());

                // Check the root directories and files
                SmbDirectory rootDirectory = new SmbDirectory(smbConnection);
                assertTrue(!rootDirectory.getDirectories().isEmpty());
                assertTrue(!rootDirectory.getFiles().isEmpty());

                // Create a sub directory
                SmbDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
                assertTrue(subDirectory1.isExisting());
                SmbDirectory rootDirectory2 = subDirectory1.getParentPath();
                assertEquals(transferDirectory.getPath(), rootDirectory2.getPath());

                // Create a sub directory
                SmbDirectory subDirectory2 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory2");
                assertTrue(subDirectory2.isExisting());

                // Create a sub file in the sub directory
                SmbFile subFile1_1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
                assertTrue(subFile1_1.isExisting());
                SmbDirectory rootDirectory3 = subFile1_1.getParentPath();
                assertEquals(subDirectory1.getPath(), rootDirectory3.getPath());

                // Create a sub file in the sub directory
                SmbFile subFile1_2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
                assertTrue(subFile1_2.isExisting());

                // Check the sub items
                assertEquals(2, transferDirectory.getDirectories().size());
                assertEquals(0, transferDirectory.getFiles().size());
                assertEquals(0, subDirectory1.getDirectories().size());
                assertEquals(2, subDirectory1.getFiles().size());

                // Clean up
                transferDirectory.deleteDirectoryRecursively();
                assertFalse(transferDirectory.isExisting());
                transferFile.deleteFile();
                assertFalse(transferFile.isExisting());
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
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testGetDirectoriesAndFiles(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create the subdirectory and subfiles
            SmbDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
            SmbFile subFile1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
            SmbFile subFile2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
            assertTrue(subDirectory1.isExisting());
            assertTrue(subFile1.isExisting());
            assertTrue(subFile2.isExisting());

            // Show all directories
            for (SmbDirectory smbDirectory : transferDirectory.getDirectories()) {
                String fileName = smbDirectory.getName();
                assertEquals(fileName, subDirectory1.getName());
                assertTrue(smbDirectory.isExisting());
            }

            // Show all files
            for (SmbFile smbFile : subDirectory1.getFiles()) {
                String fileName = smbFile.getName();
                assertTrue(fileName.equals(subFile1.getName()) || fileName.equals(subFile2.getName()));
                assertTrue(smbFile.isExisting());
            }

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            assertFalse(transferDirectory.isExisting());
        }
    }

    /**
     * Test the server side copy.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testRemoteServerCopy(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create a temporary file
            File contentFile = new File("src/test/resources/Screenshot.png");
            File tempFile = File.createTempFile("smbjwrapper", ".tmp");
            assertTrue(tempFile.exists());

            // Create the source file
            SmbFile sourceFile = transferDirectory.createFileInCurrentDirectory("ScreenshotIn.png");
            assertTrue(sourceFile.isExisting());

            // Upload a file
            InputStream inputStream2 = new FileInputStream(contentFile);
            OutputStream outputStream2 = sourceFile.getOutputStream();
            IOUtils.copy(inputStream2, outputStream2);
            inputStream2.close();
            outputStream2.close();

            // Copy the file via server side copy
            String path = sourceFile.getParentPath().getPath();
            SmbFile destinationFile = new SmbFile(smbConnection, path + "/ScreenshotOut.png");
            sourceFile.copyFileViaServerSideCopy(destinationFile);
            assertTrue(destinationFile.isExisting());
            assertEquals(sourceFile.getFileSize(), destinationFile.getFileSize());

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            assertFalse(transferDirectory.isExisting());
        }
    }

    /**
     * Test the ensure directory functionality.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testEnsureDirectory(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Ensure the directory existence
            SmbDirectory ensureDirectory1 = new SmbDirectory(smbConnection, transferDirectory.getPath() + "/EnsureDirectory1");
            assertFalse(ensureDirectory1.isExisting());
            ensureDirectory1.ensureExists();
            assertTrue(ensureDirectory1.isExisting());

            // Ensure an existing directory
            ensureDirectory1.ensureExists();

            // Ensure a non-recoverable directory existence
            try {
                SmbFile ensureFile1 = transferDirectory.createFileInCurrentDirectory("EnsureDirectory2");
                assertTrue(ensureFile1.isExisting());
                assertTrue(ensureFile1.isFile());

                SmbDirectory ensureDirectory2 = new SmbDirectory(smbConnection, transferDirectory.getPath() + "/EnsureDirectory2");
                ensureDirectory2.ensureExists();
                fail("Ensure exception not failed");
            } catch (IllegalStateException exception) {
                assertEquals("The given path does already exist, but not as directory", exception.getMessage());
            }

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            assertFalse(transferDirectory.isExisting());
        }
    }

    /**
     * Test the renaming of directories.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testRenamingTo(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create a directory in the root directory
            SmbDirectory shareRootDirectory = new SmbDirectory(smbConnection);
            SmbDirectory smbDirectory1 = shareRootDirectory.createDirectoryInCurrentDirectory("Directory1");
            assertTrue(smbDirectory1.isExisting());

            // Rename it
            SmbDirectory smbDirectory1New = smbDirectory1.renameTo("Directory1New", false);
            assertFalse(smbDirectory1.isExisting());
            assertTrue(smbDirectory1New.isExisting());

            // Cleanup
            smbDirectory1New.deleteDirectoryRecursively();
            assertFalse(smbDirectory1New.isExisting());

            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create a second directory
            SmbDirectory smbDirectory2 = transferDirectory.createDirectoryInCurrentDirectory("Directory2");
            assertTrue(smbDirectory2.isExisting());

            // Create a third directory
            SmbDirectory smbDirectory3 = transferDirectory.createDirectoryInCurrentDirectory("Directory3");
            assertTrue(smbDirectory3.isExisting());

            // Create a first file
            SmbFile smbFile1 = transferDirectory.createFileInCurrentDirectory("File1");
            assertTrue(smbFile1.isExisting());

            // Do a regular rename
            SmbDirectory smbDirectory2New = smbDirectory2.renameTo("Directory2New", false);
            assertEquals("Directory2New", smbDirectory2New.getName());
            assertEquals(transferDirectory.getPath() + "/Directory2New", smbDirectory2New.getPath());

            // Do a rename and trigger an exception
            try {
                smbDirectory2New.renameTo("Directory3", false);
                fail("Rename without replace flag should fail");
            } catch (Exception exception) {
                assertEquals("Directory2New", smbDirectory2New.getName());
                assertEquals(transferDirectory.getPath() + "/Directory2New", smbDirectory2New.getPath());
            }

            // Do a replace rename
            smbDirectory3 = smbDirectory2New.renameTo("Directory3", true);
            assertEquals("Directory3", smbDirectory3.getName());
            assertEquals(transferDirectory.getPath() + "/Directory3", smbDirectory3.getPath());

            // Do a rename to a file and trigger an exception
            try {
                smbDirectory3.renameTo("File1", true);
                fail("Rename a directory to a file should fail");
            } catch (Exception eception) {
                assertTrue(smbDirectory3.isExisting());
                assertEquals("Directory3", smbDirectory3.getName());
                assertTrue(smbFile1.isFile());
            }

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            assertFalse(transferDirectory.isExisting());
        }
    }

    /**
     * Test the directory listing.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testList(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create the directories
            List<String> directories = Arrays.asList("Dir1", "Dir1/Dir2", "Dir1/Dir2/Dir3", "Dir4", "Dir5", "Dir5/Dir1");
            directories.forEach(path -> new SmbDirectory(smbConnection, transferDirectory.getPath() + "/" + path).createDirectory());

            // Create the files
            List<String> files = Arrays.asList("File1", "Dir1/Dir2/File2", "Dir1/Dir2/File1", "Dir4/File3", "Dir5/Dir1/File3");
            files.forEach(path -> new SmbFile(smbConnection, transferDirectory.getPath() + "/" + path).createFile());

            // Search fot files in the current transfer directory
            List<SmbItem> smbItems = transferDirectory.listItems();
            assertEquals(4, smbItems.size());
            assertEquals("Dir1", smbItems.get(0).getName());
            assertEquals("Dir4", smbItems.get(1).getName());
            assertEquals("Dir5", smbItems.get(2).getName());
            assertEquals("File1", smbItems.get(3).getName());

            // Search for a few directories
            List<SmbItem> smbItems1a = transferDirectory.listItems(item -> item.getName().contains("Dir1"), false);
            List<SmbItem> smbItems1b = transferDirectory.listItems("Dir1", false);
            assertEquals(1, smbItems1a.size());
            assertEquals(transferDirectory.getPath() + "/Dir1", smbItems1a.get(0).getPath());
            assertEquals(smbItems1a, smbItems1b);

            List<SmbItem> smbItems2a = transferDirectory.listItems(item -> item.getName().contains("Dir1"), true);
            List<SmbItem> smbItems2b = transferDirectory.listItems("Dir1", true);
            assertEquals(2, smbItems2a.size());
            assertEquals(transferDirectory.getPath() + "/Dir1", smbItems2a.get(0).getPath());
            assertEquals(transferDirectory.getPath() + "/Dir5/Dir1", smbItems2a.get(1).getPath());
            assertEquals(smbItems2a, smbItems2b);

            List<SmbItem> smbItems3a = transferDirectory.listItems(item -> item.getName().contains("Dir3"), true);
            List<SmbItem> smbItems3b = transferDirectory.listItems("Dir3", true);
            assertEquals(1, smbItems3a.size());
            assertEquals(transferDirectory.getPath() + "/Dir1/Dir2/Dir3", smbItems3a.get(0).getPath());
            assertEquals(smbItems3a, smbItems3b);

            // Search for a few files
            List<SmbItem> smbItems4a = transferDirectory.listItems(item -> item.getName().contains("File1"), false);
            List<SmbItem> smbItems4b = transferDirectory.listItems("File1", false);
            assertEquals(1, smbItems4b.size());
            assertEquals("File1", smbItems4b.get(0).getName());
            assertEquals(smbItems4a, smbItems4b);

            List<SmbItem> smbItems5a = transferDirectory.listItems(item -> item.getName().contains("File1"), true);
            List<SmbItem> smbItems5b = transferDirectory.listItems("File1", true);
            assertEquals(2, smbItems5a.size());
            assertEquals(transferDirectory.getPath() + "/Dir1/Dir2/File1", smbItems5a.get(0).getPath());
            assertEquals(transferDirectory.getPath() + "/File1", smbItems5a.get(1).getPath());
            assertEquals(smbItems5a, smbItems5b);

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            assertFalse(transferDirectory.isExisting());
        }
    }
}
