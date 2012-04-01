package rajawali.primitives;


import rajawali.BaseObject3D;
import rajawali.materials.SimpleMaterial;


public class Plane extends BaseObject3D {
	protected float mWidth;
	protected float mHeight;
	protected int mSegmentsW;
	protected int mSegmentsH;
	protected int mDirection;
	
	public Plane() {
		this(1f, 1f, 0, 3, 3);
	}
	
	public Plane(float width, float height, int segmentsW, int segmentsH) {
		this(width, height, 1, segmentsW, segmentsH);
	}
	
	public Plane(float width, float height, int direction, int segmentsW, int segmentsH) {
		super();
		mWidth = width;
		mHeight = height;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		mDirection = direction;
		init();
	}
	
	private void init() {
		mMaterial = new SimpleMaterial();
		
        int i, j;
        int numVertices = (mSegmentsW+1) * (mSegmentsH+1);
        float[] vertices = new float[numVertices * 3];
        float[] textureCoords = new float[numVertices * 2];
        float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
        short[] indices = new short[mSegmentsW * mSegmentsH * 6];
        int vertexCount = 0;
        int texCoordCount = 0;
        
        for (i = 0; i <= mSegmentsW; ++i) {
            for (j = 0; j <= mSegmentsH; ++j) {
            	vertices[vertexCount] = ((float)i / (float)mSegmentsW - 0.5f) * mWidth;
            	if(mDirection == 0) {
	            	vertices[vertexCount+1] = 0;
	            	vertices[vertexCount+2] = ((float)j / (float)mSegmentsH - 0.5f) * mHeight;
            	}
            	else {
	            	vertices[vertexCount+1] = ((float)j / (float)mSegmentsH - 0.5f) * mHeight;
	            	vertices[vertexCount+2] = 0;
            	}
            	
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
        
		int numColors = numVertices * 4;
		for(j = 0; j < numColors; j += 4 )
		{
			colors[ j ] = 1.0f;
			colors[ j + 1 ] = 1.0f;
			colors[ j + 2 ] = 0.0f;
			colors[ j + 3 ] = 1.0f;
		}
        
        setData(vertices, normals, textureCoords, colors, indices);
	}
}
