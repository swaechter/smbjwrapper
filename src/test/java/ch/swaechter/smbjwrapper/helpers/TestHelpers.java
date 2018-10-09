package ch.swaechter.smbjwrapper.helpers;

import org.junit.jupiter.api.Assertions;

import java.util.Date;
import java.util.UUID;

public class TestHelpers {

    public static String buildUniquePath() {
        return "Transfer_" + UUID.randomUUID();
    }

    public static void isDateBetweenDates(Date beforeDate, Date givenDate, Date afterDate) {
        Assertions.assertTrue(beforeDate.before(givenDate));
        Assertions.assertTrue(afterDate.after(givenDate));
    }
}
