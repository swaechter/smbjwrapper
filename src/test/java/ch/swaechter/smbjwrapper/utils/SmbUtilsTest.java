package ch.swaechter.smbjwrapper.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This class is responsible for testing the util methods.
 *
 * @author Simon Wächter
 */
public class SmbUtilsTest {

    /**
     * Test the valid and invalid SMB item names.
     */
    @Test
    public void testIsValidSharedItemName() {
        Assertions.assertFalse(SmbUtils.isValidSmbItemName("."));
        Assertions.assertFalse(SmbUtils.isValidSmbItemName(".."));
        Assertions.assertFalse(SmbUtils.isValidSmbItemName("/"));
        Assertions.assertTrue(SmbUtils.isValidSmbItemName("Directory"));
        Assertions.assertTrue(SmbUtils.isValidSmbItemName("File.txt"));
    }
}
