package org.rajawali3d.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Taken from http://www.peterfranza.com/2008/09/26/little-endian-input-stream/
 * 
 * @author dennis.ippel
 * 
 */
public class LittleEndianDataInputStream extends InputStream implements DataInput {

	protected final DataInputStream d;
	protected final InputStream in;
	protected final byte w[];

	protected long mPosition;

	public LittleEndianDataInputStream(InputStream in) {
		this.in = in;
		this.d = new DataInputStream(in);
		w = new byte[8];
	}

	public int available() throws IOException {
		return d.available();
	}
	
	public long getPosition() {
		return mPosition;
	}

	public final short readShort() throws IOException {
		mPosition += 2;
		d.readFully(w, 0, 2);
		return (short) ((w[1] & 0xff) << 8 | (w[0] & 0xff));
	}

	public String readString(int length) throws IOException {
		if (length == 0)
			return null;

		mPosition += length;

		byte[] b = new byte[length];
		d.readFully(b);

		return new String(b, "US-ASCII");
	}

	/**
	 * Note, returns int even though it reads a short.
	 */
	public final int readUnsignedShort() throws IOException {
		mPosition += 2;
		d.readFully(w, 0, 2);
		return ((w[1] & 0xff) << 8 | (w[0] & 0xff));
	}

	/**
	 * like DataInputStream.readChar except little endian.
	 */
	public final char readChar() throws IOException {
		mPosition += 2;
		d.readFully(w, 0, 2);
		return (char) ((w[1] & 0xff) << 8 | (w[0] & 0xff));
	}

	/**
	 * like DataInputStream.readInt except little endian.
	 */
	public final int readInt() throws IOException {
		mPosition += 4;
		d.readFully(w, 0, 4);
		return (w[3]) << 24 |
				(w[2] & 0xff) << 16 |
				(w[1] & 0xff) << 8 |
				(w[0] & 0xff);
	}

	/**
	 * like DataInputStream.readInt except little endian and for unsigned integers.
	 */
	public final long readUnsignedInt() throws IOException {
		mPosition += 4;
		d.readFully(w, 0, 4);
		return ((long)
				(w[3]) << 24 |
				(w[2] & 0xff) << 16 |
				(w[1] & 0xff) << 8 | (w[0] & 0xff)) & 0X00000000FFFFFFFFL;
	}

	/**
	 * like DataInputStream.readLong except little endian.
	 */
	public final long readLong() throws IOException {
		mPosition += 8;
		d.readFully(w, 0, 8);
		return (long) (w[7]) << 56 |
				(long) (w[6] & 0xff) << 48 |
				(long) (w[5] & 0xff) << 40 |
				(long) (w[4] & 0xff) << 32 |
				(long) (w[3] & 0xff) << 24 |
				(long) (w[2] & 0xff) << 16 |
				(long) (w[1] & 0xff) << 8 |
				(long) (w[0] & 0xff);
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public final int read(byte b[], int off, int len) throws IOException {
		mPosition += len;
		return in.read(b, off, len);
	}

	public final void readFully(byte b[]) throws IOException {
		mPosition += b.length;
		d.readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		mPosition += len;
		d.readFully(b, off, len);
	}

	public final long skip(int n) throws IOException {
		mPosition += n;
		return d.skip(n);
	}

	public final long skip(long n) throws IOException {
		mPosition += n;
		return d.skip(n);
	}

	public final int skipBytes(int n) throws IOException {
		mPosition += n;
		return d.skipBytes(n);
	}

	public final boolean readBoolean() throws IOException {
		mPosition += 1;
		return d.readBoolean();
	}

	public final byte readByte() throws IOException {
		mPosition += 1;
		return d.readByte();
	}

	public int read() throws IOException {
		mPosition += 1;
		return in.read();
	}

	public final int readUnsignedByte() throws IOException {
		mPosition += 1;
		return d.readUnsignedByte();
	}

	@Deprecated
	public final String readLine() throws IOException {
		final String line = d.readLine();
		mPosition += line.getBytes().length;
		return line;
	}

	public final String readUTF() throws IOException {
		final String line = d.readUTF();
		mPosition += line.getBytes().length;
		return line;
	}

	public final void close() throws IOException {
		d.close();
	}

}
