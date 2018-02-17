package ch.swaechter.smbjwrapper.utils;

public class ShareUtils {

    public static boolean isValidSharedItemName(String fileName) {
        return !fileName.equals(".") && !fileName.equals("..");
    }
}
