package rajawali.primitives;


import rajawali.BaseObject3D;


public class Plane extends BaseObject3D {
	protected float mWidth;
	protected float mHeight;
	protected int mSegmentsW;
	protected int mSegmentsH;
	protected int mDirection;
	protected boolean mRotatedTexCoords;
	
	public Plane() {
		this(1f, 1f, 0, 3, 3);
	}
	
	public Plane(float width, float height, int segmentsW, int segmentsH) {
		this(width, height, 1, segmentsW, segmentsH);
	}
	
	public Plane(float width, float height, int direction, int segmentsW, int segmentsH, boolean rotatedTexCoords) {
		super();
		mWidth = width;
		mHeight = height;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		mDirection = direction;
		mRotatedTexCoords = rotatedTexCoords;
		init();
	}
	
	public Plane(float width, float height, int direction, int segmentsW, int segmentsH) {
		this(width, height, direction, segmentsW, segmentsH, false);
	}
	
	private void init() {
        int i, j;
        int numVertices = (mSegmentsW+1) * (mSegmentsH+1);
        float[] vertices = new float[numVertices * 3];
        float[] textureCoords = new float[numVertices * 2];
        float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
        int[] indices = new int[mSegmentsW * mSegmentsH * 6];
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
            	
            	float t1 = (float)j / (float)mSegmentsW;
            	float t2 = 1f - (float)i / (float)mSegmentsH;
            	
            	textureCoords[texCoordCount++] = mRotatedTexCoords ? t2 : t1;
            	textureCoords[texCoordCount++] = mRotatedTexCoords ? 1.0f - t1 : t2;
            	
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
                
                indices[indexCount++] = (int)ul;
                indices[indexCount++] = (int)ur;
                indices[indexCount++] = (int)lr;
                
                indices[indexCount++] = (int)ul;
                indices[indexCount++] = (int)lr;
                indices[indexCount++] = (int)ll;
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
