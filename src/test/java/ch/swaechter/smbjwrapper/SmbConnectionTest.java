package ch.swaechter.smbjwrapper;

import ch.swaechter.smbjwrapper.helpers.BaseTest;
import ch.swaechter.smbjwrapper.helpers.TestConnection;
import com.hierynomus.smbj.SmbConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class SmbConnectionTest extends BaseTest {

    /**
     * Test the custom SMB configuration.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testConfig(TestConnection testConnection) throws Exception {
        // Test a directly initialized connection
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            Assertions.assertTrue(smbConnection.isConnectionAlive());

            SmbDirectory rootDirectory1 = new SmbDirectory(smbConnection);
            Assertions.assertTrue(rootDirectory1.isExisting());
            Assertions.assertTrue(rootDirectory1.isDirectory());
        }

        // Test a delayed initialized connection via call to get the disk share
        SmbConfig smbConfig = SmbConfig.builder().withSoTimeout(3000).build();
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext(), smbConfig, true)) {
            Assertions.assertFalse(smbConnection.isConnectionAlive());

            SmbDirectory rootDirectory1 = new SmbDirectory(smbConnection);
            Assertions.assertTrue(rootDirectory1.isExisting());
            Assertions.assertTrue(rootDirectory1.isDirectory());

            Assertions.assertTrue(smbConnection.isConnectionAlive());
        }

        // Test a delayed initialized connection via call to ensure the connection
        smbConfig = SmbConfig.builder().withSoTimeout(3000).build();
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext(), smbConfig, true)) {
            Assertions.assertFalse(smbConnection.isConnectionAlive());
            smbConnection.ensureConnectionIsAlive();
            Assertions.assertTrue(smbConnection.isConnectionAlive());

            SmbDirectory rootDirectory1 = new SmbDirectory(smbConnection);
            Assertions.assertTrue(rootDirectory1.isExisting());
            Assertions.assertTrue(rootDirectory1.isDirectory());

            Assertions.assertTrue(smbConnection.isConnectionAlive());
        }

        // Test a delayed initialized connection via call to ensure the connection + test the timout reconnect
        smbConfig = SmbConfig.builder().withSoTimeout(3000).build();
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext(), smbConfig, true)) {
            Assertions.assertFalse(smbConnection.isConnectionAlive());
            smbConnection.ensureConnectionIsAlive();
            Assertions.assertTrue(smbConnection.isConnectionAlive());

            Thread.sleep(5 * 1000);

            Assertions.assertFalse(smbConnection.isConnectionAlive());
            smbConnection.ensureConnectionIsAlive();
            Assertions.assertTrue(smbConnection.isConnectionAlive());

            SmbDirectory rootDirectory1 = new SmbDirectory(smbConnection);
            Assertions.assertTrue(rootDirectory1.isExisting());
            Assertions.assertTrue(rootDirectory1.isDirectory());

            Assertions.assertTrue(smbConnection.isConnectionAlive());
        }
    }

    /**
     * Test the decorated connection to prevent a session pool overflow.
     *
     * @param testConnection Parameterized test connection data
     * @throws Exception Exception in case of a problem
     */
    @ParameterizedTest
    @MethodSource("ch.swaechter.smbjwrapper.helpers.BaseTest#getTestConnections")
    public void testOverflowSessionPool(TestConnection testConnection) throws Exception {
        try (SmbConnection smbConnection = new SmbConnection(testConnection.getHostName(), testConnection.getShareName(), testConnection.getAuthenticationContext())) {
            for (int i = 0; i < 1000; i++) {
                // Create the entry point directory
                SmbDirectory transferDirectory = new SmbDirectory(smbConnection, buildUniquePath());
                transferDirectory.createDirectory();
                Assertions.assertTrue(transferDirectory.isExisting());

                // Delete it
                transferDirectory.deleteDirectoryRecursively();
                Assertions.assertFalse(transferDirectory.isExisting());
            }
        }
    }
}
