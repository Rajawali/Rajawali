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
package rajawali.primitives;

import rajawali.Object3D;

/**
 * A cube primitive. The constructor takes two boolean arguments that indicate whether certain buffers should be
 * created or not. Not creating these buffers can reduce memory footprint.
 * <p>
 * When creating solid color cube both <code>createTextureCoordinates</code> and <code>createVertexColorBuffer</code>
 * can be set to <code>false</code>.
 * <p>
 * When creating a textured cube <code>createTextureCoordinates</code> should be set to <code>true</code> and
 * <code>createVertexColorBuffer</code> should be set to <code>false</code>.
 * <p>
 * When creating a cube without a texture but with different colors per texture <code>createTextureCoordinates</code>
 * should be set to <code>false</code> and <code>createVertexColorBuffer</code> should be set to <code>true</code>.
 * 
 * @author dennis.ippel
 * 
 */
public class Cube extends Object3D {

	private float mSize;
	private boolean mIsSkybox;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;

	/**
	 * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer. 
	 * @param size		The size of the cube.
	 */
	public Cube(float size) {
		this(size, false, false, true, false);
	}

	/**
	 * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * 
	 * @param size			The size of the cube.
	 * @param isSkybox		A boolean that indicates whether this is a skybox or not. If set to true the normals will 
	 * 						be inverted.
	 */
	public Cube(float size, boolean isSkybox) {
		this(size, isSkybox, true, true, false);
	}
	
	/**
	 * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 * 
	 * @param size					The size of the cube.
	 * @param isSkybox				A boolean that indicates whether this is a skybox or not. If set to true the normals will 
	 * 								be inverted.
	 * @param hasCubemapTexture		A boolean that indicates a cube map texture will be used (6 textures) or a regular 
	 * 								single texture.
	 */
	public Cube(float size, boolean isSkybox, boolean hasCubemapTexture)
	{
		this(size, isSkybox, hasCubemapTexture, true, false);
	}

	/**
	 * Creates a cube primitive.
	 * 
	 * @param size						The size of the cube.
	 * @param isSkybox					A boolean that indicates whether this is a skybox or not. If set to true the normals will 
	 * 									be inverted.
	 * @param hasCubemapTexture			A boolean that indicates a cube map texture will be used (6 textures) or a regular 
	 * 									single texture.
	 * @param createTextureCoordinates	A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer	A boolean that indicates whether a vertex color buffer should be created or not.
	 */
	public Cube(float size, boolean isSkybox, boolean hasCubemapTexture, boolean createTextureCoordinates,
			boolean createVertexColorBuffer) {
		super();
		mIsSkybox = isSkybox;
		mSize = size;
		mHasCubemapTexture = hasCubemapTexture;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		init();
	}

	private void init()
	{
		float halfSize = mSize * .5f;
		float[] vertices = {
				// -- back
				halfSize, halfSize, halfSize, 			-halfSize, halfSize, halfSize,
				-halfSize, -halfSize, halfSize,			halfSize, -halfSize, halfSize, // 0-1-halfSize-3 front
				
				halfSize, halfSize, halfSize, 			halfSize, -halfSize, halfSize, 
				halfSize, -halfSize, -halfSize, 		halfSize, halfSize, -halfSize,// 0-3-4-5 right
				// -- front
				halfSize, -halfSize, -halfSize, 		-halfSize, -halfSize, -halfSize, 
				-halfSize, halfSize, -halfSize,			halfSize, halfSize, -halfSize,// 4-7-6-5 back
				
				-halfSize, halfSize, halfSize, 			-halfSize, halfSize, -halfSize, 
				-halfSize, -halfSize, -halfSize,		-halfSize,	-halfSize, halfSize,// 1-6-7-halfSize left
				
				halfSize, halfSize, halfSize, 			halfSize, halfSize, -halfSize, 
				-halfSize, halfSize, -halfSize, 		-halfSize, halfSize, halfSize, // top
				
				halfSize, -halfSize, halfSize, 			-halfSize, -halfSize, halfSize, 
				-halfSize, -halfSize, -halfSize,		halfSize, -halfSize, -halfSize,// bottom
		};

		float[] textureCoords = null;
		float[] skyboxTextureCoords = null;

		if (mCreateTextureCoords && !mIsSkybox && !mHasCubemapTexture)
		{
			textureCoords = new float[]
			{
					0, 1, 1, 1, 1, 0, 0, 0, // front
					0, 1, 1, 1, 1, 0, 0, 0, // up
					0, 1, 1, 1, 1, 0, 0, 0, // back
					0, 1, 1, 1, 1, 0, 0, 0, // down
					0, 1, 1, 1, 1, 0, 0, 0, // right
					0, 1, 1, 1, 1, 0, 0, 0, // left
			};
		}
		else if (mIsSkybox && !mHasCubemapTexture)
		{
			skyboxTextureCoords = new float[] {
					.25f, .3333f, .5f, .3333f, .5f, .6666f, .25f, .6666f, // back
					.25f, .3333f, .25f, .6666f, 0, .6666f, 0, .3333f, // left
					1, .6666f, .75f, .6666f, .75f, .3333f, 1, .3333f, // front
					.5f, .3333f, .75f, .3333f, .75f, .6666f, .5f, .6666f, // right
					.25f, .3333f, .25f, 0, .5f, 0, .5f, .3333f, // up
					.25f, .6666f, .5f, .6666f, .5f, 1, .25f, 1 // down
			};
		}

		float[] colors = null;
		if (mCreateVertexColorBuffer)
		{
			colors = new float[] {
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
			};
		}

		float n = 1;

		float[] normals = {
				0, 0, n, 0, 0, n, 0, 0, n, 0, 0, n, // front
				n, 0, 0, n, 0, 0, n, 0, 0, n, 0, 0, // right
				0, 0, -n, 0, 0, -n, 0, 0, -n, 0, 0, -n, // back
				-n, 0, 0, -n, 0, 0, -n, 0, 0, -n, 0, 0, // left
				0, n, 0, 0, n, 0, 0, n, 0, 0, n, 0, // top
				0, -n, 0, 0, -n, 0, 0, -n, 0, 0, -n, 0, // bottom
		};

		int[] indices = {
				0, 1, 2, 0, 2, 3,
				4, 5, 6, 4, 6, 7,
				8, 9, 10, 8, 10, 11,
				12, 13, 14, 12, 14, 15,
				16, 17, 18, 16, 18, 19,
				20, 21, 22, 20, 22, 23,
		};
		int[] skyboxIndices = {
				2, 1, 0, 3, 2, 0,
				6, 5, 4, 7, 6, 4,
				10, 9, 8, 11, 10, 8,
				14, 13, 12, 15, 14, 12,
				18, 17, 16, 19, 18, 16,
				22, 21, 20, 23, 22, 20
		};

		setData(vertices, normals, mIsSkybox || mHasCubemapTexture ? skyboxTextureCoords : textureCoords, colors,
				mIsSkybox && mHasCubemapTexture ? skyboxIndices : indices);
		
		vertices = null;
		normals = null;
		skyboxTextureCoords = null;
		textureCoords = null;
		colors = null;
		skyboxIndices = null;
		indices = null;
	}
}
