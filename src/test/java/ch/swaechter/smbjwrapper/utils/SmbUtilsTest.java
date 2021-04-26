package ch.swaechter.smbjwrapper.utils;

import ch.swaechter.smbjwrapper.SmbConnection;
import ch.swaechter.smbjwrapper.SmbDirectory;
import ch.swaechter.smbjwrapper.SmbFile;
import ch.swaechter.smbjwrapper.SmbItem;
import ch.swaechter.smbjwrapper.helpers.BaseTest;
import ch.swaechter.smbjwrapper.helpers.TestConnection;
import com.hierynomus.smbj.auth.AuthenticationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;

/**
 * This class is responsible for testing the util methods.
 *
 * @author Simon Wächter
 */
public class SmbUtilsTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testUncCreation(TestConnection testConnection) throws Exception {
        // Create a regular connection for creating the required directories/files
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            // Define some reused variables
            String uniquePath = buildUniquePath();
            String baseUnc = "\\\\" + testConnection.getHostName() + "\\" + testConnection.getShareName();
            String testUnc = baseUnc + "\\" + uniquePath;
            AuthenticationContext authenticationContext = testConnection.getAuthenticationContext();

            // Detect a non existing sub-directory
            SmbItem smbItem = SmbUtils.buildSmbItemFromUncPath(authenticationContext, CreationStrategy.DIRECTORY, testUnc + "\\Directory");
            Assertions.assertTrue(smbItem instanceof SmbDirectory);
            smbItem.getSmbConnection().close();

            // Detect a non existing file in a non existing sub directory
            smbItem = SmbUtils.buildSmbItemFromUncPath(authenticationContext, CreationStrategy.FILE, testUnc + "\\Directory\\File");
            Assertions.assertTrue(smbItem instanceof SmbFile);
            smbItem.getSmbConnection().close();

            // Fail the detection
            for (String path : Arrays.asList("\\Directory", "\\Directory\\Fail")) {
                try {
                    SmbUtils.buildSmbItemFromUncPath(authenticationContext, CreationStrategy.EXCEPTION, testUnc + path);
                    Assertions.fail("UNC build should fail");
                } catch (IOException exception) {
                    Assertions.assertEquals("The given UNC path does not exist and thus it's not possible to decide if the path is a file/directory", exception.getMessage());
                }
            }

            // Create the sub directory and the file
            SmbDirectory smbTestDirectory = new SmbDirectory(smbConnection, uniquePath);
            smbTestDirectory.ensureExists();

            SmbDirectory smbDirectory = new SmbDirectory(smbConnection, uniquePath + "/Directory");
            smbDirectory.ensureExists();

            SmbFile smbFile = new SmbFile(smbConnection, uniquePath + "/Directory/File");
            smbFile.createFile();

            // Auto detect the root directory
            smbItem = SmbUtils.buildSmbItemFromUncPath(authenticationContext, CreationStrategy.EXCEPTION, testUnc);
            try (SmbConnection temporarySmbConnection = smbItem.getSmbConnection()) {
                Assertions.assertTrue(smbItem instanceof SmbDirectory);
            }

            // Auto detect a sub directory
            smbItem = SmbUtils.buildSmbItemFromUncPath(authenticationContext, CreationStrategy.EXCEPTION, testUnc + "\\Directory");
            try (SmbConnection temporarySmbConnection = smbItem.getSmbConnection()) {
                Assertions.assertTrue(smbItem instanceof SmbDirectory);
            }

            // Auto detect a file in a sub directory
            smbItem = SmbUtils.buildSmbItemFromUncPath(authenticationContext, CreationStrategy.EXCEPTION, testUnc + "\\Directory\\File");
            try (SmbConnection temporarySmbConnection = smbItem.getSmbConnection()) {
                Assertions.assertTrue(smbItem instanceof SmbFile);
            }
        }
    }
}
