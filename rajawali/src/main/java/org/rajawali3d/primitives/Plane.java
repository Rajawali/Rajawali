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
import org.rajawali3d.math.vector.Vector3.Axis;

/**
 * A plane primitive. The constructor takes two boolean arguments that indicate whether certain buffers should be
 * created or not. Not creating these buffers can reduce memory footprint.
 * <p>
 * When creating solid color plane both <code>createTextureCoordinates</code> and <code>createVertexColorBuffer</code>
 * can be set to <code>false</code>.
 * <p>
 * When creating a textured plane <code>createTextureCoordinates</code> should be set to <code>true</code> and
 * <code>createVertexColorBuffer</code> should be set to <code>false</code>.
 * <p>
 * When creating a plane without a texture but with different colors per texture <code>createTextureCoordinates</code>
 * should be set to <code>false</code> and <code>createVertexColorBuffer</code> should be set to <code>true</code>.
 * 
 * @author dennis.ippel
 * 
 */
public class Plane extends Object3D {

	protected float mWidth;
	protected float mHeight;
	protected int mSegmentsW;
	protected int mSegmentsH;
	protected int mNumTextureTiles;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;
	private Axis mUpAxis;

	/**
	 * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * The plane will be facing the camera ({@link Axis.Z}) by default.
	 */
	public Plane() {
		this(1f, 1f, 1, 1, Axis.Z, true, false, 1);
	}
	
	/**
	 * Create a plane primitive. Calling this constructor will create a plane facing the specified axis.
	 * @param upAxis
	 */
	public Plane(Axis upAxis)
	{
		this(1f, 1f, 1, 1, upAxis, true, false, 1);
	}
	
	/**
	 * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * The plane will be facing the camera ({@link Axis.Z}) by default.
	 * 
	 * @param width
	 *            The plane width
	 * @param height
	 *            The plane height
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 */
	public Plane(float width, float height, int segmentsW, int segmentsH)
	{
		this(width, height, segmentsW, segmentsH, Axis.Z, true, false, 1);
	}
	
	/**
	 * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * The plane will be facing the camera ({@link Axis.Z}) by default.
	 * 
	 * @param width
	 *            The plane width
	 * @param height
	 *            The plane height
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param numTextureTiles
	 * 			  The number of texture tiles. If more than 1 the texture will be repeat by n times.
	 */
	public Plane(float width, float height, int segmentsW, int segmentsH, int numTextureTiles)
	{
		this(width, height, segmentsW, segmentsH, Axis.Z, true, false, numTextureTiles);
	}

	/**
	 * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * 
	 * @param width
	 *            The plane width
	 * @param height
	 *            The plane height
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param upAxis
	 * 			  The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
	 */
	public Plane(float width, float height, int segmentsW, int segmentsH, Axis upAxis)
	{
		this(width, height, segmentsW, segmentsH, upAxis, true, false, 1);
	}

	/**
	 * Creates a plane primitive.
	 * 
	 * @param width
	 *            The plane width
	 * @param height
	 *            The plane height
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param upAxis
	 * 			  The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
	 * @param createTextureCoordinates
	 *            A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *            A boolean that indicates whether a vertex color buffer should be created or not.
	 */
	public Plane(float width, float height, int segmentsW, int segmentsH, Axis upAxis, boolean createTextureCoordinates,
			boolean createVertexColorBuffer) {
		this(width, height, segmentsW, segmentsH, upAxis, createTextureCoordinates, createVertexColorBuffer, 1);
	}

    /**
     * Creates a plane primitive.
     *
     * @param width                    The plane width
     * @param height                   The plane height
     * @param segmentsW                The number of vertical segments
     * @param segmentsH                The number of horizontal segments
     * @param upAxis                   The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
     * @param createTextureCoordinates A boolean that indicates whether the texture coordinates should be calculated or not.
     * @param createVertexColorBuffer  A boolean that indicates whether a vertex color buffer should be created or not.
     * @param numTextureTiles          The number of texture tiles. If more than 1 the texture will be repeat by n times.
     */
    public Plane(float width, float height, int segmentsW, int segmentsH, Axis upAxis, boolean createTextureCoordinates,
                 boolean createVertexColorBuffer, int numTextureTiles) {
        this(width, height, segmentsW, segmentsH, upAxis, createTextureCoordinates, createVertexColorBuffer, numTextureTiles, true);
    }

	/**
	 * Creates a plane primitive.
	 * 
	 * @param width
	 *            The plane width
	 * @param height
	 *            The plane height
	 * @param segmentsW
	 *            The number of vertical segments
	 * @param segmentsH
	 *            The number of horizontal segments
	 * @param upAxis
	 * 			  The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
	 * @param createTextureCoordinates
	 *            A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *            A boolean that indicates whether a vertex color buffer should be created or not.
	 * @param numTextureTiles
	 * 			  The number of texture tiles. If more than 1 the texture will be repeat by n times.
     * @param createVBOs
     *            A boolean that indicates whether the VBOs should be created immediately.
	 */
	public Plane(float width, float height, int segmentsW, int segmentsH, Axis upAxis, boolean createTextureCoordinates,
			boolean createVertexColorBuffer, int numTextureTiles, boolean createVBOs) {
		super();
		mWidth = width;
		mHeight = height;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		mUpAxis = upAxis;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		mNumTextureTiles = numTextureTiles;
		init(createVBOs);
	}

	private void init(boolean createVBOs) {
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

		for (i = 0; i <= mSegmentsW; i++) {
			for (j = 0; j <= mSegmentsH; j++) {
				float v1 = ((float) i / (float) mSegmentsW - 0.5f) * mWidth;
				float v2 = ((float) j / (float) mSegmentsH - 0.5f) * mHeight;
				if(mUpAxis == Axis.X)
				{
					vertices[vertexCount] = 0;
					vertices[vertexCount + 1] = v1;
					vertices[vertexCount + 2] = v2;
				}
				else if(mUpAxis == Axis.Y)
				{
					vertices[vertexCount] = v1;
					vertices[vertexCount + 1] = 0;
					vertices[vertexCount + 2] = v2;
				}
				else if(mUpAxis == Axis.Z)
				{
					vertices[vertexCount] = v1;
					vertices[vertexCount + 1] = v2;
					vertices[vertexCount + 2] = 0;
				}

				if (mCreateTextureCoords) {
					float u = (float) i / (float) mSegmentsW;
					textureCoords[texCoordCount++] = (1.0f - u) * mNumTextureTiles;
					float v = (float) j / (float) mSegmentsH;
					textureCoords[texCoordCount++] = (1.0f - v) * mNumTextureTiles;
				}

				normals[vertexCount] = mUpAxis == Axis.X ? 1 : 0;
				normals[vertexCount + 1] = mUpAxis == Axis.Y ? 1 : 0;
				normals[vertexCount + 2] = mUpAxis == Axis.Z ? 1 : 0;

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

                if (mUpAxis == Axis.X || mUpAxis == Axis.Z) {
                    indices[indexCount++] = (int) ur;
                    indices[indexCount++] = (int) lr;
                    indices[indexCount++] = (int) ul;

                    indices[indexCount++] = (int) lr;
                    indices[indexCount++] = (int) ll;
                    indices[indexCount++] = (int) ul;
                } else {
                    indices[indexCount++] = (int) ur;
                    indices[indexCount++] = (int) ul;
                    indices[indexCount++] = (int) lr;

                    indices[indexCount++] = (int) lr;
                    indices[indexCount++] = (int) ul;
                    indices[indexCount++] = (int) ll;
                }
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
	}
}