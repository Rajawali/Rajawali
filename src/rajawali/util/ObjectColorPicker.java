package rajawali.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import rajawali.BaseObject3D;
import rajawali.materials.ColorPickerMaterial;
import rajawali.materials.TextureInfo;
import rajawali.materials.TextureManager.TextureType;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;

public class ObjectColorPicker implements IObjectPicker {

	protected final int FLOAT_SIZE_BYTES = 4;

	private ArrayList<BaseObject3D> mObjectLookup;
	private int mColorIndex = 0;
	private RajawaliRenderer mRenderer;
	private int mFrameBufferHandle = -1;
	private int mDepthBufferHandle = -1;
	private TextureInfo mTextureInfo;
	private boolean mIsInitialized = false;
	private ColorPickerMaterial mPickerMaterial;
	private OnObjectPickedListener mObjectPickedListener;

	public ObjectColorPicker(RajawaliRenderer renderer) {
		mObjectLookup = new ArrayList<BaseObject3D>();
		mRenderer = renderer;
	}

	public void initialize() {
		int size = Math.max(mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
		mTextureInfo = mRenderer.getTextureManager().addTexture(null, size, size,
				TextureType.FRAME_BUFFER);
		genBuffers();
		mPickerMaterial = new ColorPickerMaterial();
		mIsInitialized = true;
	}

	public void reload() {
		if (!mIsInitialized)
			return;
		genBuffers();
		mPickerMaterial.reload();
	}

	public void genBuffers() {
		int[] frameBuffers = new int[1];
		GLES20.glGenFramebuffers(1, frameBuffers, 0);
		mFrameBufferHandle = frameBuffers[0];

		int[] depthBuffers = new int[1];
		GLES20.glGenRenderbuffers(1, depthBuffers, 0);
		mDepthBufferHandle = depthBuffers[0];

		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
				mTextureInfo.getWidth(), mTextureInfo.getHeight());
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
	}

	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener) {
		mObjectPickedListener = objectPickedListener;
	}

	public void bindFrameBuffer() {
		if (!mIsInitialized)
			initialize();
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
				GLES20.GL_TEXTURE_2D, mTextureInfo.getTextureId(), 0);
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			RajLog.d("Could not bind FrameBuffer for color picking.");
		}
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
				GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
	}

	public void unbindFrameBuffer() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
	}

	public void registerObject(BaseObject3D object) {
		if (!mObjectLookup.contains(object)) {
			mObjectLookup.add(object);
			object.setPickingColor(mColorIndex);
			mColorIndex++;
		}
	}

	public void getObjectAt(float x, float y) {
		mRenderer.requestColorPickingTexture(new ColorPickerInfo(x, y, this));
	}

	public void createColorPickingTexture(ColorPickerInfo pickerInfo) {
		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4);
		pixelBuffer.order(ByteOrder.nativeOrder());
		GLES20.glReadPixels((int) pickerInfo.getX(), mRenderer.getViewportHeight()
				- (int) pickerInfo.getY(), 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
				pixelBuffer);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		pixelBuffer.rewind();

		int r = pixelBuffer.get(0) & 0xff;
		int g = pixelBuffer.get(1) & 0xff;
		int b = pixelBuffer.get(2) & 0xff;
		int a = pixelBuffer.get(3) & 0xff;
		int index = Color.argb(a, r, g, b);

		if (0 <= index && index < mObjectLookup.size() && mObjectPickedListener != null) {
			mObjectPickedListener.onObjectPicked(mObjectLookup.get(index));
		}
	}

	public ColorPickerMaterial getMaterial() {
		return mPickerMaterial;
	}

	public class ColorPickerInfo {

		private float mX;
		private float mY;
		private ObjectColorPicker mPicker;
		private ByteBuffer mColorPickerBuffer;

		public ColorPickerInfo(float x, float y, ObjectColorPicker picker) {
			mX = x;
			mY = y;
			mPicker = picker;
		}

		public ObjectColorPicker getPicker() {
			return mPicker;
		}

		public float getX() {
			return mX;
		}

		public float getY() {
			return mY;
		}

		public void setColorPickerBuffer(ByteBuffer buffer) {
			mColorPickerBuffer = buffer;
		}

		public ByteBuffer getColorPickerBuffer() {
			return mColorPickerBuffer;
		}
	}
}
