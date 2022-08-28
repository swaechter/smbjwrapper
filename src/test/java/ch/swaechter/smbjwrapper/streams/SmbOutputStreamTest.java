package ch.swaechter.smbjwrapper.streams;

import com.hierynomus.smbj.share.File;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * This class provides a test for the SMB output stream.
 *
 * @author Simon Wächter
 */
public class SmbOutputStreamTest {

    /**
     * Test the output stream decorator including all required method invocations.
     *
     * @throws IOException Exception in case of a problem
     */
    @Test
    public void testSmbOutputStream() throws IOException {
        // Create a spy output stream object
        String testData = "Hello";
        ByteArrayOutputStream outputStream = spy(new ByteArrayOutputStream(testData.getBytes().length));

        // Create a mocked file object
        File file = mock(File.class);
        when(file.getOutputStream(false)).thenReturn(outputStream);

        // Write the string
        SmbOutputStream smbOutputStream = new SmbOutputStream(file, false);
        IOUtils.write(testData, smbOutputStream, StandardCharsets.UTF_8);
        smbOutputStream.close();

        // Test the data and method invocations
        assertEquals(testData, new String(outputStream.toByteArray()));
        verify(file, times(1)).close();
        verify(outputStream, times(1)).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream, times(1)).flush();
        verify(outputStream, times(1)).close();
    }
}
