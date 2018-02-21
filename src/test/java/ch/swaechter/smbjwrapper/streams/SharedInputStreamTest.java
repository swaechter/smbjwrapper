package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This class provides a test for the shared input stream.
 *
 * @author Simon Wächter
 */
public class SharedInputStreamTest {

    /**
     * Test the input stream decorator including all required method invocations.
     *
     * @throws IOException Exception in case of a problem
     */
    @Test
    public void testSharedInputStream() throws IOException {
        // Create a spy input stream object
        String testData = "Hello";
        ByteArrayInputStream inputStream = Mockito.spy(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8)));

        // Create a mocked file object
        File file = Mockito.mock(File.class);
        Mockito.when(file.getInputStream()).thenReturn(inputStream);

        // Read the string
        byte[] testBuffer = new byte[testData.getBytes().length];
        SharedInputStream sharedInputStream = new SharedInputStream(file);
        IOUtils.read(sharedInputStream, testBuffer);
        sharedInputStream.close();

        // Test the data and method invocations
        Assertions.assertEquals(testData, new String(testBuffer));
        Mockito.verify(file, Mockito.times(1)).close();
        Mockito.verify(inputStream, Mockito.times(5)).read();
        Mockito.verify(inputStream, Mockito.times(1)).close();
    }
}
