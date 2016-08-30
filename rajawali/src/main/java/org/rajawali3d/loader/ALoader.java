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
package org.rajawali3d.loader;

import android.content.res.Resources;
import android.os.Environment;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.LittleEndianDataInputStream;
import org.rajawali3d.util.RajLog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public abstract class ALoader implements ILoader {

	protected Resources mResources;
	protected int mResourceId;
	protected String mFileOnSDCard;
	protected File mFile;
	protected int mTag;

	public ALoader(File file) {
		this(file.getAbsolutePath());
		mFile = file;
	}

	public ALoader(String fileOnSDCard)
	{
		mResources = null;
		mResourceId = 0;
		mFileOnSDCard = fileOnSDCard;
	}

	public ALoader(Renderer renderer, String fileOnSDCard)
	{
		this(renderer.getContext().getResources(), 0);
		mFileOnSDCard = fileOnSDCard;
	}

	public ALoader(Renderer renderer, int resourceId)
	{
		this(renderer.getContext().getResources(), resourceId);
	}

	public ALoader(Resources resources, int resourceId)
	{
		mResources = resources;
		mResourceId = resourceId;
	}

	public ALoader(Renderer renderer, File file) {
		this(renderer.getContext().getResources(), 0);
		mFile = file;
	}

	public ILoader parse() throws ParsingException {
		if (mFile == null && mFileOnSDCard != null)
			mFile = new File(Environment.getExternalStorageDirectory(), mFileOnSDCard);

		if (mFile != null && RajLog.isDebugEnabled())
			RajLog.d("Parsing: " + mFile.getAbsolutePath());
		return this;
	}

	public int getTag() {
		return mTag;
	}

	public void setTag(int tag) {
		mTag = tag;
	}

	/**
	 * Open a BufferedReader for the current resource or file with a buffer size of 8192 bytes.
	 *
	 * @return
	 * @throws FileNotFoundException
	 */
	protected BufferedReader getBufferedReader() throws FileNotFoundException {
		return getBufferedReader(8192);
	}

	/**
	 * Open a BufferedReader for the current resource or file with a given buffer size.
	 *
	 * @param size
	 *            Size of buffer in number of bytes
	 * @return
	 * @throws FileNotFoundException
	 */
	protected BufferedReader getBufferedReader(int size) throws FileNotFoundException {
		BufferedReader buffer = null;

		if (mFile == null) {
			buffer = new BufferedReader(new InputStreamReader(mResources.openRawResource(mResourceId)), size);
		} else {
			buffer = new BufferedReader(new FileReader(mFile), size);
		}

		return buffer;
	}

	/**
	 * Open a BufferedReader for the current resource or file with a buffer size of 8192 bytes.
	 *
	 * @return
	 * @throws FileNotFoundException
	 */
	protected BufferedInputStream getBufferedInputStream() throws FileNotFoundException {
		return getBufferedInputStream(8192);
	}

	/**
	 * Open a BufferedReader for the current resource or file using the given buffer size.
	 *
	 * @param size
	 * @return
	 * @throws FileNotFoundException
	 */
	protected BufferedInputStream getBufferedInputStream(int size) throws FileNotFoundException {
		BufferedInputStream bis;

		if (mFile == null) {
			bis = new BufferedInputStream(mResources.openRawResource(mResourceId), size);
		} else {
			bis = new BufferedInputStream(new FileInputStream(mFile), size);
		}

		return bis;
	}

	/**
	 * Open a DataInputStream for the current resource or file using Little Endian format with a buffer size of 8192
	 * bytes.
	 *
	 * @return
	 * @throws FileNotFoundException
	 */
	protected LittleEndianDataInputStream getLittleEndianInputStream() throws FileNotFoundException {
		return getLittleEndianInputStream(8192);
	}

	/**
	 * Open a DataInputStream for the current resource or file using Little Endian format with a given buffer size.
	 *
	 * @param size
	 *            Size of buffer in number of bytes
	 * @return
	 * @throws FileNotFoundException
	 */
	protected LittleEndianDataInputStream getLittleEndianInputStream(int size) throws FileNotFoundException {
		return new LittleEndianDataInputStream(getBufferedInputStream(size));
	}

	protected String readString(InputStream stream) throws IOException {
		String result = new String();
		byte inByte;
		while ((inByte = (byte) stream.read()) != 0)
			result += (char) inByte;
		return result;
	}

	protected int readInt(InputStream stream) throws IOException {
		return stream.read() | (stream.read() << 8) | (stream.read() << 16)
				| (stream.read() << 24);
	}

	protected int readShort(InputStream stream) throws IOException {
		return (stream.read() | (stream.read() << 8));
	}

	protected float readFloat(InputStream stream) throws IOException {
		return Float.intBitsToFloat(readInt(stream));
	}

	protected String getOnlyFileName(String fileName) {
		String fName = new String(fileName);
		int indexOf = fName.lastIndexOf("\\");
		if (indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		indexOf = fName.lastIndexOf("/");
		if (indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		return fName.toLowerCase(Locale.ENGLISH).replaceAll("\\s", "_");
	}

	protected String getFileNameWithoutExtension(String fileName) {
		String fName = fileName.substring(0, fileName.lastIndexOf("."));
		int indexOf = fName.lastIndexOf("\\");
		if (indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		indexOf = fName.lastIndexOf("/");
		if (indexOf > -1)
			fName = fName.substring(indexOf + 1, fName.length());
		return fName.toLowerCase(Locale.ENGLISH).replaceAll("\\s", "_");
	}

}
