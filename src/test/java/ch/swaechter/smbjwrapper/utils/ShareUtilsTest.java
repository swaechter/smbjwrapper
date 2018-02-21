package ch.swaechter.smbjwrapper.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This class is responsible for testing the util methods.
 *
 * @author Simon Wächter
 */
public class ShareUtilsTest {

    /**
     * Test the valid and invalid shared item name.
     */
    @Test
    public void testIsValidSharedItemName() {
        Assertions.assertFalse(ShareUtils.isValidSharedItemName("."));
        Assertions.assertFalse(ShareUtils.isValidSharedItemName(".."));
        Assertions.assertTrue(ShareUtils.isValidSharedItemName("Directory"));
        Assertions.assertTrue(ShareUtils.isValidSharedItemName("File.txt"));
    }
}
