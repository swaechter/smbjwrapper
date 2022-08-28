package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * This class provides a test for the SMB input stream.
 *
 * @author Simon Wächter
 */
public class SmbInputStreamTest {

    /**
     * Test the input stream decorator including all required method invocations.
     *
     * @throws IOException Exception in case of a problem
     */
    @Test
    public void testSmbInputStream() throws IOException {
        // Create a spy input stream object
        String testData = "Hello";
        ByteArrayInputStream inputStream = spy(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8)));

        // Create a mocked file object
        File file = mock(File.class);
        when(file.getInputStream()).thenReturn(inputStream);

        // Read the string
        byte[] testBuffer = new byte[testData.getBytes().length];
        SmbInputStream smbInputStream = new SmbInputStream(file);
        IOUtils.read(smbInputStream, testBuffer);
        smbInputStream.close();

        // Test the data and method invocations
        assertEquals(testData, new String(testBuffer));
        verify(file, times(1)).close();
        verify(inputStream, times(5)).read();
        verify(inputStream, times(1)).close();
    }
}
