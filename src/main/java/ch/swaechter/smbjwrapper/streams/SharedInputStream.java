package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.InputStream;

public class SharedInputStream extends InputStream {

    private final File file;

    private final InputStream inputStream;

    public SharedInputStream(File file) {
        this.file = file;
        this.inputStream = file.getInputStream();
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        file.close();
    }
}
