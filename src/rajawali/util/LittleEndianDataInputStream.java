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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Taken from http://www.peterfranza.com/2008/09/26/little-endian-input-stream/
 * @author dennis.ippel
 *
 */
public class LittleEndianDataInputStream extends InputStream implements DataInput {
 
	public LittleEndianDataInputStream(InputStream in) {
		this.in = in;
		this.d = new DataInputStream(in);
		w = new byte[8];
	}
 
	public int available() throws IOException {
		return d.available();
	}
 
 
	public final short readShort() throws IOException
	{
		d.readFully(w, 0, 2);
		return (short)(
				(w[1]&0xff) << 8 |
				(w[0]&0xff));
	}
	
	 public String readString(int length) throws IOException {
	        if (length == 0) {
	            return null;
	        }

	        byte[] b = new byte[length];
	        d.readFully(b);

	        return new String(b, "US-ASCII");
	    }


 
	/**
	 * Note, returns int even though it reads a short.
	 */
	 public final int readUnsignedShort() throws IOException
	 {
		 d.readFully(w, 0, 2);
		 return (
				 (w[1]&0xff) << 8 |
				 (w[0]&0xff));
	 }
 
	 /**
	  * like DataInputStream.readChar except little endian.
	  */
	 public final char readChar() throws IOException
	 {
		 d.readFully(w, 0, 2);
		 return (char) (
				 (w[1]&0xff) << 8 |
				 (w[0]&0xff));
	 }
 
	 /**
	  * like DataInputStream.readInt except little endian.
	  */
	 public final int readInt() throws IOException
	 {
		 d.readFully(w, 0, 4);
		 return
		 (w[3])      << 24 |
		 (w[2]&0xff) << 16 |
		 (w[1]&0xff) <<  8 |
		 (w[0]&0xff);
	 }
	 
	 /**
	  * like DataInputStream.readInt except little endian and for unsigned integers.
	  */
	 public final long readUnsignedInt() throws IOException
	 {
		 d.readFully(w, 0, 4);
		 return
		 (w[3])      << 24 |
		 (w[2]&0xff) << 16 |
		 (w[1]&0xff) <<  8 |
		 (w[0]&0xff) & 0xFFFFFFFFL;
	 }
 
	 /**
	  * like DataInputStream.readLong except little endian.
	  */
	 public final long readLong() throws IOException
	 {
		 d.readFully(w, 0, 8);
		 return
		 (long)(w[7])      << 56 | 
		 (long)(w[6]&0xff) << 48 |
		 (long)(w[5]&0xff) << 40 |
		 (long)(w[4]&0xff) << 32 |
		 (long)(w[3]&0xff) << 24 |
		 (long)(w[2]&0xff) << 16 |
		 (long)(w[1]&0xff) <<  8 |
		 (long)(w[0]&0xff);
	 }
 
	 public final float readFloat() throws IOException {
		 return Float.intBitsToFloat(readInt());
	 }
 
	 public final double readDouble() throws IOException {
		 return Double.longBitsToDouble(readLong());
	 }
 
	 public final int read(byte b[], int off, int len) throws IOException {
		 return in.read(b, off, len);
	 }
 
	 public final void readFully(byte b[]) throws IOException {
		 d.readFully(b, 0, b.length);
	 }
 
	 public final void readFully(byte b[], int off, int len) throws IOException {
		 d.readFully(b, off, len);
	 }
 
	 public final int skipBytes(int n) throws IOException {
		 return d.skipBytes(n);
	 }
 
	 public final boolean readBoolean() throws IOException {
		 return d.readBoolean();
	 }
 
	 public final byte readByte() throws IOException {
		 return d.readByte();
	 }
 
	 public int read() throws IOException {
		 return in.read();
	 }
 
	 public final int readUnsignedByte() throws IOException {
		 return d.readUnsignedByte();
	 }
 
	 @Deprecated
	 public final String readLine() throws IOException {
		 return d.readLine();
	 }
 
	 public final String readUTF() throws IOException {
		 return d.readUTF();
	 }
 
	 public final void close() throws IOException {
		 d.close();
	 }
 
	 private DataInputStream d; // to get at high level readFully methods of
	 // DataInputStream
	 private InputStream in; // to get at the low-level read methods of
	 // InputStream
	 private byte w[]; // work array for buffering input
}