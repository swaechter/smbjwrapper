package ch.swaechter.smbjwrapper.helpers;

import com.hierynomus.smbj.auth.AuthenticationContext;
import org.junit.jupiter.api.Assertions;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class BaseTest {

    public static Stream<TestConnection> getTestConnections() {
        return Stream.of(
            new TestConnection("127.0.0.1", "Share", new AuthenticationContext("secasign", "123456aA".toCharArray(), ""))
        );
    }

    public static String buildUniquePath() {
        return "Transfer_" + UUID.randomUUID();
    }

    public static void isDateBetweenDates(Date beforeDate, Date givenDate, Date afterDate) {
        Assertions.assertTrue(beforeDate.before(givenDate));
        Assertions.assertTrue(afterDate.after(givenDate));
    }
}
