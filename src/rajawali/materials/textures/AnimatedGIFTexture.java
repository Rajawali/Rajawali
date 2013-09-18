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
package rajawali.materials.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;


/**
 * Creates a texture from an animated GIF.
 * 
 * @author dennis.ippel
 *
 */
public class AnimatedGIFTexture extends ASingleTexture {
	private Canvas mCanvas;
	private Movie mMovie;
	private Bitmap mGIFBitmap;
	private int mResourceId;
	private int mWidth;
	private int mHeight;
	private int mTextureSize;
	private long mStartTime;
	private boolean mLoadNewGIF;
	private boolean mLoop;
	private int mLoopCount;
	private boolean mAnimationRunning;
	public AnimatedGIFTexture(String name, int resourceId) {
		this(name, resourceId, 512);
	}
	
	/**
	 * Creates an animated GIF texture
	 * 
	 * @param resourceId	The animated GIF resource
	 * @param textureSize 	The power of two size
	 */
	public AnimatedGIFTexture(String name, int resourceId, int textureSize) {
		super(TextureType.DIFFUSE, name);
		mTextureSize = textureSize;
		mResourceId = resourceId;
		mLoop = true;
		loadGIF();
	}
	
	public AnimatedGIFTexture(AnimatedGIFTexture other) {
		super(other);
		setFrom(other);
	}
	
	@Override
	public AnimatedGIFTexture clone() {
		return new AnimatedGIFTexture(this);
	}
	
	private void loadGIF() {
		Context context = TextureManager.getInstance().getContext();
		mMovie = Movie.decodeStream(context.getResources().openRawResource(mResourceId));
		mWidth = mMovie.width();
		mHeight = mMovie.height();
		
		mGIFBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		mCanvas = new Canvas(mGIFBitmap);
		mMovie.draw(mCanvas, 0, 0);
		mBitmap = Bitmap.createScaledBitmap(mGIFBitmap, mTextureSize, mTextureSize, false);
	}
	
	/**
	 * Copies every property from another AnimatedGIFTexture object
	 * 
	 * @param other
	 *            another AnimatedGIFTexture object to copy from
	 */
	public void setFrom(AnimatedGIFTexture other)
	{
		super.setFrom(other);
		mBitmap = other.getBitmap();
		mCanvas = other.getCanvas();
		mMovie = other.getMovie();
		mWidth = other.getWidth();
		mHeight = other.getHeight();
		mTextureSize = other.getTextureSize();
	}
	
	public void rewind()
	{
		mStartTime = SystemClock.uptimeMillis();
		mAnimationRunning = true;
	}
	
	@Override
	void replace() throws TextureException
	{
		if(mLoadNewGIF)
		{
			loadGIF();
			mLoadNewGIF = false;
		}
		super.replace();
	}

	public void update() throws TextureException
	{
		if(mMovie == null || mMovie.duration() == 0) return;
		if (mAnimationRunning) {
			long now = SystemClock.uptimeMillis();
			int relTime = (int) ((now - mStartTime) % mMovie.duration());

			mLoopCount = ((int) ((now - mStartTime) / mMovie.duration()));
			if (!mLoop && mLoopCount >= 1) {
				relTime = mMovie.duration();
				mAnimationRunning = false;
			}
			mMovie.setTime(relTime);
			mMovie.draw(mCanvas, 0, 0);
			mBitmap = Bitmap.createScaledBitmap(mGIFBitmap, mTextureSize, mTextureSize, false);
			TextureManager.getInstance().replaceTexture(this);
			replace();
		}
	}
	
	@Override
	public void setResourceId(int resourceId) {
		if(mResourceId == resourceId)
			return;
		mResourceId = resourceId;
		mLoadNewGIF = true;
	}
	
	@Override
	public void reset() throws TextureException
	{
		super.reset();
		
		if(mGIFBitmap != null)
		{
			mGIFBitmap.recycle();
			mGIFBitmap = null;
		}
		
		mCanvas = null;
		mMovie = null;
	}
	
	@Override
	void remove() throws TextureException
	{
		if(mGIFBitmap != null)
		{
			mGIFBitmap.recycle();
			mGIFBitmap = null;
		}
		
		mCanvas = null;
		mMovie = null;
		
		super.remove();
	}

	@Override
	public int getResourceId()
	{
		return mResourceId;
	}
	
	public Canvas getCanvas() {
		return mCanvas;
	}
	
	public Movie getMovie() {
		return mMovie;
	}
	
	@Override
	public int getWidth() {
		return mWidth;
	}
	
	@Override
	public int getHeight() {
		return mHeight;
	}
	
	public int getTextureSize() {
		return mTextureSize;
	}

	public void setLoop(boolean loop) {
		mLoop = loop;
	}

	public boolean getLoop() {
		return mLoop;
	}

	public int getLoopCount() {
		return mLoopCount;
	}

	public boolean isAnimationRunning() {
		return mAnimationRunning;
	}

	public void stopAnimation() {
		mAnimationRunning = false;
	}

	public void animate() {
		mLoopCount = 0;
		rewind();
	}
}
