package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a decorated output stream that respects the reference counting close mechanism of the file.
 *
 * @author Simon Wächter
 */
public class SmbOutputStream extends OutputStream {

    /**
     * File that provides the output stream.
     */
    private final File file;

    /**
     * output stream of the file that will be decorated.
     */
    private final OutputStream outputStream;

    /**
     * Create a new decorated output stream that respects the reference counting close mechanism of the file. It's possible to append or
     * overwrite existing content.
     *
     * @param file          File that will provide the output stream
     * @param appendContent Append the content to the current file or overwrite it
     */
    public SmbOutputStream(File file, boolean appendContent) {
        this.file = file;
        this.outputStream = file.getOutputStream(appendContent);
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
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        outputStream.flush();
        outputStream.close();
        file.close();
    }
}
