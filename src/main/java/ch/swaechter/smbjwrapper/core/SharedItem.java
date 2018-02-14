package ch.swaechter.smbjwrapper.core;

public interface SharedItem {

    boolean isExisting();

    boolean isDirectory();

    boolean isFile();

    String getName();

    String getPath();

    SharedItem getParentPath();

    SharedItem getRootPath();

    boolean isRootPath();
}
