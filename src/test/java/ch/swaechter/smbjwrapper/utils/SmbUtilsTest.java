package ch.swaechter.smbjwrapper.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertFalse(SmbUtils.isValidSmbItemName("."));
        assertFalse(SmbUtils.isValidSmbItemName(".."));
        assertFalse(SmbUtils.isValidSmbItemName("/"));
        assertTrue(SmbUtils.isValidSmbItemName("Directory"));
        assertTrue(SmbUtils.isValidSmbItemName("File.txt"));
    }
}
