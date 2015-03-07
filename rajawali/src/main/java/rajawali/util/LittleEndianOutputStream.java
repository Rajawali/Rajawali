/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

/**
 * Helper class for wrapping an {@link OutputStream} for little endian encoding.
 * 
 * Taken from http://www.javafaq.nu/java-example-code-1078.html
 * 
 */
public class LittleEndianOutputStream extends FilterOutputStream {

	protected int written;

	public LittleEndianOutputStream(OutputStream out) {
		super(out);
	}

	public void write(int b) throws IOException {
		out.write(b);
		written++;
	}

	public void write(byte[] data, int offset, int length)
			throws IOException {
		out.write(data, offset, length);
		written += length;
	}

	public void writeBoolean(boolean b) throws IOException {
		if (b)
			this.write(1);
		else
			this.write(0);
	}

	public void writeByte(int b) throws IOException {
		out.write(b);
		written++;
	}

	public void writeShort(int s) throws IOException {
		out.write(s & 0xFF);
		out.write((s >>> 8) & 0xFF);
		written += 2;
	}

	public void writeChar(int c) throws IOException {
		out.write(c & 0xFF);
		out.write((c >>> 8) & 0xFF);
		written += 2;
	}

	public void writeInt(int i) throws IOException {

		out.write(i & 0xFF);
		out.write((i >>> 8) & 0xFF);
		out.write((i >>> 16) & 0xFF);
		out.write((i >>> 24) & 0xFF);
		written += 4;

	}

	public void writeLong(long l) throws IOException {

		out.write((int) l & 0xFF);
		out.write((int) (l >>> 8) & 0xFF);
		out.write((int) (l >>> 16) & 0xFF);
		out.write((int) (l >>> 24) & 0xFF);
		out.write((int) (l >>> 32) & 0xFF);
		out.write((int) (l >>> 40) & 0xFF);
		out.write((int) (l >>> 48) & 0xFF);
		out.write((int) (l >>> 56) & 0xFF);
		written += 8;

	}

	public final void writeFloat(float f) throws IOException {
		this.writeInt(Float.floatToIntBits(f));
	}

	public final void writeDouble(double d) throws IOException {
		this.writeLong(Double.doubleToLongBits(d));
	}

	public void writeBytes(String s) throws IOException {
		int length = s.length();
		for (int i = 0; i < length; i++) {
			out.write((byte) s.charAt(i));
		}
		written += length;
	}

	public void writeChars(String s) throws IOException {
		int length = s.length();
		for (int i = 0; i < length; i++) {
			int c = s.charAt(i);
			out.write(c & 0xFF);
			out.write((c >>> 8) & 0xFF);
		}
		written += length * 2;
	}

	public void writeUTF(String s) throws IOException {

		int numchars = s.length();
		int numbytes = 0;

		for (int i = 0; i < numchars; i++) {
			int c = s.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F))
				numbytes++;
			else if (c > 0x07FF)
				numbytes += 3;
			else
				numbytes += 2;
		}

		if (numbytes > 65535)
			throw new UTFDataFormatException();

		out.write((numbytes >>> 8) & 0xFF);
		out.write(numbytes & 0xFF);
		for (int i = 0; i < numchars; i++) {
			int c = s.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				out.write(c);
			}
			else if (c > 0x07FF) {
				out.write(0xE0 | ((c >> 12) & 0x0F));
				out.write(0x80 | ((c >> 6) & 0x3F));
				out.write(0x80 | (c & 0x3F));
				written += 2;
			}
			else {
				out.write(0xC0 | ((c >> 6) & 0x1F));
				out.write(0x80 | (c & 0x3F));
				written += 1;
			}
		}

		written += numchars + 2;

	}

	public int size() {
		return this.written;
	}
}
