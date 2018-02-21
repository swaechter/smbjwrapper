package ch.swaechter.smbjwrapper.utils;

/**
 * This class provides some common share util methods.
 *
 * @author Simon Wächter
 */
public class ShareUtils {

    /**
     * Check if the shared item name is valid and does not contain a '.' or '..'.
     *
     * @param fileName File name to be checked
     * @return Status of the check
     */
    public static boolean isValidSharedItemName(String fileName) {
        return !fileName.equals(".") && !fileName.equals("..");
    }
}
