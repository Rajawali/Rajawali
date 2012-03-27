package rajawali.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import rajawali.BaseObject3D;
import rajawali.materials.ColorPickerMaterial;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

public class ObjectColorPicker {
	protected final int FLOAT_SIZE_BYTES = 4;
	
	private ArrayList<BaseObject3D> mObjectLookup;
	private RajawaliRenderer mRenderer;
	private int mFrameBufferHandle = -1;
	private TextureInfo mTextureInfo;
	private boolean mIsInitialised = false;
	private ColorPickerMaterial mPickerMaterial;
	private OnObjectPickedListener mObjectPickedListener;
	
	public ObjectColorPicker(RajawaliRenderer renderer) {
		mObjectLookup = new ArrayList<BaseObject3D>();
		mRenderer = renderer;
	}
	
	public void initialise() {
		int[] frameBuffers = new int[1];
		GLES20.glGenFramebuffers(1, frameBuffers, 0);
		mFrameBufferHandle = frameBuffers[0];
		mTextureInfo = mRenderer.getTextureManager().addTexture(null, mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
		mIsInitialised = true;
	}
	
	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener) {
		mObjectPickedListener = objectPickedListener;
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
	}
	
	private int getUniqueColor() {
		int color = 0;
		boolean isUnique = false;
		
		while(!isUnique) {
			isUnique = true;
			color = Color.rgb((int)(Math.random() * 255f), (int)(Math.random() * 255f), (int)(Math.random() * 255f));
			if(color == 0xff000000) isUnique = false; // background color			
			for(int i=0; i<mObjectLookup.size(); ++i)
				if(mObjectLookup.get(i).getPickingColor() == color)
					isUnique = false;
		}
		
		return color;
	}
	
	public void getObjectAt(float x, float y) {
		mRenderer.requestColorPickingTexture(new ColorPickerInfo(x, y, this));
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
			{
				mObjectPickedListener.onObjectPicked(mObjectLookup.get(i));
				break;
			}
		}
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
