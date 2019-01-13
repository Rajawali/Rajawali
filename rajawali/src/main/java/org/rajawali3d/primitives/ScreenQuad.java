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
 *
 * When creating solid color plane both <code>createTextureCoordinates</code> and <code>createVertexColorBuffer</code>
 * can be set to <code>false</code>.
 * <p>
 * When creating a textured plane <code>createTextureCoordinates</code> should be set to <code>true</code> and
 * <code>createVertexColorBuffer</code> should be set to <code>false</code>.
 * <p>
 * When creating a plane without a texture but with different colors per texture <code>createTextureCoordinates</code>
 * should be set to <code>false</code> and <code>createVertexColorBuffer</code> should be set to <code>true</code>.
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

	protected int mSegmentsW;
	protected int mSegmentsH;
	protected int mNumTextureTiles;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;
	private Camera2D mCamera;
	private Matrix4 mVPMatrix;
	private EffectPass mEffectPass;

	public enum UVmapping {
		CW,
		CCW,
	}

	public ScreenQuad() {
		this(1, 1, true, false, 1, true, UVmapping.CCW);
	}

	/**
	 * Create a ScreenQuad. Calling this constructor will create texture coordinates but no vertex color buffer.
	 */
	public ScreenQuad(UVmapping mapping) {
		this(1, 1, true, false, 1, true, mapping);
	}

	public ScreenQuad(boolean createVBOs) {
		this(1, 1, true, false, 1, createVBOs, UVmapping.CCW);
	}

	/**
	 * Create a ScreenQuad. Calling this constructor will create texture coordinates but no vertex color buffer.
	 *
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 */
	public ScreenQuad(int segmentsW, int segmentsH)
	{
		this(segmentsW, segmentsH, true, false, 1, true, UVmapping.CCW);
	}

	/**
	 * Create a ScreenQuad. Calling this constructor will create texture coordinates but no vertex color buffer.
	 *
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param numTextureTiles
	 * 			  The number of texture tiles. If more than 1 the texture will be repeat by n times.
	 */
	public ScreenQuad(int segmentsW, int segmentsH, int numTextureTiles, boolean createVBOs)
	{
		this(segmentsW, segmentsH, true, false, numTextureTiles, createVBOs, UVmapping.CCW);
	}

	/**
	 * Create a ScreenQuad. Calling this constructor will create texture coordinates but no vertex color buffer.
	 *
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 */
	public ScreenQuad(int segmentsW, int segmentsH, boolean createVBOs)
	{
		this(segmentsW, segmentsH, true, false, 1, createVBOs, UVmapping.CCW);
	}

	/**
	 * Creates a ScreenQuad.
	 *
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param createTextureCoordinates
	 *            A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *            A boolean that indicates whether a vertex color buffer should be created or not.
	 */
	public ScreenQuad(int segmentsW, int segmentsH, boolean createTextureCoordinates,
			boolean createVertexColorBuffer, boolean createVBOs) {
		this(segmentsW, segmentsH, createTextureCoordinates, createVertexColorBuffer, 1, createVBOs, UVmapping.CCW);
	}

	/**
	 * Creates a ScreenQuad.
	 *
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param createTextureCoordinates
	 *            A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *            A boolean that indicates whether a vertex color buffer should be created or not.
	 * @param numTextureTiles
	 * 			  The number of texture tiles. If more than 1 the texture will be repeat by n times.
     * @param createVBOs
     *            A boolean that indicates whether the VBOs should be created immediately.
	 */
	public ScreenQuad(int segmentsW, int segmentsH, boolean createTextureCoordinates,
			boolean createVertexColorBuffer, int numTextureTiles, boolean createVBOs, UVmapping mapping) {
		super();
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		mNumTextureTiles = numTextureTiles;
		init(createVBOs, mapping);
	}

	private void init(boolean createVBOs, UVmapping mapping) {
		int i, j;
		int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
		float[] vertices = new float[numVertices * 3];
		float[] textureCoords = null;
		if (mCreateTextureCoords)
			textureCoords = new float[numVertices * 2];
		float[] normals = new float[numVertices * 3];
		float[] colors = null;
		if (mCreateVertexColorBuffer)
			colors = new float[numVertices * 4];
		int[] indices = new int[mSegmentsW * mSegmentsH * 6];
		int vertexCount = 0;
		int texCoordCount = 0;

		mCamera = new Camera2D();
		mCamera.setProjectionMatrix(0, 0);
		mVPMatrix = new Matrix4();

		for (i = 0; i <= mSegmentsW; i++) {
			for (j = 0; j <= mSegmentsH; j++) {
				float v1 = ((float) i / (float) mSegmentsW - 0.5f);
				float v2 = ((float) j / (float) mSegmentsH - 0.5f);
				vertices[vertexCount] = v1;
				vertices[vertexCount + 1] = v2;
				vertices[vertexCount + 2] = 0;

				if (mCreateTextureCoords) {
					float u = (float) i / (float) mSegmentsW;
					textureCoords[texCoordCount++] = u * mNumTextureTiles;
					float v = (float) j / (float) mSegmentsH;
					if(mapping==UVmapping.CCW) {
						textureCoords[texCoordCount++] = (1.0f - v) * mNumTextureTiles;
					} else {
						textureCoords[texCoordCount++] = v * mNumTextureTiles;
					}
				}

				normals[vertexCount] = 0;
				normals[vertexCount + 1] = 0;
				normals[vertexCount + 2] = 1;

				vertexCount += 3;
			}
		}

		int colspan = mSegmentsH + 1;
		int indexCount = 0;

		for (int col = 0; col < mSegmentsW; col++) {
			for (int row = 0; row < mSegmentsH; row++) {
				int ul = col * colspan + row;
				int ll = ul + 1;
				int ur = (col + 1) * colspan + row;
				int lr = ur + 1;

				indices[indexCount++] = ur;
				indices[indexCount++] = lr;
				indices[indexCount++] = ul;

				indices[indexCount++] = lr;
				indices[indexCount++] = ll;
				indices[indexCount++] = ul;
			}
		}

		if (mCreateVertexColorBuffer)
		{
			int numColors = numVertices * 4;
			for (j = 0; j < numColors; j += 4)
			{
				colors[j] = 1.0f;
				colors[j + 1] = 1.0f;
				colors[j + 2] = 1.0f;
				colors[j + 3] = 1.0f;
			}
		}

		setData(vertices, normals, textureCoords, colors, indices, createVBOs);

		vertices = null;
		normals = null;
		textureCoords = null;
		colors = null;
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
