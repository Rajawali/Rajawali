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

import org.rajawali3d.Object3D;

/**
 * A sphere primitive. The constructor takes two boolean arguments that indicate whether certain buffers should be
 * created or not. Not creating these buffers can reduce memory footprint.
 * <p>
 * When creating solid color sphere both <code>createTextureCoordinates</code> and <code>createVertexColorBuffer</code>
 * can be set to <code>false</code>.
 * <p>
 * When creating a textured sphere <code>createTextureCoordinates</code> should be set to <code>true</code> and
 * <code>createVertexColorBuffer</code> should be set to <code>false</code>.
 * <p>
 * When creating a sphere without a texture but with different colors per texture <code>createTextureCoordinates</code>
 * should be set to <code>false</code> and <code>createVertexColorBuffer</code> should be set to <code>true</code>.
 * 
 * @author dennis.ippel
 * 
 */
public class Sphere extends Object3D {

	private final float PI = (float) Math.PI;
	private float mRadius;
	private int mSegmentsW;
	private int mSegmentsH;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;

	/**
	 * Creates a sphere primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * 
	 * @param radius
	 *            The radius of the sphere
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 */
	public Sphere(float radius, int segmentsW, int segmentsH) {
		this(radius, segmentsW, segmentsH, true, false, true);
	}

	/**
	 * Creates a sphere primitive.
	 * 
	 * @param radius
	 *            The radius of the sphere
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param createTextureCoordinates
	 *            A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *            A boolean that indicates whether a vertex color buffer should be created or not.
     * @param createVertexColorBuffer
     *            A boolean that indicates whether the VBOs should be created immediately.
	 */
	public Sphere(float radius, int segmentsW, int segmentsH, boolean createTextureCoordinates,
			boolean createVertexColorBuffer, boolean createVBOs) {
		super();
		mRadius = radius;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		init(createVBOs);
	}

	protected void init(boolean createVBOs) {
		int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
		int numIndices = 2 * mSegmentsW * (mSegmentsH - 1) * 3;

		float[] vertices = new float[numVertices * 3];
		float[] normals = new float[numVertices * 3];
		int[] indices = new int[numIndices];

		int i, j;
		int vertIndex = 0, index = 0;
		final float normLen = 1.0f / mRadius;

		for (j = 0; j <= mSegmentsH; ++j) {
			float horAngle = PI * j / mSegmentsH;
			float z = mRadius * (float) Math.cos(horAngle);
			float ringRadius = mRadius * (float) Math.sin(horAngle);

			for (i = 0; i <= mSegmentsW; ++i) {
				float verAngle = 2.0f * PI * i / mSegmentsW;
				float x = ringRadius * (float) Math.cos(verAngle);
				float y = ringRadius * (float) Math.sin(verAngle);

				normals[vertIndex] = x * normLen;
				vertices[vertIndex++] = x;
				normals[vertIndex] = z * normLen;
				vertices[vertIndex++] = z;
				normals[vertIndex] = y * normLen;
				vertices[vertIndex++] = y;

				if (i > 0 && j > 0) {
					int a = (mSegmentsW + 1) * j + i;
					int b = (mSegmentsW + 1) * j + i - 1;
					int c = (mSegmentsW + 1) * (j - 1) + i - 1;
					int d = (mSegmentsW + 1) * (j - 1) + i;

					if (j == mSegmentsH) {
						indices[index++] = a;
						indices[index++] = c;
						indices[index++] = d;
					} else if (j == 1) {
						indices[index++] = a;
						indices[index++] = b;
						indices[index++] = c;
					} else {
						indices[index++] = a;
						indices[index++] = b;
						indices[index++] = c;
						indices[index++] = a;
						indices[index++] = c;
						indices[index++] = d;
					}
				}
			}
		}

		float[] textureCoords = null;
		if (mCreateTextureCoords) {
			int numUvs = (mSegmentsH + 1) * (mSegmentsW + 1) * 2;
			textureCoords = new float[numUvs];

			numUvs = 0;
			for (j = 0; j <= mSegmentsH; ++j) {
				for (i = mSegmentsW; i >= 0; --i) {
					textureCoords[numUvs++] = (float) i / mSegmentsW;
					textureCoords[numUvs++] = (float) j / mSegmentsH;
				}
			}
		}

		float[] colors = null;

		if (mCreateVertexColorBuffer)
		{
			int numColors = numVertices * 4;
			colors = new float[numColors];
			for (j = 0; j < numColors; j += 4)
			{
				colors[j] = 1.0f;
				colors[j + 1] = 0;
				colors[j + 2] = 0;
				colors[j + 3] = 1.0f;
			}
		}

		setData(vertices, normals, textureCoords, colors, indices, createVBOs);

		vertices = null;
		normals = null;
		textureCoords = null;
		indices = null;
	}
}
