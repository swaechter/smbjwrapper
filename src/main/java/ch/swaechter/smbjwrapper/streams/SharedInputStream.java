package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a decorated input stream that respects the reference counting close mechanism of the file.
 *
 * @author Simon Wächter
 */
public class SharedInputStream extends InputStream {

    /**
     * File that provides the input stream.
     */
    private final File file;

    /**
     * Input stream of the file that will be decorated.
     */
    private final InputStream inputStream;

    /**
     * Create a new decorated input stream that respects the reference couting close mechanism of the file.
     *
     * @param file File that will provide the input stream
     */
    public SharedInputStream(File file) {
        this.file = file;
        this.inputStream = file.getInputStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
        file.close();
    }
}
