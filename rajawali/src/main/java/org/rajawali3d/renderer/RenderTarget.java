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
package org.rajawali3d.renderer;

import org.rajawali3d.materials.textures.ATexture.FilterType;
import org.rajawali3d.materials.textures.ATexture.WrapType;
import org.rajawali3d.materials.textures.RenderTargetTexture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.util.RajLog;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLU;

/**
 * Defines configurations for a given render target.
 * 
 * @author Andrew Jo (andrewjo@gmail.com)
 * @author dennis.ippel
 */
public class RenderTarget {

	protected int mWidth;
	protected int mHeight;
	protected int mOffsetX;
	protected int mOffsetY;
	protected String mName;
	protected boolean mMipmaps;
	protected int mGLType;
	protected Config mBitmapConfig;
	protected FilterType mFilterType;
	protected WrapType mWrapType;

	protected boolean mStencilBuffer;
	protected boolean mFullscreen = true;

	protected int mFrameBufferHandle;
	protected int mDepthBufferHandle;
	protected int mStencilBufferHandle;

	protected RenderTargetTexture mTexture;

	/**
	 * Instantiates a new RenderTarget object
	 * 
	 * @param name
	 *            The name of the render target. This should be unique and should comply with regular variable naming
	 *            standards.
	 * @param width
	 *            Width of the render target
	 * @param height
	 *            Height of the render target
	 * @param offsetX
	 *            Horizontal offset of the render target
	 * @param offsetY
	 *            Vertical offset of the render target
	 * @param depthBuffer
	 *            Set to true to enable depth buffer
	 * @param stencilBuffer
	 *            Set to true to enable stencil buffer
	 * @param mipmaps
	 *            Set to true to enable automatic mipmap generation
	 * @param glType
	 *            Datatype to use for the texture
	 * @param bitmapConfig
	 *            Bitmap configuration
	 * @param filterType
	 *            Texture filter type
	 * @param wrapType
	 *            Texture wrap type
	 */
	public RenderTarget(String name, int width, int height, int offsetX, int offsetY,
			boolean stencilBuffer, boolean mipmaps,
			int glType, Config bitmapConfig, FilterType filterType,
			WrapType wrapType) {
		mName = name;
		mWidth = width;
		mHeight = height;
		mOffsetX = offsetX;
		mOffsetY = offsetY;
		mStencilBuffer = stencilBuffer;
		mMipmaps = mipmaps;
		mGLType = glType;
		mBitmapConfig = bitmapConfig;
		mFilterType = filterType;
		mWrapType = wrapType;
		
		mTexture = new RenderTargetTexture(mName + "FBTex", mWidth, mHeight);
		mTexture.setMipmap(mMipmaps);
		mTexture.setGLTextureType(mGLType);
		mTexture.setBitmapConfig(mBitmapConfig);
		mTexture.setFilterType(mFilterType);
		mTexture.setWrapType(mWrapType);
	}
	

	/**
	 * Instantiates a new RenderTarget object with default values
	 * 
	 * @param width
	 *            Width of the render target
	 * @param height
	 *            Height of the render target
	 */
	public RenderTarget(String name, int width, int height) {
		this(name, width, height, 0, 0, false, false, GLES20.GL_TEXTURE_2D, Config.ARGB_8888, FilterType.LINEAR,
				WrapType.CLAMP);
	}

	@Override
	public RenderTarget clone() {
		return new RenderTarget(
				mName,
				mWidth,
				mHeight,
				mOffsetX,
				mOffsetY,
				mStencilBuffer,
				mTexture.isMipmap(),
				mTexture.getGLTextureType(),
				mTexture.getBitmapConfig(),
				mTexture.getFilterType(),
				mTexture.getWrapType());
	}

	/**
	 * Returns whether stencil buffer has been enabled for this render target.
	 * 
	 * @return True if stencil buffer is enabled, false otherwise.
	 */
	public boolean isStencilBufferEnabled() {
		return mStencilBuffer;
	}

	/**
	 * Sets whether stencil buffer is enabled.
	 * 
	 * @param stencilBuffer
	 *            Set to true to enable stencil buffer.
	 */
	public void enableStencilBuffer(boolean stencilBuffer) {
		mStencilBuffer = stencilBuffer;
	}

	/**
	 * Returns the current texture height for this render target.
	 * 
	 * @return The current texture height in pixels.
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * Sets the current texture height for this render target.
	 * 
	 * @param height
	 *            The current texture height in pixels. Set the dimension to power of two unless NPOT extensions are
	 *            supported.
	 */
	public void setHeight(int height) {
		mHeight = height;
		mTexture.setHeight(height);
	}

