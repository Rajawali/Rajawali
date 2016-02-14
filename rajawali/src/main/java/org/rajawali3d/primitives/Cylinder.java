package org.rajawali3d.primitives;

import org.rajawali3d.Object3D;

/**
 * A cylinder primitive. The constructor takes two boolean arguments that indicate whether certain buffers should be
 * created or not. Not creating these buffers can reduce memory footprint.
 * <p>
 * When creating solid color cylinder both <code>createTextureCoordinates</code> and <code>createVertexColorBuffer</code>
 * can be set to <code>false</code>.
 * <p>
 * When creating a textured cylinder <code>createTextureCoordinates</code> should be set to <code>true</code> and
 * <code>createVertexColorBuffer</code> should be set to <code>false</code>.
 * <p>
 * When creating a cylinder without a texture but with different colors per texture <code>createTextureCoordinates</code>
 * should be set to <code>false</code> and <code>createVertexColorBuffer</code> should be set to <code>true</code>.
 *
 */
public class Cylinder extends Object3D {

	private final float PI = (float) Math.PI;
	private float mLength;
	private float mRadius;
	private int mSegmentsC;
	private int mSegmentsL;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;

	/**
	 * Creates a cylinder primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 *
	 * @param length
	 *			The length of the cylinder
	 * @param radius
	 *			The radius of the cylinder
	 * @param segmentsL
	 *			The number of length segments
	 * @param segmentsC
	 *			The number of circle segments
	 */
	public Cylinder(float length, float radius, int segmentsL, int segmentsC) {
		this(length, radius, segmentsL, segmentsC, true, false, true);
	}

	/**
	 * Creates a cylinder primitive.
	 *
	 * @param length
	 *			The length of the cylinder
	 * @param radius
	 *			The radius of the cylinder
	 * @param segmentsL
	 *			The number of length segments
	 * @param segmentsC
	 *			The number of circle segments
	 * @param createTextureCoordinates
	 *			A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *			A boolean that indicates whether a vertex color buffer should be created or not.
	 * @param createVBOs
	 *			A boolean that indicates whether the VBOs should be created immediately.
	 */
	public Cylinder(float length, float radius, int segmentsL, int segmentsC, boolean createTextureCoordinates,
					boolean createVertexColorBuffer, boolean createVBOs) {
		super();
		mLength = length;
		mRadius = radius;
		mSegmentsL = segmentsL;
		mSegmentsC = segmentsC;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		init(createVBOs);
	}

	protected void init(boolean createVBOs) {
		int numVertices = (mSegmentsC + 1) * (mSegmentsL + 1);
		int numIndices = 2 * mSegmentsC * mSegmentsL * 3;

		float[] vertices = new float[numVertices * 3];
		float[] normals = new float[numVertices * 3];
		int[] indices = new int[numIndices];

		int i, j;
		int vertIndex = 0, index = 0;
		final float normLen = 1.0f / mRadius;

		for (j = 0; j <= mSegmentsL; ++j) {
			float z = mLength * ((float)j/(float)mSegmentsL) - mLength / 2.0f;

			for (i = 0; i <= mSegmentsC; ++i) {
				float verAngle = 2.0f * PI * i / mSegmentsC;
				float x = mRadius * (float) Math.cos(verAngle);
				float y = mRadius * (float) Math.sin(verAngle);

				normals[vertIndex] = x * normLen;
				vertices[vertIndex++] = x;
				normals[vertIndex] = y * normLen;
				vertices[vertIndex++] = y;
				normals[vertIndex] = 0;
				vertices[vertIndex++] = z;
				if (i > 0 && j > 0) {
					int a = (mSegmentsC + 1) * j + i;
					int b = (mSegmentsC + 1) * j + i - 1;
					int c = (mSegmentsC + 1) * (j - 1) + i - 1;
					int d = (mSegmentsC + 1) * (j - 1) + i;
					indices[index++] = a;
					indices[index++] = b;
					indices[index++] = c;
					indices[index++] = a;
					indices[index++] = c;
					indices[index++] = d;
				}
			}
		}

		float[] textureCoords = null;
		if (mCreateTextureCoords) {
			int numUvs = (mSegmentsL + 1) * (mSegmentsC + 1) * 2;
			textureCoords = new float[numUvs];

			numUvs = 0;
			for (j = 0; j <= mSegmentsL; ++j) {
				for (i = mSegmentsC; i >= 0; --i) {
					textureCoords[numUvs++] = (float) i / mSegmentsC;
					textureCoords[numUvs++] = (float) j / mSegmentsL;
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
