package ch.swaechter.smbjwrapper.utils;

/**
 * This class provides some common share util methods.
 *
 * @author Simon Wächter
 */
public class ShareUtils {

    /**
     * Check if the shared item name is valid and does not contain some invalid characters
     *
     * @param fileName File name to be checked
     * @return Status of the check
     */
    public static boolean isValidSharedItemName(String fileName) {
        switch (fileName) {
            case ".":
            case "..":
            case "/":
                return false;
            default:
                return true;
        }
    }
}
