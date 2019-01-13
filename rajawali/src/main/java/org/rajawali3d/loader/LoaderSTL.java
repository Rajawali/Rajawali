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
import android.content.res.Resources.NotFoundException;

import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.LittleEndianDataInputStream;
import org.rajawali3d.util.RajLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * STL Parser written using the ASCII format as describe on Wikipedia.
 * <p>
 *
 * @author Ian Thomas (toxicbakery@gmail.com)
 *
 * @see <a href="http://en.wikipedia.org/wiki/STL_(file_format)">http://en.wikipedia.org/wiki/STL_(file_format)</a>
 */
public class LoaderSTL extends AMeshLoader {

	// FIXME More testing, Nexus 7 specifically has some issues with certain models. Nexus 4 works fine with same
	// 'problem' models.

	// TODO Add a feature for ASCII to Binary conversion.

	public enum StlType {
		UNKNOWN,
		ASCII,
		BINARY
	}

	public LoaderSTL(Renderer renderer, File file) {
		super(renderer, file);
	}

    public LoaderSTL(Resources resources, TextureManager textureManager, int resourceId) {
        super(resources, textureManager, resourceId);
    }

    public LoaderSTL(Renderer renderer, String fileOnSDCard) {
        super(renderer, fileOnSDCard);
    }

	@Override
	public AMeshLoader parse() throws ParsingException {
		return parse(StlType.UNKNOWN);
	}

	public AMeshLoader parse(StlType type) throws ParsingException {
		super.parse();
		try {

			// Open the file
			BufferedReader buffer = null;
			LittleEndianDataInputStream dis = null;

			switch (type) {
			case UNKNOWN:
				buffer = getBufferedReader();
				// Determine if ASCII or Binary
				boolean isASCII = isASCII(buffer);

				// Determine ASCII or Binary format
				if (isASCII) {
					readASCII(buffer);
				} else {

					// Switch to a LittleEndianDataInputStream (all values in binary are stored in little endian format)
					buffer.close();
					dis = getLittleEndianInputStream();
					readBinary(dis);
				}
				break;
			case ASCII:
				buffer = getBufferedReader();
				readASCII(buffer);
				break;
			case BINARY:
				dis = getLittleEndianInputStream();
				readBinary(dis);
				break;
			}

			// Cleanup
			if (buffer != null)
				buffer.close();
			if (dis != null)
				dis.close();

		} catch (FileNotFoundException e) {
			RajLog.e("[" + getClass().getCanonicalName() + "] Could not find file.");
			throw new ParsingException("File not found.", e);
		} catch (NumberFormatException e) {
			RajLog.e(e.getMessage());
			throw new ParsingException("Unexpected value.", e);
		} catch (IOException e) {
			RajLog.e(e.getMessage());
			throw new ParsingException("File reading failed.", e);
		} catch (Exception e) {
			RajLog.e(e.getMessage());
			throw new ParsingException("Unexpected exception occured.", e);
		}

		return this;
	}

