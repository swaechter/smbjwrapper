package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a decorated output stream that respects the reference counting close mechanism of the file.
 *
 * @author Simon Wächter
 */
public class SharedOutputStream extends OutputStream {

    /**
     * File that provides the output stream.
     */
    private final File file;

    /**
     * output stream of the file that will be decorated.
     */
    private final OutputStream outputStream;

    /**
     * Create a new decorated output stream that respects the reference couting close mechanism of the file.
     *
     * @param file File that will provide the output stream
     */
    public SharedOutputStream(File file) {
        this.file = file;
        this.outputStream = file.getOutputStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int i) throws IOException {
        outputStream.write(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        outputStream.close();
        file.close();
    }
}
