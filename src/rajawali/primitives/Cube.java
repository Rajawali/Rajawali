package rajawali.primitives;

import rajawali.BaseObject3D;

public class Cube extends BaseObject3D {
	private float mSize;
	private boolean mIsSkybox;
	
	public Cube(float size) {
		super();
		mSize = size;
		mHasCubemapTexture = false;
		init();
	}
	
	public Cube(float size, boolean isSkybox) {
		super();
		mIsSkybox = isSkybox;
		mSize = size;
		mHasCubemapTexture = true;
		init();
	}
	
	public Cube(float size, boolean isSkybox, boolean hasCubemapTexture) {
		super();
		mIsSkybox = isSkybox;
		mSize = size;
		mHasCubemapTexture = hasCubemapTexture;
		init();
	}
	
	private void init()
	{
		float halfSize = mSize * .5f;
		float[] vertices = {
				halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize,-halfSize, halfSize, halfSize,-halfSize, halfSize, //0-1-halfSize-3 front
                halfSize, halfSize, halfSize, halfSize,-halfSize, halfSize,  halfSize,-halfSize,-halfSize, halfSize, halfSize,-halfSize,//0-3-4-5 right
                halfSize,-halfSize,-halfSize, -halfSize,-halfSize,-halfSize, -halfSize, halfSize,-halfSize, halfSize, halfSize,-halfSize,//4-7-6-5 back
                -halfSize, halfSize, halfSize, -halfSize, halfSize,-halfSize, -halfSize,-halfSize,-halfSize, -halfSize,-halfSize, halfSize,//1-6-7-halfSize left
                halfSize, halfSize, halfSize, halfSize, halfSize,-halfSize, -halfSize, halfSize,-halfSize, -halfSize, halfSize, halfSize, //top
                halfSize,-halfSize, halfSize, -halfSize,-halfSize, halfSize, -halfSize,-halfSize,-halfSize, halfSize,-halfSize,-halfSize,//bottom
                };
		
		float t = 1;
		
		float[] textureCoords = {
				0, 1,	1, 1,  	1, 0, 	0, 0, // front
				0, 1,	1, 1,  	1, 0, 	0, 0, // up
				0, 1,	1, 1,  	1, 0, 	0, 0, // back
				0, 1,	1, 1,  	1, 0, 	0, 0, // down
				0, 1,	1, 1,  	1, 0, 	0, 0, // right
				0, 1,	1, 1,  	1, 0, 	0, 0, // left
		};
		float[] skyboxTextureCoords = {
				-t,t,t, t,t,t, t,-t,t, -t,-t,t,     // front
				t,t,-t, t,-t,-t, t,-t,t, t,t,t,   // up
				-t,-t,-t, t,-t,-t, t,t,-t, -t,t,-t, // back
				-t,t,-t, -t,t,t, -t,-t,t,-t,-t,-t, // down
				-t,t,t, -t,t,-t, t,t,-t, t,t,t,   // right
				-t,-t,t, t,-t,t, t,-t,-t, -t,-t,-t,  // left
		};
		float[] skybox2TextureCoords = {
				.25f, .3333f,	.5f, .3333f,  	.5f, .6666f, 	.25f, .6666f, 	// front
				.25f, .3333f,	.25f, .6666f,	0, .6666f,		0, .3333f,		// left
				1, .6666f, 		.75f, .6666f,	.75f, .3333f,	1, .3333f,		// back
				.5f, .3333f,	.75f, .3333f,  	.75f, .6666f, 	.5f, .6666f, 	// right
				.25f, .3333f, 	.25f, 0,		.5f, 0,  		.5f, .3333f, 	// up
				.25f, .6666f,	.5f, .6666f,  	.5f, 1, 		.25f, 1 		// down
		};
		if(mIsSkybox && !mHasCubemapTexture)
			skyboxTextureCoords = skybox2TextureCoords;
		
		float[] colors = {
			1, 1, 1, 1, 	1, 1, 1, 1,		1, 1, 1, 1,		1, 1, 1, 1,
			1, 1, 1, 1,		1, 1, 1, 1,		1, 1, 1, 1, 	1, 1, 1, 1,
			1, 1, 1, 1, 	1, 1, 1, 1,		1, 1, 1, 1,		1, 1, 1, 1,
			1, 1, 1, 1,		1, 1, 1, 1,		1, 1, 1, 1, 	1, 1, 1, 1,
			1, 1, 1, 1, 	1, 1, 1, 1,		1, 1, 1, 1,		1, 1, 1, 1,
			1, 1, 1, 1,		1, 1, 1, 1,		1, 1, 1, 1, 	1, 1, 1, 1
		};
		
		float n = 1;
		
		float[] normals = {
				0, 0, n,   0, 0, n,   0, 0, n,   0, 0, n,     //front
                n, 0, 0,   n, 0, 0,   n, 0, 0,   n, 0, 0,     // right
                0, 0,-n,   0, 0,-n,   0, 0,-n,   0, 0,-n,     //back
                -n, 0, 0,  -n, 0, 0,  -n, 0, 0,  -n, 0, 0,     // left
                0, n, 0,   0, n, 0,   0, n, 0,   0, n, 0,     //  top                          
                0,-n, 0,   0,-n, 0,   0,-n, 0,   0,-n, 0,     // bottom
		};
		
		int[] indices = {
				0,1,2, 0,2,3,
				4,5,6, 4,6,7,
                8,9,10, 8,10,11,
                12,13,14, 12,14,15,
                16,17,18, 16,18,19,
                20,21,22, 20,22,23,
		};
		int[] skyboxIndices = {
				2,1,0, 3,2,0,
				6,5,4, 7,6,4,
				10,9,8, 11, 10, 8,
				14, 13, 12, 15, 14, 12,
				18, 17, 16, 19, 18, 16,
				22, 21, 20, 23, 22, 20
		};
		
		setData(vertices, normals, mIsSkybox ? skyboxTextureCoords : textureCoords, colors, mIsSkybox && mHasCubemapTexture ? skyboxIndices : indices);
	}
}
