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
     * Output stream of the file that will be decorated.
     */
    private final OutputStream outputStream;

    /**
     * Flag whether the content should be appended or the file should be overwritten/written at the beginning.
     */
    private final Boolean appendContent;

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
        this.appendContent = appendContent;
    }

    /**
     * Write a single value to the SMB file. The value is appended if appendContent is set to true.
     *
     * @param value Value to write or append
     * @throws IOException Exception in case of an IO/network problem
     */
    @Override
    public void write(int value) throws IOException {
        outputStream.write(value);
    }

    /**
     * Write a byte buffer to the SMB file. The values are appended if appendContent is set to true. Otherwise, they will overwrite the file.
     *
     * @param values Values to write or append
     * @throws IOException Exception in case of an IO/network problem
     */
    @Override
    public void write(byte[] values) throws IOException {
        outputStream.write(values, 0, values.length); // smbj ignores the offset when appending is enabled
    }

    /**
     * Write a byte buffer to the SMB file at a given offset with a given size. This method can only be used when appendContent is set to false.
     *
     * @param values Values to write
     * @param offset Offset in the file
     * @param length Length of the values to write
     * @throws IOException Exception in case of an IO/network problem
     */
    @Override
    public void write(byte[] values, int offset, int length) throws IOException {
        if (appendContent) {
            throw new IOException("The method SmbOutputStream.write(values, offset, length) can not be used when appendingContent is set to true.");
        }
        outputStream.write(values, offset, length);
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
