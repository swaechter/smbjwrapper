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
import java.nio.charset.StandardCharsets;

public class SmbFileTest extends BaseTest {

    /**
     * Test the up- and download including a final delete.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testUploadAndDownload(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create the subdirectory and subfiles
            SmbDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Subdirectory1");

            // Upload a file
            InputStream inputStream = new FileInputStream(new File("src/test/resources/Screenshot.png"));
            Assertions.assertNotNull(inputStream);

            SmbFile subFile2_1 = subDirectory1.createFileInCurrentDirectory("Subfile1.txt");
            OutputStream outputStream = subFile2_1.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();

            // Transfer/download a file
            SmbFile subFile2_2 = subDirectory1.createFileInCurrentDirectory("Subfile2.txt");
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
     * Test the appended upload.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testAppendedUploadAndDownload(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Define some test data
            String testData1 = "Hello dear world. How are you today?";
            String testData2 = "Sad to hear that climate change is killing you!";
            String testData3 = "I hope we'll be able to fix that for you!";

            // Create a test file
            SmbFile testFile = transferDirectory.createFileInCurrentDirectory("TestFile");
            Assertions.assertTrue(testFile.isExisting());

            // Do a first non-appended upload
            OutputStream outputStream1 = testFile.getOutputStream();
            outputStream1.write(testData1.getBytes(StandardCharsets.UTF_8));
            outputStream1.close();

            InputStream inputStream1 = testFile.getInputStream();
            Assertions.assertEquals(testData1, IOUtils.toString(inputStream1, StandardCharsets.UTF_8));

            // Do a second appended upload
            OutputStream outputStream2 = testFile.getOutputStream(true);
            outputStream2.write(testData2.getBytes(StandardCharsets.UTF_8));
            outputStream2.close();

            InputStream inputStream2 = testFile.getInputStream();
            Assertions.assertEquals(testData1 + testData2, IOUtils.toString(inputStream2, StandardCharsets.UTF_8));

            // Do a third appended upload
            OutputStream outputStream3 = testFile.getOutputStream(true);
            outputStream3.write(testData3.getBytes(StandardCharsets.UTF_8));
            outputStream3.close();

            InputStream inputStream3 = testFile.getInputStream();
            Assertions.assertEquals(testData1 + testData2 + testData3, IOUtils.toString(inputStream3, StandardCharsets.UTF_8));
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
            // Create a first file in the root directory
            SmbDirectory shareRootDirectory = new SmbDirectory(smbConnection);
            SmbFile smbFile1 = shareRootDirectory.createFileInCurrentDirectory("File1");
            Assertions.assertTrue(smbFile1.isExisting());

            // Rename it
            SmbFile smbFile1New = smbFile1.renameTo("File1New", false);
            Assertions.assertFalse(smbFile1.isExisting());
            Assertions.assertTrue(smbFile1New.isExisting());

            // Cleanup
            smbFile1New.deleteFile();
            Assertions.assertFalse(smbFile1New.isExisting());

            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create a second file
            SmbFile smbFile2 = transferDirectory.createFileInCurrentDirectory("File2");
            Assertions.assertTrue(smbFile2.isExisting());

            // Create a third file
            SmbFile smbFile3 = transferDirectory.createFileInCurrentDirectory("File3");
            Assertions.assertTrue(smbFile3.isExisting());

            // Create a first directory
            SmbDirectory smbDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Directory1");
            Assertions.assertTrue(smbFile2.isExisting());

            // Do a regular rename
            SmbFile smbFile2New = smbFile2.renameTo("File2New", false);
            Assertions.assertEquals("File2New", smbFile2New.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/File2New", smbFile2New.getPath());

            // Do a rename and trigger an exception
            try {
                smbFile2New.renameTo("File3", false);
                Assertions.fail("Rename without replace flag should fail");
            } catch (Exception exception) {
                Assertions.assertEquals("File2New", smbFile2New.getName());
                Assertions.assertEquals(transferDirectory.getPath() + "/File2New", smbFile2New.getPath());
            }

            // Do a replace rename
            smbFile3 = smbFile2New.renameTo("File3", true);
            Assertions.assertEquals("File3", smbFile3.getName());
            Assertions.assertEquals(transferDirectory.getPath() + "/File3", smbFile3.getPath());

            // Do a rename to a directory and trigger an exception
            try {
                smbFile3.renameTo("Directory1", true);
                Assertions.fail("Rename a file to a directory should fail");
            } catch (Exception eception) {
                Assertions.assertTrue(smbFile3.isExisting());
                Assertions.assertEquals("File3", smbFile3.getName());
                Assertions.assertTrue(smbDirectory1.isDirectory());
            }
        }
    }
}
