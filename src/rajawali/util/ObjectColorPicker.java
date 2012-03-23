package rajawali.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import rajawali.BaseObject3D;
import rajawali.materials.ColorPickerMaterial;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.util.Log;

public class ObjectColorPicker {
	protected final int FLOAT_SIZE_BYTES = 4;
	
	private ArrayList<BaseObject3D> mObjectLookup;
	private RajawaliRenderer mRenderer;
	private int mFrameBufferHandle = -1;
	private IntBuffer mTexBuffer;
	private TextureInfo mTextureInfo;
	private boolean mIsInitialised = false;
	private ColorPickerMaterial mPickerMaterial;
	
	public ObjectColorPicker(RajawaliRenderer renderer) {
		mObjectLookup = new ArrayList<BaseObject3D>();
		mRenderer = renderer;
	}
	
	public void initialise() {
		int[] frameBuffers = new int[1];
		GLES20.glGenFramebuffers(1, frameBuffers, 0);
		mFrameBufferHandle = frameBuffers[0];
		int[] buf = new int[mRenderer.getViewportWidth() * mRenderer.getViewportHeight()];
		mTexBuffer = ByteBuffer.allocateDirect(buf.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
		mTextureInfo = mRenderer.getTextureManager().addTexture(mTexBuffer, mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
		mIsInitialised = true;
	}
	
	public void bindFrameBuffer() {
		if(!mIsInitialised)
			initialise();
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureInfo.getTextureId(), 0);
		
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE)
		{
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			Log.d(RajawaliRenderer.TAG, "Could not bind FrameBuffer for color picking.");
		}
	}
	
	public void registerObject(BaseObject3D object) {
		int color = getUniqueColor();
		mObjectLookup.add(object);
		object.setPickingColor(color);
		Log.d(RajawaliRenderer.TAG, String.valueOf(color));
	}
	
	private int getUniqueColor() {
		int color = 0;
		boolean isUnique = false;
		
		while(!isUnique) {
			color = Color.rgb((int)(Math.random() * 255f), (int)(Math.random() * 255f), (int)(Math.random() * 255f));
			isUnique = true;
			for(int i=0; i<mObjectLookup.size(); ++i)
				if(mObjectLookup.get(i).getPickingColor() == color)
					isUnique = false;
		}
		
		return color;
	}
	
	public BaseObject3D getObjectAt(float x, float y) {
		mRenderer.requestColorPickingTexture(new ColorPickerInfo(x, y, this));
		return null;
	}
	
	public void createColorPickingTexture(ColorPickerInfo pickerInfo) {
		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4);
		pixelBuffer.order(ByteOrder.nativeOrder());
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);
		GLES20.glReadPixels((int)pickerInfo.getX(), mRenderer.getViewportHeight() - (int)pickerInfo.getY(), 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		
		pixelBuffer.rewind();
		int r = pixelBuffer.get(0) & 0xff;
		int g = pixelBuffer.get(1) & 0xff;
		int b = pixelBuffer.get(2) & 0xff;
		
		for(int i=0; i<mObjectLookup.size(); i++) {
			int test = mObjectLookup.get(i).getPickingColor();
			if(Color.red(test) == r && Color.green(test) == g && Color.blue(test) == b)
				Log.d(RajawaliRenderer.TAG, "Found! " + mObjectLookup.get(i).getName());
		}
		
		int data[] = new int[1];
		pixelBuffer.asIntBuffer().get(data);
		pixelBuffer = null;
		
		Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.RGB_565);
		bitmap.setPixels(data, 0, 1, 0, 0, 1, 1);
		data = null;
		
		short sdata[] = new short[1];
		ShortBuffer sBuf = ShortBuffer.wrap(sdata);
		bitmap.copyPixelsToBuffer(sBuf);
			
		short v = sdata[0];
		//short test = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
		
		
		//Log.d(RajawaliRenderer.TAG, "Color: " + Color.red(test) + "|" + Color.green(test) + "|" + Color.blue(test));
	}
	
	public ColorPickerMaterial getMaterial() {
		if(mPickerMaterial == null)
			mPickerMaterial = new ColorPickerMaterial();
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
