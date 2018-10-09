package ch.swaechter.smbjwrapper.helpers;

import com.hierynomus.smbj.auth.AuthenticationContext;

public class TestConnection {

    private final String hostName;

    private final String shareName;

    private final AuthenticationContext authenticationContext;

    public TestConnection(String hostName, String shareName, AuthenticationContext authenticationContext) {
        this.hostName = hostName;
        this.shareName = shareName;
        this.authenticationContext = authenticationContext;
    }

    public String getHostName() {
        return hostName;
    }

    public String getShareName() {
        return shareName;
    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }
}
