package ch.swaechter.smbjwrapper.helpers;

import com.hierynomus.smbj.auth.AuthenticationContext;

import java.util.stream.Stream;

public class TestConnectionFactory {

    public static Stream getTestConnections() {
        return Stream.of(
            new TestConnection("127.0.0.1", "SecureShare", new AuthenticationContext("documentmanager", "123456aA".toCharArray(), ""))
        );
    }
}
