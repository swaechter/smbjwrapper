package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.helpers.BaseTest;
import ch.swaechter.smbjwrapper.helpers.TestConnection;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class SmbItemTest extends BaseTest {

    /**
     * Test the file attributes.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testIsHiddenAttribute(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create a hidden Linux file
            SmbFile subFile = transferDirectory.createFileInCurrentDirectory(".notahiddenfile.txt");
            assertTrue(subFile.isExisting());

            // Check if the file is hidden. This depends on the SMB server configuration. For more information read the Javadoc & referenced links there.
            subFile.isHidden();  // Test is a bit useless, so at least prevent crashes

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            assertFalse(transferDirectory.isExisting());
        }
    }

    /**
     * Test the file attributes.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    //@ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testDateAndSizeAttributes(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Get the start date and create the entry point directory
            Date startDate = new Date();
            SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
            transferDirectory.createDirectory();

            // Create a temporary file
            File contentFile = new File("src/test/resources/Screenshot.png");
            File tempFile = File.createTempFile("smbjwrapper", ".tmp");
            assertTrue(tempFile.exists());

            // Create a directory
            SmbDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Directory");
            assertTrue(subDirectory1.isExisting());

            // Create a file
            SmbFile subFile1 = subDirectory1.createFileInCurrentDirectory("File1.txt");
            assertTrue(subFile1.isExisting());

            // Check the file size
            assertEquals(0, subFile1.getFileSize());

            // Get the current date and check the creation time
            Date checkDate1 = new Date();
            isDateBetweenDates(startDate, subDirectory1.getCreationTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subDirectory1.getLastAccessTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subDirectory1.getLastWriteTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subDirectory1.getChangeTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subFile1.getCreationTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subFile1.getLastAccessTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subFile1.getLastWriteTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subFile1.getChangeTime().toDate(), checkDate1);

            // Download a file
            InputStream inputStream1 = subFile1.getInputStream();
            OutputStream outputStream1 = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream1, outputStream1);
            inputStream1.close();
            outputStream1.close();

            // Check the download file size
            assertEquals(0, tempFile.length());

            // Trigger another file creation
            SmbFile subFile2 = subDirectory1.createFileInCurrentDirectory("File2.txt");
            assertTrue(subFile2.isExisting());

            // Check the last access time
            Date checkDate2 = new Date();
            isDateBetweenDates(checkDate1, subDirectory1.getCreationTime().toDate(), checkDate2); // Creation time gets updated when an item is created inside the directory
            isDateBetweenDates(checkDate1, subDirectory1.getLastAccessTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getLastWriteTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getChangeTime().toDate(), checkDate2);
            isDateBetweenDates(startDate, subFile1.getCreationTime().toDate(), checkDate1);
            isDateBetweenDates(checkDate1, subFile1.getLastAccessTime().toDate(), checkDate2); // Last access time won't get updated when file upload is empty
            isDateBetweenDates(startDate, subFile1.getLastWriteTime().toDate(), checkDate1);
            isDateBetweenDates(startDate, subFile1.getChangeTime().toDate(), checkDate1);

            // Upload a file
            InputStream inputStream2 = new FileInputStream(contentFile);
            OutputStream outputStream2 = subFile1.getOutputStream();
            IOUtils.copy(inputStream2, outputStream2);
            inputStream2.close();
            outputStream2.close();

            // Check the file size
            assertEquals(37888, subFile1.getFileSize());

            // Check the last write time
            Date checkDate3 = new Date();
            isDateBetweenDates(checkDate1, subDirectory1.getCreationTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getLastAccessTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getLastWriteTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getChangeTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subFile1.getCreationTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subFile1.getLastAccessTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate2, subFile1.getLastWriteTime().toDate(), checkDate3);
            isDateBetweenDates(checkDate2, subFile1.getChangeTime().toDate(), checkDate3);

            // Download a file
            InputStream inputStream3 = subFile1.getInputStream();
            OutputStream outputStream3 = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream3, outputStream3);
            inputStream3.close();
            outputStream3.close();

            // Check the download file size
            assertEquals(37888, tempFile.length());

            // Check the last access time
            Date checkDate4 = new Date();
            isDateBetweenDates(checkDate1, subDirectory1.getCreationTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getLastAccessTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getLastWriteTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate1, subDirectory1.getChangeTime().toDate(), checkDate2);
            isDateBetweenDates(checkDate2, subFile1.getCreationTime().toDate(), checkDate3);
            isDateBetweenDates(checkDate3, subFile1.getLastAccessTime().toDate(), checkDate4);
            isDateBetweenDates(checkDate2, subFile1.getLastWriteTime().toDate(), checkDate3);
            isDateBetweenDates(checkDate2, subFile1.getChangeTime().toDate(), checkDate3);

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            assertFalse(transferDirectory.isExisting());
        }
    }
}
