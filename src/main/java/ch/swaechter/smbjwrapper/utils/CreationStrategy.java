package ch.swaechter.smbjwrapper.utils;

/**
 * Decide what strategy to use when a path does not exist: Interpret it as file/directory or throw an exception.
 */
public enum CreationStrategy {
    FILE,
    DIRECTORY,
    EXCEPTION
}
