package ch.swaechter.smbjwrapper;

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
import java.util.stream.Stream;

public class SharedFileTest {

    /**
     * Test the up- and download including a final delete.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testUploadAndDownload(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Create the subdirectory and subfiles
            SharedDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory1");

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
            transferDirectory.deleteDirectoryRecursively();
            Assertions.assertFalse(transferDirectory.isExisting());
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

            // Create a first file
            SharedFile sharedFile1 = transferDirectory.createFileInCurrentDirectory("File1");
            Assertions.assertTrue(sharedFile1.isExisting());

            // Create a second file
            SharedFile sharedFile2 = transferDirectory.createFileInCurrentDirectory("File2");
            Assertions.assertTrue(sharedFile2.isExisting());

            // Create a first directory
            SharedDirectory sharedDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Directory1");
            Assertions.assertTrue(sharedFile1.isExisting());

            // Do a regular rename
            sharedFile1.renameTo("File1New", false);
            Assertions.assertEquals("File1New", sharedFile1.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/File1New", sharedFile1.getPath());

            // Do a rename and trigger an exception
            try {
                sharedFile1.renameTo("File2", false);
                Assertions.fail("Rename without replace flag should fail");
            } catch (Exception exception) {
                Assertions.assertEquals("File1New", sharedFile1.getName());
                Assertions.assertEquals(transferDirectory.getPath() + "/File1New", sharedFile1.getPath());
            }

            // Do a replace rename
            sharedFile1.renameTo("File2", true);
            Assertions.assertEquals("File2", sharedFile1.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/File2", sharedFile1.getPath());

            // Do a rename to a directory and trigger an exception
            try {
                sharedFile1.renameTo("Directory1", true);
                Assertions.fail("Rename a file to a directory should fail");
            } catch (Exception eception) {
                Assertions.assertTrue(sharedFile1.isExisting());
                Assertions.assertEquals("File2", sharedFile1.getName());
                Assertions.assertTrue(sharedDirectory1.isDirectory());
            }
        }
    }

    private static Stream getTestConnections() {
        return TestConnectionFactory.getTestConnections();
    }
}
