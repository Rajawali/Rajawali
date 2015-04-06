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
package org.rajawali3d.animation.mesh;

import android.opengl.GLES20;
import android.os.SystemClock;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.Geometry3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.plugins.VertexAnimationMaterialPlugin;

public class VertexAnimationObject3D extends AAnimationObject3D {
	private VertexAnimationMaterialPlugin mMaterialPlugin;
	
	public VertexAnimationObject3D() {
		super();
	}

	public void setShaderParams(Camera camera) {
		super.setShaderParams(camera);

		long now = SystemClock.uptimeMillis();

		// Calculate interpolation and frame delta (if playing)
		if (isPlaying()) {
			mInterpolation += (now - mStartTime) * mFps / 1000.0;
			mCurrentFrameIndex += (int) mInterpolation; // advance frame if interpolation >= 1
			if (mCurrentFrameIndex > mEndFrameIndex) {
				if (mLoop) {
					mCurrentFrameIndex -= mStartFrameIndex;
					mCurrentFrameIndex %= mEndFrameIndex - mStartFrameIndex;
					mCurrentFrameIndex += mStartFrameIndex;
				} else {
					mCurrentFrameIndex = mEndFrameIndex;
					pause();
				}
			}
			mInterpolation -= (int) mInterpolation; // clamp to [0, 1)
		}

		// Update geometry (if current frame is different from before)
		Geometry3D currentGeometry = ((VertexAnimationFrame) mFrames.get(mCurrentFrameIndex)).getGeometry();
		if (mGeometry.getVertexBufferInfo() != currentGeometry.getVertexBufferInfo()) {
			mGeometry.setVertexBufferInfo(currentGeometry.getVertexBufferInfo());
			mGeometry.setNormalBufferInfo(currentGeometry.getNormalBufferInfo());
		}

		// Find geometry for next frame in sequence
		Geometry3D nextGeometry = currentGeometry;
		int nextFrame = mCurrentFrameIndex + 1;
		if (nextFrame > mEndFrameIndex) {
			if (mLoop) {
				nextFrame = mStartFrameIndex;
			} else {
				nextFrame = mEndFrameIndex;
			}
		}
		if (nextFrame >= 0 && nextFrame < mNumFrames) {
			nextGeometry = mFrames.get(nextFrame).getGeometry();
		}

		// Set shader parameters
		mMaterialPlugin.setInterpolation(mInterpolation);
		mMaterialPlugin.setNextFrameVertices(nextGeometry.getVertexBufferInfo().bufferHandle);
		mMaterialPlugin.setNextFrameNormals(nextGeometry.getNormalBufferInfo().bufferHandle);
		mStartTime = now;
	}

	public void reload() {
		for (int i = 0; i < mNumFrames; i++) {
			mFrames.get(i).getGeometry().reload();
		}
		super.reload();
	}
	
	@Override
	public void setMaterial(Material material) {
		super.setMaterial(material);
		
		IMaterialPlugin plugin = material.getPlugin(VertexAnimationMaterialPlugin.class);
		
		if(plugin == null)
		{
			mMaterialPlugin = new VertexAnimationMaterialPlugin();
			material.addPlugin(mMaterialPlugin);
		}
		else
		{
			mMaterialPlugin = (VertexAnimationMaterialPlugin)plugin;
		}
	}

	public VertexAnimationObject3D clone(boolean copyMaterial) {
		VertexAnimationObject3D clone = new VertexAnimationObject3D();
		clone.getGeometry().copyFromGeometry3D(mGeometry);
		clone.isContainer(mIsContainerOnly);
		clone.setMaterial(mMaterial);

		for (int i = 0; i < mNumFrames; ++i) {
			clone.addFrame(getFrame(i));
		}
		clone.setRotation(getOrientation());
		clone.setScale(getScale());
		clone.setFps(mFps);
		clone.mElementsBufferType = mGeometry.areOnlyShortBuffersSupported() ? GLES20.GL_UNSIGNED_SHORT : GLES20.GL_UNSIGNED_INT;
		return clone;
	}

	public VertexAnimationObject3D clone() {
		return clone(true);
	}
}
