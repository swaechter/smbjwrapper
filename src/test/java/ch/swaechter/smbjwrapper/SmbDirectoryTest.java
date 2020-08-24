package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.helpers.BaseTest;
import ch.swaechter.smbjwrapper.helpers.TestConnection;
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

            // Create a root directory
            SmbDirectory rootDirectory2 = new SmbDirectory(smbConnection, "");
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

            // Create a root file
            SmbFile rootFile1 = new SmbFile(smbConnection, "File1.txt");
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

            // Create a root file
            SmbFile rootFile2 = new SmbFile(smbConnection, "File1.txt");

            // Compare all root directories and files
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
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testRecreation(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());

            // Repeat the creation/deletion multiple times
            for (int i = 0; i < 5; i++) {
                // Create the directory
                Assertions.assertFalse(transferDirectory.isExisting());
                transferDirectory.createDirectory();
                Assertions.assertTrue(transferDirectory.isExisting());
                SmbDirectory smbDirectory = transferDirectory.getParentPath();
                Assertions.assertEquals(smbDirectory.getRootPath().getPath(), smbDirectory.getPath());
                Assertions.assertEquals("", smbDirectory.getName());
                Assertions.assertTrue(smbDirectory.isRootPath());

                // Create a root file
                SmbFile transferFile = new SmbFile(smbConnection, "Text_" + buildUniquePath() + ".txt");
                transferFile.createFile();
                Assertions.assertTrue(transferFile.isExisting());
                smbDirectory = transferFile.getParentPath();
                Assertions.assertEquals(smbDirectory.getRootPath().getPath(), smbDirectory.getPath());
                Assertions.assertEquals("", smbDirectory.getName());
                Assertions.assertTrue(smbDirectory.isRootPath());

                // Recreate the file
                transferFile.deleteFile();
                Assertions.assertFalse(transferFile.isExisting());
                transferFile.createFile();
                Assertions.assertTrue(transferFile.isExisting());

                // Check the root directories and files
                SmbDirectory rootDirectory = new SmbDirectory(smbConnection);
                Assertions.assertTrue(!rootDirectory.getDirectories().isEmpty());
                Assertions.assertTrue(!rootDirectory.getFiles().isEmpty());

                // Create a sub directory
                SmbDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory1");
                Assertions.assertTrue(subDirectory1.isExisting());
                SmbDirectory rootDirectory2 = subDirectory1.getParentPath();
                Assertions.assertEquals(transferDirectory.getPath(), rootDirectory2.getPath());

                // Create a sub directory
                SmbDirectory subDirectory2 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory2");
                Assertions.assertTrue(subDirectory2.isExisting());

                // Create a sub file in the sub directory
                SmbFile subFile1_1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
                Assertions.assertTrue(subFile1_1.isExisting());
                SmbDirectory rootDirectory3 = subFile1_1.getParentPath();
                Assertions.assertEquals(subDirectory1.getPath(), rootDirectory3.getPath());

                // Create a sub file in the sub directory
                SmbFile subFile1_2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
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
            Assertions.assertTrue(subDirectory1.isExisting());
            Assertions.assertTrue(subFile1.isExisting());
            Assertions.assertTrue(subFile2.isExisting());

            // Show all directories
            for (SmbDirectory smbDirectory : transferDirectory.getDirectories()) {
                String fileName = smbDirectory.getName();
                Assertions.assertEquals(fileName, subDirectory1.getName());
                Assertions.assertTrue(smbDirectory.isExisting());
            }

            // Show all files
            for (SmbFile smbFile : subDirectory1.getFiles()) {
                String fileName = smbFile.getName();
                Assertions.assertTrue(fileName.equals(subFile1.getName()) || fileName.equals(subFile2.getName()));
                Assertions.assertTrue(smbFile.isExisting());
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
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testRemoteServerCopy(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create a temporary file
            File contentFile = new File("src/test/resources/Screenshot.png");
            File tempFile = File.createTempFile("smbjwrapper", ".tmp");
            Assertions.assertTrue(tempFile.exists());

            // Create the source file
            SmbFile sourceFile = transferDirectory.createFileInCurrentDirectory("ScreenshotIn.png");
            Assertions.assertTrue(sourceFile.isExisting());

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
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testEnsureDirectory(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Ensure the directory existence
            SmbDirectory ensureDirectory1 = new SmbDirectory(smbConnection, transferDirectory.getPath() + "/EnsureDirectory1");
            Assertions.assertFalse(ensureDirectory1.isExisting());
            ensureDirectory1.ensureExists();
            Assertions.assertTrue(ensureDirectory1.isExisting());

            // Ensure an existing directory
            ensureDirectory1.ensureExists();

            // Ensure a non-recoverable directory existence
            try {
                SmbFile ensureFile1 = transferDirectory.createFileInCurrentDirectory("EnsureDirectory2");
                Assertions.assertTrue(ensureFile1.isExisting());
                Assertions.assertTrue(ensureFile1.isFile());

                SmbDirectory ensureDirectory2 = new SmbDirectory(smbConnection, transferDirectory.getPath() + "/EnsureDirectory2");
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
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testRenamingTo(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create a directory in the root directory
            SmbDirectory shareRootDirectory = new SmbDirectory(smbConnection);
            SmbDirectory smbDirectory1 = shareRootDirectory.createDirectoryInCurrentDirectory("Directory1");
            Assertions.assertTrue(smbDirectory1.isExisting());

            // Rename it
            SmbDirectory smbDirectory1New = smbDirectory1.renameTo("Directory1New", false);
            Assertions.assertFalse(smbDirectory1.isExisting());
            Assertions.assertTrue(smbDirectory1New.isExisting());

            // Cleanup
            smbDirectory1New.deleteDirectoryRecursively();
            Assertions.assertFalse(smbDirectory1New.isExisting());

            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create a second directory
            SmbDirectory smbDirectory2 = transferDirectory.createDirectoryInCurrentDirectory("Directory2");
            Assertions.assertTrue(smbDirectory2.isExisting());

            // Create a third directory
            SmbDirectory smbDirectory3 = transferDirectory.createDirectoryInCurrentDirectory("Directory3");
            Assertions.assertTrue(smbDirectory3.isExisting());

            // Create a first file
            SmbFile smbFile1 = transferDirectory.createFileInCurrentDirectory("File1");
            Assertions.assertTrue(smbFile1.isExisting());

            // Do a regular rename
            SmbDirectory smbDirectory2New = smbDirectory2.renameTo("Directory2New", false);
            Assertions.assertEquals("Directory2New", smbDirectory2New.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/Directory2New", smbDirectory2New.getPath());

            // Do a rename and trigger an exception
            try {
                smbDirectory2New.renameTo("Directory3", false);
                Assertions.fail("Rename without replace flag should fail");
            } catch (Exception exception) {
                Assertions.assertEquals("Directory2New", smbDirectory2New.getName());
                Assertions.assertEquals(transferDirectory.getPath() + "/Directory2New", smbDirectory2New.getPath());
            }

            // Do a replace rename
            smbDirectory3 = smbDirectory2New.renameTo("Directory3", true);
            Assertions.assertEquals("Directory3", smbDirectory3.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/Directory3", smbDirectory3.getPath());

            // Do a rename to a file and trigger an exception
            try {
                smbDirectory3.renameTo("File1", true);
                Assertions.fail("Rename a directory to a file should fail");
            } catch (Exception eception) {
                Assertions.assertTrue(smbDirectory3.isExisting());
                Assertions.assertEquals("Directory3", smbDirectory3.getName());
                Assertions.assertTrue(smbFile1.isFile());
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
            Assertions.assertEquals(4, smbItems.size());
            Assertions.assertEquals("Dir1", smbItems.get(0).getName());
            Assertions.assertEquals("Dir4", smbItems.get(1).getName());
            Assertions.assertEquals("Dir5", smbItems.get(2).getName());
            Assertions.assertEquals("File1", smbItems.get(3).getName());

            // Search for a few directories
            List<SmbItem> smbItems1a = transferDirectory.listItems(item -> item.getName().contains("Dir1"), false);
            List<SmbItem> smbItems1b = transferDirectory.listItems("Dir1", false);
            Assertions.assertEquals(1, smbItems1a.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1", smbItems1a.get(0).getPath());
            Assertions.assertEquals(smbItems1a, smbItems1b);

            List<SmbItem> smbItems2a = transferDirectory.listItems(item -> item.getName().contains("Dir1"), true);
            List<SmbItem> smbItems2b = transferDirectory.listItems("Dir1", true);
            Assertions.assertEquals(2, smbItems2a.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1", smbItems2a.get(0).getPath());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir5/Dir1", smbItems2a.get(1).getPath());
            Assertions.assertEquals(smbItems2a, smbItems2b);

            List<SmbItem> smbItems3a = transferDirectory.listItems(item -> item.getName().contains("Dir3"), true);
            List<SmbItem> smbItems3b = transferDirectory.listItems("Dir3", true);
            Assertions.assertEquals(1, smbItems3a.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1/Dir2/Dir3", smbItems3a.get(0).getPath());
            Assertions.assertEquals(smbItems3a, smbItems3b);

            // Search for a few files
            List<SmbItem> smbItems4a = transferDirectory.listItems(item -> item.getName().contains("File1"), false);
            List<SmbItem> smbItems4b = transferDirectory.listItems("File1", false);
            Assertions.assertEquals(1, smbItems4b.size());
            Assertions.assertEquals("File1", smbItems4b.get(0).getName());
            Assertions.assertEquals(smbItems4a, smbItems4b);

            List<SmbItem> smbItems5a = transferDirectory.listItems(item -> item.getName().contains("File1"), true);
            List<SmbItem> smbItems5b = transferDirectory.listItems("File1", true);
            Assertions.assertEquals(2, smbItems5a.size());
            Assertions.assertEquals(transferDirectory.getPath() + "/Dir1/Dir2/File1", smbItems5a.get(0).getPath());
            Assertions.assertEquals(transferDirectory.getPath() + "/File1", smbItems5a.get(1).getPath());
            Assertions.assertEquals(smbItems5a, smbItems5b);
        }
    }
}
