package ch.swaechter.smbjwrapper.core;

import ch.swaechter.smbjwrapper.SharedConnection;
import ch.swaechter.smbjwrapper.SharedDirectory;
import ch.swaechter.smbjwrapper.SharedFile;
import ch.swaechter.smbjwrapper.helpers.TestConnection;
import ch.swaechter.smbjwrapper.helpers.TestConnectionFactory;
import ch.swaechter.smbjwrapper.helpers.TestHelpers;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.Date;
import java.util.stream.Stream;

public class AbstractSharedItemTest {

    /**
     * Test the file attributes.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testIsHiddenAttribute(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Create a hidden Linux file
            SharedFile subFile = transferDirectory.createFileInCurrentDirectory(".notahiddenfile.txt");
            Assertions.assertTrue(subFile.isExisting());

            // Check if the file is hidden. This depends on the SMB server configuration. For more information read the Javadoc & referenced links there.
            subFile.isHidden();  // Test is a bit useless, so at least prevent crashes

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            Assertions.assertFalse(transferDirectory.isExisting());
        }
    }

    /**
     * Test the file attributes.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("getTestConnections")
    public void testDateAndSizeAttributes(TestConnection testConnection) throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Get the start date and create the entry point directory
            Date startDate = new Date();
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, TestHelpers.buildUniquePath());
            transferDirectory.createDirectory();

            // Create a temporary file
            File contentFile = new File("src/test/resources/Screenshot.png");
            File tempFile = File.createTempFile("smbjwrapper", ".tmp");
            Assertions.assertTrue(tempFile.exists());

            // Create a directory
            SharedDirectory subDirectory1 = transferDirectory.createDirectoryInCurrentDirectory("Directory");
            Assertions.assertTrue(subDirectory1.isExisting());

            // Create a file
            SharedFile subFile1 = subDirectory1.createFileInCurrentDirectory("File1.txt");
            Assertions.assertTrue(subFile1.isExisting());

            // Check the file size
            Assertions.assertEquals(0, subFile1.getFileSize());

            // Get the current date and check the creation time
            Date checkDate1 = new Date();
            TestHelpers.isDateBetweenDates(startDate, subDirectory1.getCreationTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subDirectory1.getLastAccessTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subDirectory1.getLastWriteTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subDirectory1.getChangeTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subFile1.getCreationTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subFile1.getLastAccessTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subFile1.getLastWriteTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subFile1.getChangeTime().toDate(), checkDate1);

            // Download a file
            InputStream inputStream1 = subFile1.getInputStream();
            OutputStream outputStream1 = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream1, outputStream1);
            inputStream1.close();
            outputStream1.close();

            // Check the download file size
            Assertions.assertEquals(0, tempFile.length());

            // Trigger another file creation
            SharedFile subFile2 = subDirectory1.createFileInCurrentDirectory("File2.txt");
            Assertions.assertTrue(subFile2.isExisting());

            // Check the last access time
            Date checkDate2 = new Date();
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getCreationTime().toDate(), checkDate2); // Creation time gets updated when an item is created inside the directory
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getLastAccessTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getLastWriteTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getChangeTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(startDate, subFile1.getCreationTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(checkDate1, subFile1.getLastAccessTime().toDate(), checkDate2); // Last access time won't get updated when file upload is empty
            TestHelpers.isDateBetweenDates(startDate, subFile1.getLastWriteTime().toDate(), checkDate1);
            TestHelpers.isDateBetweenDates(startDate, subFile1.getChangeTime().toDate(), checkDate1);

            // Upload a file
            InputStream inputStream2 = new FileInputStream(contentFile);
            OutputStream outputStream2 = subFile1.getOutputStream();
            IOUtils.copy(inputStream2, outputStream2);
            inputStream2.close();
            outputStream2.close();

            // Check the file size
            Assertions.assertEquals(37888, subFile1.getFileSize());

            // Check the last write time
            Date checkDate3 = new Date();
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getCreationTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getLastAccessTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getLastWriteTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getChangeTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subFile1.getCreationTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subFile1.getLastAccessTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate2, subFile1.getLastWriteTime().toDate(), checkDate3);
            TestHelpers.isDateBetweenDates(checkDate2, subFile1.getChangeTime().toDate(), checkDate3);

            // Download a file
            InputStream inputStream3 = subFile1.getInputStream();
            OutputStream outputStream3 = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream3, outputStream3);
            inputStream3.close();
            outputStream3.close();

            // Check the download file size
            Assertions.assertEquals(37888, tempFile.length());

            // Check the last access time
            Date checkDate4 = new Date();
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getCreationTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getLastAccessTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getLastWriteTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate1, subDirectory1.getChangeTime().toDate(), checkDate2);
            TestHelpers.isDateBetweenDates(checkDate2, subFile1.getCreationTime().toDate(), checkDate3);
            TestHelpers.isDateBetweenDates(checkDate3, subFile1.getLastAccessTime().toDate(), checkDate4);
            TestHelpers.isDateBetweenDates(checkDate2, subFile1.getLastWriteTime().toDate(), checkDate3);
            TestHelpers.isDateBetweenDates(checkDate2, subFile1.getChangeTime().toDate(), checkDate3);

            // Clean up
            transferDirectory.deleteDirectoryRecursively();
            Assertions.assertFalse(transferDirectory.isExisting());
        }
    }

    private static Stream getTestConnections() {
        return TestConnectionFactory.getTestConnections();
    }
}
