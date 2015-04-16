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
package org.rajawali3d.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.opengl.GLES20;
import android.os.Environment;

/**
 * Grabs the pixels from the buffer and saves it to a file on the SD card.
 * Usage example (saves every frame to a separate .png file):
 * 
 * <pre><code>
 * public void onDrawFrame(GL10 glUnused) {
 * 		super.onDrawFrame(glUnused);
 * 		Screengrab.saveAsImage(0, 0, mDefaultViewportWidth, mDefaultViewportHeight, "/frame_" + mFrameCount + ".png", CompressFormat.PNG);
 * }
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class ScreenGrab {
	/**
	 * Saves the pixels from the buffer as a .png file on the SD card.
	 * 
	 * @param width		the image width
	 * @param height	the image height
	 * @param path		the file path
	 * @throws FileNotFoundException 
	 */
	public static void saveAsImage(int width, int height, String path) throws FileNotFoundException
	{
		saveAsImage(0, 0, width, height, path, CompressFormat.PNG, 100);
	}
		
	/**
	 * Saves the pixels from the buffer on the SD card.
	 * 
	 * @param x					the image origin x
	 * @param y					the image origin y
	 * @param width				the image width
	 * @param height			the image height
	 * @param path				the file path
	 * @param compressFormat	the compression format {@link CompressFormat}
	 * @param quality			the compression quality
	 * @throws FileNotFoundException 
	 */
	public static void saveAsImage(int x, int y, int width, int height, String path, CompressFormat compressFormat, int quality) throws FileNotFoundException {
		Bitmap bmp = getPixelsFromBuffer(x, y, width, height);
		try {
			File file = new File(path);

			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(compressFormat, quality, fos);
			try {
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			throw(e);
		}
	}

	/**
	 * Grabs the pixels from the buffer
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getPixelsFromBuffer(int x, int y, int width, int height) {
		int b[] = new int[width * (y + height)];
		int bt[] = new int[width * height];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

		for (int i = 0, k = 0; i < height; i++, k++) {
			for (int j = 0; j < width; j++) {
				int pix = b[i * width + j];
				int pb = (pix >> 16) & 0xff;
				int pr = (pix << 16) & 0x00ff0000;
				int pix1 = (pix & 0xff00ff00) | pr | pb;
				bt[(height - k - 1) * width + j] = pix1;
			}
		}

		return Bitmap.createBitmap(bt, width, height, Bitmap.Config.ARGB_8888);
	}
}
