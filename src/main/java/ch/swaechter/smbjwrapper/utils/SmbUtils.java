package ch.swaechter.smbjwrapper.utils;

/**
 * This class provides some common SMB util methods.
 *
 * @author Simon Wächter
 */
public class SmbUtils {

    /**
     * Check if the SMB item name is valid and does not contain some invalid characters
     *
     * @param itemName Item name to be checked
     * @return Status of the check
     */
    public static boolean isValidSmbItemName(String itemName) {
        switch (itemName) {
            case ".":
            case "..":
            case "/":
                return false;
            default:
                return true;
        }
    }
}
