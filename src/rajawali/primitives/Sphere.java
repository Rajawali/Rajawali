package rajawali.primitives;

import rajawali.BaseObject3D;
import rajawali.materials.DiffuseMaterial;


public class Sphere extends BaseObject3D {
	private final float PI = 3.14159265f;
	private float mRadius;
	private int mSegmentsW;
	private int mSegmentsH;
	
	public Sphere(float radius, int segmentsW, int segmentsH) {
		super();
		mRadius = radius;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		init();
	}
	
	protected void init() {
		mMaterial = new DiffuseMaterial();
		//mShader = new SimpleShader();
		int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
		int numIndices = 2 * mSegmentsW * (mSegmentsH - 1) * 3;
		
		float[] vertices = new float[numVertices * 3];
		float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
		short[] indices = new short[numIndices]; 
		
		int i, j;
		int vertIndex = 0, index = 0;
		
		for(j=0; j<=mSegmentsH; ++j) {
			float horAngle = PI * j / mSegmentsH;
			float z = -mRadius * (float)Math.cos(horAngle);
			float ringRadius = mRadius * (float)Math.sin(horAngle);
			
			for(i=0; i<=mSegmentsW; ++i) {
				float verAngle = 2.0f * PI * i / mSegmentsW;
				float x = ringRadius * (float)Math.cos(verAngle);
				float y = ringRadius * (float)Math.sin(verAngle);
				float normLen = 1.0f / (float)Math.sqrt(x*x+y*y+z*z);
				
				normals[vertIndex] = x * normLen;
				vertices[vertIndex++] = x;
				normals[vertIndex] = -z * normLen;
				vertices[vertIndex++] = -z;
				normals[vertIndex] = y * normLen;
				vertices[vertIndex++] = y;
				
				if(i > 0 && j > 0) {
					int a = (mSegmentsW + 1) * j + i;
					int b = (mSegmentsW + 1) * j + i - 1;
					int c = (mSegmentsW + 1) * (j - 1) + i - 1;
					int d = (mSegmentsW + 1) * (j - 1) + i;
					
					if(j == mSegmentsH) {
						indices[index++] = (short)a;
						indices[index++] = (short)c;
						indices[index++] = (short)d;
					} else if(j==1) {
						indices[index++] = (short)a;
						indices[index++] = (short)b;
						indices[index++] = (short)c;
					} else {
						indices[index++] = (short)a;
						indices[index++] = (short)b;
						indices[index++] = (short)c;
						indices[index++] = (short)a;
						indices[index++] = (short)c;
						indices[index++] = (short)d;
					}
				}
			}
		}
		
		int numUvs = (mSegmentsH + 1) * (mSegmentsW + 1) * 2;
		float[] textureCoords = new float[numUvs];

		numUvs = 0;
		for (j = 0; j <= mSegmentsH; ++j) {
			for (i = 0; i <= mSegmentsW; ++i) {
				textureCoords[numUvs++] = -(float)i / mSegmentsW;
				textureCoords[numUvs++] = (float)j / mSegmentsH;
			}
		}
		
		int numColors = numVertices * 4;
		for(j = 0; j < numColors; j += 4 )
		{
			colors[ j ] = 1.0f;
			colors[ j + 1 ] = 0;
			colors[ j + 2 ] = 0;
			colors[ j + 3 ] = 1.0f;
		}

		setData(vertices, normals, textureCoords, colors, indices);
	}
}
