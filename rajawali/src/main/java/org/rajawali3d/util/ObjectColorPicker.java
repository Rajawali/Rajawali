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

import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.MaterialManager;
import org.rajawali3d.materials.textures.ATexture.FilterType;
import org.rajawali3d.materials.textures.ATexture.WrapType;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.RenderTarget;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class ObjectColorPicker implements IObjectPicker {

	private final ArrayList<Object3D> mObjectLookup = new ArrayList<>();
	private final RajawaliRenderer mRenderer;

	private int mColorIndex = 0;
	private RenderTarget mRenderTarget;
	private Material mPickerMaterial;
	private OnObjectPickedListener mObjectPickedListener;

	public ObjectColorPicker(RajawaliRenderer renderer) {
		mRenderer = renderer;
		mRenderer.initializeColorPicker(this);
	}

	public void initialize() {
		final int size = Math.max(mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
		
		mRenderTarget = new RenderTarget("colorPickerTarget", size, size, 
				0, 0, false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888, 
				FilterType.LINEAR, WrapType.CLAMP);
		mRenderer.addRenderTarget(mRenderTarget);

		mPickerMaterial = new Material();
		MaterialManager.getInstance().addMaterial(mPickerMaterial);
	}

	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener) {
		mObjectPickedListener = objectPickedListener;
	}

	public void registerObject(Object3D object) {
		if (!mObjectLookup.contains(object)) {
			mObjectLookup.add(object);
			object.setPickingColor(mColorIndex);
			++mColorIndex;
		}
	}

	public void unregisterObject(Object3D object) {
		if (mObjectLookup.contains(object)) {
			mObjectLookup.remove(object);
		}
	}

	public void getObjectAt(float x, float y) {
		mRenderer.getCurrentScene().requestColorPickingTexture(new ColorPickerInfo(x, y, this));
	}
	
	public RenderTarget getRenderTarget() {
		return mRenderTarget;
	}

	public static void createColorPickingTexture(ColorPickerInfo pickerInfo) {
		final ObjectColorPicker picker = pickerInfo.getPicker();
		final ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4);
		pixelBuffer.order(ByteOrder.nativeOrder());

		GLES20.glReadPixels(pickerInfo.getX(), picker.mRenderer.getDefaultViewportHeight()
				- pickerInfo.getY(), 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
				pixelBuffer);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		pixelBuffer.rewind();

		final int r = pixelBuffer.get(0) & 0xff;
		final int g = pixelBuffer.get(1) & 0xff;
		final int b = pixelBuffer.get(2) & 0xff;
		final int a = pixelBuffer.get(3) & 0xff;
		final int index = Color.argb(a, r, g, b);

		if (0 <= index && index < picker.mObjectLookup.size() && picker.mObjectPickedListener != null)
			picker.mObjectPickedListener.onObjectPicked(picker.mObjectLookup.get(index));
	}

	public Material getMaterial() {
		return mPickerMaterial;
	}

	public class ColorPickerInfo {

		private int mX;
		private int mY;
		private ObjectColorPicker mPicker;
		private ByteBuffer mColorPickerBuffer;

		public ColorPickerInfo(float x, float y, ObjectColorPicker picker) {
			mX = (int) x;
			mY = (int) y;
			mPicker = picker;
		}

		public ObjectColorPicker getPicker() {
			return mPicker;
		}

		public int getX() {
			return mX;
		}

		public int getY() {
			return mY;
		}

		public void setColorPickerBuffer(ByteBuffer buffer) {
			mColorPickerBuffer = buffer;
		}

		public ByteBuffer getColorPickerBuffer() {
			return mColorPickerBuffer;
		}
	}

	public static final class ObjectColorPickerException extends Exception {

		private static final long serialVersionUID = 3732833696361901287L;

		public ObjectColorPickerException() {
			super();
		}

		public ObjectColorPickerException(final String msg) {
			super(msg);
		}

		public ObjectColorPickerException(final Throwable throwable) {
			super(throwable);
		}

		public ObjectColorPickerException(final String msg, final Throwable throwable) {
			super(msg, throwable);
		}

	}
}
