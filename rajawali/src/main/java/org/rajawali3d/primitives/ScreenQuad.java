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
package org.rajawali3d.primitives;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.cameras.Camera2D;
import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.postprocessing.passes.EffectPass;

/**
 * A screen quad is a plane that covers the whole screen. When used in conjunction with
 * {@link Camera2D} you'll get a pixel perfect screen filling plane. This is perfect for
 * things like image slide shows or fragment shader only apps and live wallpapers.
 * <p>
 * Usage:
 * </p>
 * <pre><code>
 * // -- Use the 2D camera
 * getCurrentScene().switchCamera(new Camera2D());
 * ScreenQuad screenQuad = new ScreenQuad();
 * SimpleMaterial material = new SimpleMaterial();
 * screenQuad.setMaterial(material);
 * </code></pre>
 * 
 * If you want to show square images without distortion you'll need to resize the quad
 * when the surface changes:
 * 
 * <pre><code>
 * public void onSurfaceChanged(GL10 gl, int width, int height) {
 * 	super.onSurfaceChanged(gl, width, height);
 * 	if(width < height)
 * 		screenQuad.setScale(height / width, 1, 0);
 * 	else
 * 		screenQuad.setScale(1, width / height, 0);
 * }
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class ScreenQuad extends Object3D {
	private Camera2D mCamera;
	private Matrix4 mVPMatrix;
	private EffectPass mEffectPass;

    /**
     * Creates a new ScreenQuad.
     */
    public ScreenQuad() {
        this(true);
    }

	/**
	 * Creates a new ScreenQuad.
	 */
	public ScreenQuad(boolean createVBOs)
	{
		super();
		init(createVBOs);
	}

	private void init(boolean createVBOs) {
		mCamera = new Camera2D();
		mCamera.setProjectionMatrix(0, 0);
		mVPMatrix = new Matrix4();
		
		float[] vertices = new float[] {
				-.5f, .5f, 0,
				.5f, .5f, 0,
				.5f, -.5f, 0,
				-.5f, -.5f, 0
		};
		float[] textureCoords = new float[] {
				0, 1, 1, 1, 1, 0, 0, 0
		};
		float[] normals = new float[] {
				0, 0, 1,
				0, 0, 1,
				0, 0, 1,
				0, 0, 1
		};
		int[] indices = new int[] { 0, 2, 1, 0, 3, 2 };
		
		setData(vertices, normals, textureCoords, null, indices, createVBOs);
		
		vertices = null;
		normals = null;
		textureCoords = null;
		indices = null;
		
		mEnableDepthTest = false;
		mEnableDepthMask = false;
	}
	
	public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, 
			final Matrix4 vMatrix, final Matrix4 parentMatrix, Material sceneMaterial) {
		final Matrix4 pMatrix = mCamera.getProjectionMatrix();
		final Matrix4 viewMatrix = mCamera.getViewMatrix();
		mVPMatrix.setAll(pMatrix).multiply(viewMatrix);
		super.render(mCamera, mVPMatrix, projMatrix, viewMatrix, null, sceneMaterial);
	}
	
	@Override
	protected void setShaderParams(Camera camera) {
		super.setShaderParams(camera);
		if(mEffectPass != null)
			mEffectPass.setShaderParams();
	}
	
	public void setEffectPass(EffectPass effectPass) {
		mEffectPass = effectPass;
	}
}
