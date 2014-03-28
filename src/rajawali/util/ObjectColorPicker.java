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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import rajawali.Object3D;
import rajawali.materials.Material;
import rajawali.materials.MaterialManager;
import rajawali.materials.textures.RenderTargetTexture;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;

public class ObjectColorPicker extends AFrameTask implements IObjectPicker {

	private final ArrayList<Object3D> mObjectLookup = new ArrayList<Object3D>();
	private final RajawaliRenderer mRenderer;

	private int mColorIndex = 0;
	private int mFrameBufferHandle = -1;
	private int mDepthBufferHandle = -1;
	private RenderTargetTexture mTexture;
	private boolean mIsInitialized = false;
	private Material mPickerMaterial;
	private OnObjectPickedListener mObjectPickedListener;

	public ObjectColorPicker(RajawaliRenderer renderer) {
		mRenderer = renderer;
		mRenderer.queueInitializeTask(this);
	}

	public void initialize() {
		final int size = Math.max(mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
		mTexture = new RenderTargetTexture("colorPickerTexture");
		mTexture.setWidth(size);
		mTexture.setHeight(size);
		// -- safe to use taskAdd because initalize is called in a thread safe manner
		mRenderer.getTextureManager().taskAdd(mTexture);
		genBuffers();

		mPickerMaterial = new Material();
		MaterialManager.getInstance().addMaterial(mPickerMaterial);
		mIsInitialized = true;
	}

	public void reload() {
		if (!mIsInitialized)
			return;

		genBuffers();
	}

	public void genBuffers() {
		final int[] frameBuffers = new int[1];
		GLES20.glGenFramebuffers(1, frameBuffers, 0);
		mFrameBufferHandle = frameBuffers[0];

		final int[] depthBuffers = new int[1];
		GLES20.glGenRenderbuffers(1, depthBuffers, 0);
		mDepthBufferHandle = depthBuffers[0];

		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
				mTexture.getWidth(), mTexture.getHeight());
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
	}

	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener) {
		mObjectPickedListener = objectPickedListener;
	}

	public void bindFrameBuffer() throws ObjectColorPickerException {
		if (!mIsInitialized)
		{
			mRenderer.queueInitializeTask(this);
			return;
		}
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
				GLES20.GL_TEXTURE_2D, mTexture.getTextureId(), 0);
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			RajLog.d("Could not bind FrameBuffer for color picking." + mTexture.getTextureId());
		}
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
				GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
	}

	public void unbindFrameBuffer() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
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

	public static void createColorPickingTexture(ColorPickerInfo pickerInfo) {
		final ObjectColorPicker picker = pickerInfo.getPicker();
		final ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4);
		pixelBuffer.order(ByteOrder.nativeOrder());

		GLES20.glReadPixels(pickerInfo.getX(), picker.mRenderer.getViewportHeight()
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

	@Override
	public TYPE getFrameTaskType() {
		return TYPE.COLOR_PICKER;
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