	/**
	 * Returns the horizontal value of the current texture offset coordinate.
	 * 
	 * @return The x component of the offset coordinate.
	 */
	public int getOffsetX() {
		return mOffsetX;
	}

	/**
	 * Sets the horizontal value of the current texture offset coordinate.
	 * 
	 * @param offsetX
	 *            The x component of the offset coordinate.
	 */
	public void setOffsetX(int offsetX) {
		mOffsetX = offsetX;
	}

	/**
	 * Returns the vertical value of the current texture offset coordinate.
	 * 
	 * @return The y component of the offset coordinate.
	 */
	public int getOffsetY() {
		return mOffsetY;
	}

	/**
	 * Sets the vertical value of the current texture offset coordinate.
	 * 
	 * @param offsetY
	 *            The y component of the offset coordinate.
	 */
	public void setOffsetY(int offsetY) {
		mOffsetY = offsetY;
	}

	/**
	 * Returns the current texture width for this render target.
	 * 
	 * @return The current texture width in pixels.
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * Sets the current texture width for this render target.
	 * 
	 * @param width
	 *            The current texture width in pixels. Set the dimension to power of two unless NPOT extensions are
	 *            supported.
	 */
	public void setWidth(int width) {
		mWidth = width;
		mTexture.setWidth(width);
	}

	public void create() {
		int[] bufferHandles = new int[1];
		GLES20.glGenFramebuffers(1, bufferHandles, 0);
		mFrameBufferHandle = bufferHandles[0];

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);

		checkGLError("Could not create framebuffer: ");
		// -- add the texture directly. we can afford to do this because the create()
		//    method is called in a thread safe manner.
		TextureManager.getInstance().taskAdd(mTexture);
				
		GLES20.glFramebufferTexture2D(
			      GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTexture.getTextureId(), 0);
		
		checkGLError("Could not create framebuffer 2: ");
		
		GLES20.glGenRenderbuffers(1, bufferHandles, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, bufferHandles[0]);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mWidth, mHeight);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, bufferHandles[0]);
		
		checkGLError("Could not create framebuffer 3: ");
/*
		if (mStencilBuffer)
		{
			
			GLES20.glGenRenderbuffers(1, bufferHandles, 0);
			mStencilBufferHandle = bufferHandles[0];
			GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mStencilBufferHandle);
			GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_STENCIL_INDEX8, mWidth, mHeight);
			GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_STENCIL_ATTACHMENT,
					GLES20.GL_RENDERBUFFER, mStencilBufferHandle);

			checkGLError("Could not create stencil buffer: ");
		}
	*/
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	}

	public void bind() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);
		GLES20.glFramebufferTexture2D(
			      GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTexture.getTextureId(), 0);
		
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			String errorString = "";
			switch(status)
			{
			case GLES20.GL_FRAMEBUFFER_UNSUPPORTED:
				errorString = "GL_FRAMEBUFFER_UNSUPPORTED: returned if the combination of internal formats of the attached images violates an implementation-dependent set of restrictions.";
				break;
			case GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
				errorString = "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT: returned if any of the framebuffer attachment points are framebuffer incomplete.";
				break;
			case GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
				errorString = "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: returned if the framebuffer does not have at least one image attached to it.";
				break;
			case GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
				errorString = "GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS: not all attached images have the same width and height.";
				RajLog.i(mWidth + ", " + mHeight + " || " + mTexture.getWidth() + ", " + mTexture.getHeight());
				break;
			}
			throw new RuntimeException(errorString);
		}
	}

	public void unbind() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	}

	public void remove() {
		GLES20.glDeleteFramebuffers(GLES20.GL_FRAMEBUFFER, new int[] { mFrameBufferHandle }, 0);
	}

	public void reload() {
		create();
	}

	public void checkGLError(String ex) {
		int error = GLES20.glGetError();
		if (error != GLES20.GL_NO_ERROR)
		{
			String description = GLU.gluErrorString(error);
			RajLog.e(ex + "[" + error + "]: " + description);
			throw new RuntimeException(ex + "[" + error + "]: " + description);
		}
	}

	public void setFullscreen(boolean fullscreen) {
		mFullscreen = fullscreen;
	}
	
	public boolean getFullscreen() {
		return mFullscreen;
	}
	
	public RenderTargetTexture getTexture() {
		return mTexture;
	}

	public int getFrameBufferHandle() {
		return mFrameBufferHandle;
	}
	
	public String getName() {
		return mName;
	}
}
