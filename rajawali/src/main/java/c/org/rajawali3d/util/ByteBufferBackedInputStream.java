package c.org.rajawali3d.util;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="http://stackoverflow.com/a/6603018/1259881">Stack Overflow</a>
 */
public class ByteBufferBackedInputStream extends InputStream {

    final ByteBuffer buffer;

    public ByteBufferBackedInputStream(@NonNull ByteBuffer byteBuffer) {
        buffer = byteBuffer;
    }

    @Override
    public int read() throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        return buffer.get() & 0xFF;
    }

    @Override
    public int read(@NonNull byte[] bytes, int off, int len) throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buffer.remaining());
        buffer.get(bytes, off, len);
        return len;
    }
}
