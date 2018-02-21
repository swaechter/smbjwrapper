package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This class provides a test for the shared output stream.
 *
 * @author Simon Wächter
 */
public class SharedOutputStreamTest {

    /**
     * Test the output stream decorator including all required method invocations.
     *
     * @throws IOException Exception in case of a problem
     */
    @Test
    public void testSharedOutputStream() throws IOException {
        // Create a spy output stream object
        String testData = "Hello";
        ByteArrayOutputStream outputStream = Mockito.spy(new ByteArrayOutputStream(testData.getBytes().length));

        // Create a mocked file object
        File file = Mockito.mock(File.class);
        Mockito.when(file.getOutputStream()).thenReturn(outputStream);

        // Write the string
        SharedOutputStream sharedOutputStream = new SharedOutputStream(file);
        IOUtils.write(testData, sharedOutputStream, StandardCharsets.UTF_8);
        sharedOutputStream.close();

        // Test the data and method invocations
        Assertions.assertEquals(testData, new String(outputStream.toByteArray()));
        Mockito.verify(file, Mockito.times(1)).close();
        Mockito.verify(outputStream, Mockito.times(5)).write(Mockito.anyInt());
        Mockito.verify(outputStream, Mockito.times(1)).close();
    }
}
