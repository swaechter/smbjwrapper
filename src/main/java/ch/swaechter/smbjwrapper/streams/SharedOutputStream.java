package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.OutputStream;

public class SharedOutputStream extends OutputStream {

    private final File file;

    private final OutputStream outputStream;

    public SharedOutputStream(File file) {
        this.file = file;
        this.outputStream = file.getOutputStream();
    }

    @Override
    public void write(int i) throws IOException {
        outputStream.write(i);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        file.close();
    }
}
