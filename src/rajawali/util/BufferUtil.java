package rajawali.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Source: http://www.google.co.uk/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&ved=0CF4QFjAA&url=http%3A%2F%2Fwww.badlogicgames.com%2Fwiki%2Findex.php%2FDirect_Bulk_FloatBuffer.put_is_slow&ei=ALwkUM3mE8Kk0QXK0YCgCg&usg=AFQjCNHs7ss9m_6FFrjwienCC2OzqzB7-Q
 * 
 * @author dennis.ippel
 *
 */
public class BufferUtil {
	static
	{
		System.loadLibrary("bufferutil");
	}
	
	native public static void copyJni(float[] src, Buffer dst, int numFloats, int offset);
	
	public static void copy(float[] src, Buffer dst, int numFloats, int offset) {
		copyJni(src, dst, numFloats, offset);
		dst.position(0);
		
		if(dst instanceof ByteBuffer)
			dst.limit(numFloats << 2);
		else if(dst instanceof FloatBuffer)
			dst.limit(numFloats);

	}
}
