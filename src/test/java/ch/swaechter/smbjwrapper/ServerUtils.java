package ch.swaechter.smbjwrapper;

import com.hierynomus.smbj.auth.AuthenticationContext;

/**
 * This class is responsible for providing the server hostname/share and the authentication. It has an own class so it's not changed and committed
 * in Git when someone is changing the unit tests.
 *
 * @author Simon Wächter
 */
public class ServerUtils {

    /**
     * Server name of the local test server.
     */
    private static final String serverHostname = "127.0.0.1";

    /**
     * Share name of the local test server.
     */
    private static final String shareName = "Share";

    /**
     * Anonymous authentication for the local test server.
     */
    //private static final AuthenticationContext authenticationContext = new AuthenticationContext("user", "password".toCharArray(), "");
    private static final AuthenticationContext authenticationContext = AuthenticationContext.anonymous();

    /**
     * Get the server hostname used for the testing.
     *
     * @return Hostname of the server
     */
    public static String getServerHostname() {
        return serverHostname;
    }

    /**
     * Get the server share used for testing.
     *
     * @return Share of the server
     */
    public static String getShareName() {
        return shareName;
    }

    /**
     * Get the authentication to the server used for testing.
     *
     * @return Authentication to the server
     */
    public static AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }
}
