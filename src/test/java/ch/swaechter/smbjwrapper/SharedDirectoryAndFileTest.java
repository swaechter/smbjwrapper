package ch.swaechter.smbjwrapper;

import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Date;
import java.util.UUID;

/**
 * This class provides a combined test for the shared directory and file class.
 *
 * @author Simon Wächter
 */
public class SharedDirectoryAndFileTest {

    /**
     * Server hostname of the local test server.
     */
    private final String serverHostname = ServerUtils.getServerHostname();

    /**
     * Share name of the local test server.
     */
    private final String shareName = ServerUtils.getShareName();

    /**
     * Anonymous authentication for the local test server.
     */
    private final AuthenticationContext authenticationContext = ServerUtils.getAuthenticationContext();

    /**
     * Test the custom SMB configuration.
     *
     * @throws Exception Exception in case of a problem
     */
    @Test
    public void testConfig() throws Exception {
        SmbConfig smbConfig = SmbConfig.builder().withSoTimeout(3000).build();
        try (SharedConnection sharedConnection = new SharedConnection(serverHostname, shareName, authenticationContext, smbConfig)) {
            // Just check the root share directory
            SharedDirectory rootDirectory1 = new SharedDirectory(sharedConnection);
            Assertions.assertTrue(rootDirectory1.isExisting());
            Assertions.assertTrue(rootDirectory1.isDirectory());
        }
    }

    /**
     * Test all root directories and file including their attribute methods.
     *
     * @throws Exception Exception in case of a problem
     */
    @Test
    public void testRootPaths() throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(serverHostname, shareName, authenticationContext)) {
            // Create a root share directory
            SharedDirectory rootDirectory1 = new SharedDirectory(sharedConnection);
            Assertions.assertTrue(rootDirectory1.isExisting());
            Assertions.assertTrue(rootDirectory1.isDirectory());
            Assertions.assertFalse(rootDirectory1.isFile());
            Assertions.assertEquals(serverHostname, rootDirectory1.getServerName());
            Assertions.assertEquals(shareName, rootDirectory1.getShareName());
            Assertions.assertEquals("", rootDirectory1.getName());
            Assertions.assertEquals("", rootDirectory1.getPath());
            Assertions.assertEquals("\\\\" + serverHostname + "\\" + shareName, rootDirectory1.getSmbPath());
            Assertions.assertEquals(rootDirectory1, rootDirectory1.getParentPath());
            Assertions.assertEquals(rootDirectory1, rootDirectory1.getRootPath());
            Assertions.assertTrue(rootDirectory1.isRootPath());

            // Create a root share directory
            SharedDirectory rootDirectory2 = new SharedDirectory(sharedConnection, "");
            Assertions.assertTrue(rootDirectory2.isExisting());
            Assertions.assertTrue(rootDirectory2.isDirectory());
            Assertions.assertFalse(rootDirectory2.isFile());
            Assertions.assertEquals(serverHostname, rootDirectory2.getServerName());
            Assertions.assertEquals(shareName, rootDirectory2.getShareName());
            Assertions.assertEquals("", rootDirectory2.getName());
            Assertions.assertEquals("", rootDirectory2.getPath());
            Assertions.assertEquals("\\\\" + serverHostname + "\\" + shareName, rootDirectory2.getSmbPath());
            Assertions.assertEquals(rootDirectory2, rootDirectory2.getParentPath());
            Assertions.assertEquals(rootDirectory2, rootDirectory2.getRootPath());
            Assertions.assertTrue(rootDirectory2.isRootPath());

            // Create a root share file
            SharedFile rootFile1 = new SharedFile(sharedConnection, "File1.txt");
            Assertions.assertFalse(rootFile1.isExisting());
            Assertions.assertFalse(rootFile1.isDirectory());
            Assertions.assertFalse(rootFile1.isFile());
            Assertions.assertEquals(serverHostname, rootFile1.getServerName());
            Assertions.assertEquals(shareName, rootFile1.getShareName());
            Assertions.assertEquals("File1.txt", rootFile1.getName());
            Assertions.assertEquals("File1.txt", rootFile1.getPath());
            Assertions.assertEquals("\\\\" + serverHostname + "\\" + shareName + "\\File1.txt", rootFile1.getSmbPath());
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
     * @throws Exception Exception in case of a problem
     */
    @Test
    public void testRecreation() throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(serverHostname, shareName, authenticationContext)) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, buildUniquePath());

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
                SharedFile transferFile = new SharedFile(sharedConnection, "Text_" + buildUniquePath() + ".txt");
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
     * @throws Exception Exception in case of a problem
     */
    @Test
    public void testGetDirectoriesAndFiles() throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(serverHostname, shareName, authenticationContext)) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, buildUniquePath());
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
                Assertions.assertTrue(fileName.equals(subDirectory1.getName()));
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
     * Test the up- and download including a final delete.
     *
     * @throws Exception Exception in case of a problem
     */
    @Test
    public void testUploadAndDownload() throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(serverHostname, shareName, authenticationContext)) {
            // Create the entry point directory
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, buildUniquePath());
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

    @Test
    public void testAttributes() throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(serverHostname, shareName, authenticationContext)) {
            // Get the start date and create the entry point directory
            Date startDate = new Date();
            SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, buildUniquePath());
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
            Assertions.assertEquals(0, tempFile.length());

            // Trigger another file creation
            SharedFile subFile2 = subDirectory1.createFileInCurrentDirectory("File2.txt");
            Assertions.assertTrue(subFile2.isExisting());

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
            Assertions.assertEquals(37888, subFile1.getFileSize());

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
            Assertions.assertEquals(37888, tempFile.length());

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
        }
    }

    @Test
    public void testOverflowSessionPool() throws Exception {
        try (SharedConnection sharedConnection = new SharedConnection(serverHostname, shareName, authenticationContext)) {
            for (int i = 0; i < 1000; i++) {
                // Create the entry point directory
                SharedDirectory transferDirectory = new SharedDirectory(sharedConnection, buildUniquePath());
                transferDirectory.createDirectory();
                Assertions.assertTrue(transferDirectory.isExisting());

                // Delete it
                transferDirectory.deleteDirectoryRecursively();
                Assertions.assertFalse(transferDirectory.isExisting());
            }
        }
    }

    private void isDateBetweenDates(Date beforeDate, Date givenDate, Date afterDate) {
        Assertions.assertTrue(beforeDate.before(givenDate));
        Assertions.assertTrue(afterDate.after(givenDate));
    }

    private String buildUniquePath() {
        return "Transfer_" + UUID.randomUUID();
    }
}
