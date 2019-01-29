/**
 * Copyright 2013 Dennis Ippel
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.materials.textures;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;

import java.io.IOException;

public class StreamingTexture extends ATexture {

    public interface ISurfaceListener {

        void setSurface(Surface surface);
    }

    private final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private MediaPlayer mMediaPlayer;
    private Camera mCamera;
    private ISurfaceListener mSurfaceListener;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener;

    public StreamingTexture(String textureName, MediaPlayer mediaPlayer) {
        super(TextureType.VIDEO_TEXTURE, textureName);
        mMediaPlayer = mediaPlayer;
        setGLTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }

    public StreamingTexture(String textureName, Camera camera, SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener) {
        super(TextureType.VIDEO_TEXTURE, textureName);
        mCamera = camera;
        mOnFrameAvailableListener = onFrameAvailableListener;
        setGLTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }

    public StreamingTexture(String textureName, ISurfaceListener listener) {
        super(TextureType.VIDEO_TEXTURE, textureName);
        mSurfaceListener = listener;
        setGLTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }

    public StreamingTexture(StreamingTexture other) {
        super(other);
    }

    public StreamingTexture clone() {
        return new StreamingTexture(this);
    }

    protected void add() throws TextureException {
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
        if (mMediaPlayer != null) {
            mSurface = new Surface(mSurfaceTexture);
            mMediaPlayer.setSurface(mSurface);
        } else if (mCamera != null) {
            try {
                mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
                mCamera.setPreviewTexture(mSurfaceTexture);
            } catch (IOException e) {
                throw new TextureException(e);
            }
        } else if (mSurfaceListener != null) {
            mSurfaceListener.setSurface(new Surface(mSurfaceTexture));
        }
    }

    protected void remove() {
        GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        mSurfaceTexture.release();
    }

    protected void replace() {
        return;
    }

    protected void reset() {
        mSurfaceTexture.release();
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void update() {
        if (mSurfaceTexture != null)
            mSurfaceTexture.updateTexImage();
    }

    public void updateMediaPlayer(MediaPlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
        mMediaPlayer.setSurface(mSurface);
    }
}