	/**
	 * Read stream as ASCII. While this works well, ASCII is painfully slow on mobile devices due to tight memory
	 * constraints and lower processing power compared to desktops. It is advisable to use the binary parser whenever
	 * possible.
	 *
	 * @param buffer
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void readASCII(final BufferedReader buffer) throws NumberFormatException, IOException {
		RajLog.i("StlPaser: Reading ASCII");

		final List<Float> vertices = new ArrayList<Float>();
		final List<Float> normals = new ArrayList<Float>();
		final float[] tempNorms = new float[3];

		int nextOffset, prevOffset, i, insert;
		String line;

		// Skip the first line
		line = buffer.readLine();

		// Read the facet
		while ((line = buffer.readLine()) != null) {

			// Only read lines containing facet normal and vertex. No reason to read others

			if (line.contains("facet normal ")) {

				nextOffset = line.lastIndexOf(" ");
				tempNorms[2] = Float.parseFloat(line.substring(nextOffset + 1));

				prevOffset = nextOffset;
				nextOffset = line.lastIndexOf(" ", prevOffset - 1);
				tempNorms[1] = Float.parseFloat(line.substring(nextOffset + 1, prevOffset));

				prevOffset = nextOffset;
				nextOffset = line.lastIndexOf(" ", prevOffset - 1);
				tempNorms[0] = Float.parseFloat(line.substring(nextOffset + 1, prevOffset));

				// Need to duplicate the normal for each vertex of the triangle
				for (i = 0; i < 3; i++) {
					normals.add(tempNorms[0]);
					normals.add(tempNorms[1]);
					normals.add(tempNorms[2]);
				}

			} else if (line.contains("vertex ")) {

				insert = vertices.size();

				nextOffset = line.lastIndexOf(" ");
				vertices.add(Float.parseFloat(line.substring(nextOffset + 1)));

				prevOffset = nextOffset;
				nextOffset = line.lastIndexOf(" ", prevOffset - 1);
				vertices.add(insert, Float.parseFloat(line.substring(nextOffset + 1, prevOffset)));

				prevOffset = nextOffset;
				nextOffset = line.lastIndexOf(" ", prevOffset - 1);
				vertices.add(insert, Float.parseFloat(line.substring(nextOffset + 1, prevOffset)));
			}
		}

		float[] verticesArr = new float[vertices.size()];
		float[] normalsArr = new float[normals.size()];
		for (i = 0; i < verticesArr.length; i++) {
			verticesArr[i] = vertices.get(i);
			normalsArr[i] = normals.get(i);
		}

		// Cleanup
		vertices.clear();
		normals.clear();

		int[] indicesArr = new int[verticesArr.length / 3];
		for (i = 0; i < indicesArr.length; i++)
			indicesArr[i] = i;

		mRootObject.setData(verticesArr, normalsArr, null, null, indicesArr, false);
	}

	/**
	 * Read stream as binary STL. This is significantly faster than ASCII parsing. Additionally binary files are much
	 * more compressed allowing smaller file sizes for larger models compared to ASCII.
	 *
	 * @param dis
	 * @throws IOException
	 */
	private void readBinary(final LittleEndianDataInputStream dis) throws IOException {
		RajLog.i("StlPaser: Reading Binary");

		// Skip the header
		dis.skip(80);

		// Read the number of facets (have to convert the uint to a long
		int facetCount = dis.readInt();

		float[] verticesArr = new float[facetCount * 9];
		float[] normalsArr = new float[facetCount * 9];
		int[] indicesArr = new int[facetCount * 3];
		float[] tempNorms = new float[3];
		int vertPos = 0, normPos = 0;

		for (int i = 0; i < indicesArr.length; i++)
			indicesArr[i] = i;

		// Read all the facets
		while (dis.available() > 0) {

			// Read normals
			for (int j = 0; j < 3; j++) {
				tempNorms[j] = dis.readFloat();
				if (Float.isNaN(tempNorms[j]) || Float.isInfinite(tempNorms[j])) {
					RajLog.w("STL contains bad normals of NaN or Infinite!");
					tempNorms[0] = 0;
					tempNorms[1] = 0;
					tempNorms[2] = 0;
					break;
				}
			}

			for (int j = 0; j < 3; j++) {
				normalsArr[normPos++] = tempNorms[0];
				normalsArr[normPos++] = tempNorms[1];
				normalsArr[normPos++] = tempNorms[2];
			}

			// Read vertices
			for (int j = 0; j < 9; j++)
				verticesArr[vertPos++] = dis.readFloat();

			dis.skip(2);
		}

		mRootObject.setData(verticesArr, normalsArr, null, null, indicesArr, false);
	}

	/**
	 * Determine if a given file appears to be in ASCII format.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws StlParseException
	 */
	public static final boolean isASCII(File file) throws IOException, StlParseException {
		if (file.exists())
			throw new StlParseException("Passed file does not exist.");

		if (!file.isFile())
			throw new StlParseException("This is not a file.");

		final BufferedReader buffer = new BufferedReader(new FileReader(file));
		boolean isASCII = isASCII(buffer);
		buffer.close();

		return isASCII;
	}

	/**
	 * Determine if a given resource appears to be in ASCII format.
	 *
	 * @param res
	 * @param resId
	 * @return
	 * @throws IOException
	 */
	public static final boolean isASCII(Resources res, int resId) throws IOException, NotFoundException {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(res.openRawResource(resId)));
		boolean isASCII = isASCII(buffer);
		buffer.close();

		return isASCII;
	}

	/**
	 * Determine if a given BufferedReader appears to be in ASCII format.
	 *
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public static final boolean isASCII(BufferedReader buffer) throws IOException {
		final char[] readAhead = new char[300];
		buffer.mark(readAhead.length);
		buffer.read(readAhead, 0, readAhead.length);
		buffer.reset();
		final String readAheadString = new String(readAhead);

		// If the following text is present, then this is likely an ascii file
        return readAheadString.contains("facet normal") && readAheadString.contains("outer loop");

		// Likely a binary file
    }

	public static final class StlParseException extends ParsingException {

		private static final long serialVersionUID = -5098120548044169618L;

		public StlParseException(String msg) {
			super(msg);
		}
	}

}
