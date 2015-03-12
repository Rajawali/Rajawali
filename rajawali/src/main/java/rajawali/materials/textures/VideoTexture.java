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

import java.io.IOException;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;

public class VideoTexture extends ATexture {

	private final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	private MediaPlayer mMediaPlayer;
	private Camera mCamera;
	private SurfaceTexture mSurfaceTexture;
	SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener;
	
	public VideoTexture(String textureName, MediaPlayer mediaPlayer)
	{
		super(TextureType.VIDEO_TEXTURE, textureName);
		mMediaPlayer = mediaPlayer;
		setGLTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
	}
	
	public VideoTexture(String textureName, Camera camera, SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener)
	{
		super(TextureType.VIDEO_TEXTURE, textureName);
		mCamera = camera;
		mOnFrameAvailableListener = onFrameAvailableListener;
		setGLTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
	}

	public VideoTexture(VideoTexture other)
	{
		super(other);
	}

	public VideoTexture clone() {
		return new VideoTexture(this);
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
		if(mMediaPlayer != null)
			mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
		else if(mCamera != null)
			try {
				mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
				mCamera.setPreviewTexture(mSurfaceTexture);
			} catch (IOException e) {
				throw new TextureException(e);
			}
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
