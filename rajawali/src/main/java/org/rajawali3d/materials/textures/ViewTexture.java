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
package org.rajawali3d.materials.textures;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;

public class ViewTexture extends ATexture {

	public interface IViewTexture {

		public void setSurface(Surface surface);
	}

	private final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	private IViewTexture mViewPlayer;
	private SurfaceTexture mSurfaceTexture;
	SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener;

	public ViewTexture(String textureName, IViewTexture viewPlayer)
	{
		super(TextureType.VIDEO_TEXTURE, textureName);
		mViewPlayer = viewPlayer;
		setGLTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
	}

	public ViewTexture(ViewTexture other)
	{
		super(other);
	}

	public ViewTexture clone() {
		return new ViewTexture(this);
	}

	void add() throws TextureException {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
			GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
			GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		setTextureId(textureId);
		mSurfaceTexture = new SurfaceTexture(textureId);
		if(mViewPlayer != null)
			mViewPlayer.setSurface(new Surface(mSurfaceTexture));
	}

	void remove() throws TextureException {
		GLES20.glDeleteTextures(1, new int[] { mTextureId }, 0);
		mSurfaceTexture.release();
	}

	void replace() throws TextureException {
		return;
	}

	void reset() throws TextureException {
		mSurfaceTexture.release();
	}
	
	public SurfaceTexture getSurfaceTexture() {
		return mSurfaceTexture;
	}
	
	public void update()
	{
		if(mSurfaceTexture != null)
			mSurfaceTexture.updateTexImage();
	}
}
