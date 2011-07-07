package rajawali.primitives;


import rajawali.BaseObject3D;
import rajawali.materials.SimpleMaterial;
import rajawali.wallpaper.Wallpaper;
import android.util.Log;


public class Plane extends BaseObject3D {
	protected float mWidth;
	protected float mHeight;
	protected int mSegmentsW;
	protected int mSegmentsH;
	
	public Plane() {
		this(1f, 1f, 3, 3);
	}
	
	public Plane(float width, float height, int segmentsW, int segmentsH) {
		super();
		mWidth = width;
		mHeight = height;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		init();
	}
	
	private void init() {
		mMaterial = new SimpleMaterial();
		/*
		float[] vertices = new float[] {
			-.5f, -.5f, 0,
			.5f, -.5f, 0,
			-.5f, .5f, 0,
			.5f, .5f, 0	        
		};
		float[] textureCoords = new float[] {
			1f, 1f,
			0, 1f,
			1f, 0,
			0, 0
		};
		float[] normals = new float[] {
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f,
		};
		short[] indices = new short[] {
			0, 3, 1, 0, 2, 3
		};
		
		setData(vertices, normals, textureCoords, indices);*/
		
		
        int i, j;
        int numVertices = (mSegmentsW+1) * (mSegmentsH+1);
        float[] vertices = new float[numVertices * 3];
        float[] textureCoords = new float[numVertices * 2];
        float[] normals = new float[numVertices * 3];
        short[] indices = new short[mSegmentsW * mSegmentsH * 6];
        int vertexCount = 0;
        int texCoordCount = 0;
        
        for (i = 0; i <= mSegmentsW; ++i) {
            for (j = 0; j <= mSegmentsH; ++j) {
            	vertices[vertexCount] = ((float)i / (float)mSegmentsW - 0.5f) * mWidth;
            	vertices[vertexCount+1] = 0;
            	vertices[vertexCount+2] = ((float)j / (float)mSegmentsH - 0.5f) * mHeight;
            	
            	textureCoords[texCoordCount++] = (float)j / (float)mSegmentsW;
            	textureCoords[texCoordCount++] = 1f - (float)i / (float)mSegmentsH;
            	
            	normals[vertexCount] = 0;
            	normals[vertexCount+1] = 1;
            	normals[vertexCount+2] = 0;
            	
            	vertexCount += 3;
            }
        }
        

        int colspan = mSegmentsW + 1;
        int indexCount = 0;
        
        for(int row = 1; row <= mSegmentsH; row++)
        {
        	for (int col = 1; col <= mSegmentsW; col++)
        	{
        		int lr = row * colspan + col;
                int ll = lr - 1;
                int ur = lr - colspan;
                int ul = ur - 1;
                
                indices[indexCount++] = (short)ul;
                indices[indexCount++] = (short)ur;
                indices[indexCount++] = (short)lr;
                
                indices[indexCount++] = (short)ul;
                indices[indexCount++] = (short)lr;
                indices[indexCount++] = (short)ll;
        	}
        }
        
        Log.d(Wallpaper.TAG, "num verts: " + numVertices);
        Log.d(Wallpaper.TAG, "n4m v3rtz: " + vertexCount);
        Log.d(Wallpaper.TAG, "n@m 1nd1x: " + (mSegmentsW * mSegmentsH * 6));
        
        setData(vertices, normals, textureCoords, indices);
	}
}
