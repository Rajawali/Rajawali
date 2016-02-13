package org.rajawali3d.primitives;

import org.rajawali3d.Object3D;

/**
 * A torus primitive (donut shape). The constructor takes two boolean arguments that indicate whether certain buffers should be
 * created or not. Not creating these buffers can reduce memory footprint.
 * <p>
 * When creating solid color torus both <code>createTextureCoordinates</code> and <code>createVertexColorBuffer</code>
 * can be set to <code>false</code>.
 * <p>
 * When creating a textured torus <code>createTextureCoordinates</code> should be set to <code>true</code> and
 * <code>createVertexColorBuffer</code> should be set to <code>false</code>.
 * <p>
 * When creating a torus without a texture but with different colors per texture <code>createTextureCoordinates</code>
 * should be set to <code>false</code> and <code>createVertexColorBuffer</code> should be set to <code>true</code>.
 *
 */
public class Torus extends Object3D {

	private final float PI = (float) Math.PI;
	private float mLargeRadius;
	private float mSmallRadius;
	private int mSegmentsL;
	private int mSegmentsS;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;

	/**
	 * Creates a torus primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 *
	 * @param largeRadius
	 *			The large radius of the torus
	 * @param smallRadius
	 *			The small radius of the torus
	 * @param segmentsL
	 *			The number of segments on the large radius
	 * @param segmentsS
	 *			The number of segments on the small radius
	 */
	public Torus(float largeRadius, float smallRadius, int segmentsL, int segmentsS) {
		this(largeRadius, smallRadius, segmentsL, segmentsS, true, false, true);
	}

	/**
	 * Creates a torus primitive.
	 *
	 * @param largeRadius
	 *			The large radius of the dount
	 * @param smallRadius
	 *			The small radius of the dount
	 * @param segmentsL
	 *			The number of segments on the large radius
	 * @param segmentsS
	 *			The number of segments on the small radius
	 * @param createTextureCoordinates
	 *			A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *			A boolean that indicates whether a vertex color buffer should be created or not.
	 * @param createVBOs
	 *			A boolean that indicates whether the VBOs should be created immediately.
	 */
	public Torus(float largeRadius, float smallRadius, int segmentsL, int segmentsS, boolean createTextureCoordinates,
				 boolean createVertexColorBuffer, boolean createVBOs) {
		super();
		mLargeRadius = largeRadius;
		mSmallRadius = smallRadius;
		mSegmentsL = segmentsL;
		mSegmentsS = segmentsS;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		init(createVBOs);
	}

	protected void init(boolean createVBOs) {
		int numVertices = (mSegmentsS + 1) * (mSegmentsL + 1);
		int numIndices = 2 * mSegmentsS * mSegmentsL * 3;

		float[] vertices = new float[numVertices * 3];
		float[] normals = new float[numVertices * 3];
		int[] indices = new int[numIndices];

		int i, j;
		int vertIndex = 0, index = 0;
		final float normLen = 1.0f / mSmallRadius;

		for (j = 0; j <= mSegmentsL; ++j) {
			float largeRadiusAngle = 2.0f * PI * j / mSegmentsL;

			for (i = 0; i <= mSegmentsS; ++i) {
				float smallRadiusAngle = 2.0f * PI * i / mSegmentsS;
				float xNorm = (mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.sin(largeRadiusAngle);
				float x = (mLargeRadius + mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.sin(largeRadiusAngle);
				float yNorm = (mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.cos(largeRadiusAngle);
				float y = (mLargeRadius + mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.cos(largeRadiusAngle);
				float zNorm = mSmallRadius * (float) Math.cos(smallRadiusAngle);
				float z = zNorm;
				normals[vertIndex] = xNorm * normLen;
				vertices[vertIndex++] = x;
				normals[vertIndex] = yNorm * normLen;
				vertices[vertIndex++] = y;
				normals[vertIndex] = zNorm * normLen;
				vertices[vertIndex++] = z;

				if (i > 0 && j > 0) {
					int a = (mSegmentsS + 1) * j + i;
					int b = (mSegmentsS + 1) * j + i - 1;
					int c = (mSegmentsS + 1) * (j - 1) + i - 1;
					int d = (mSegmentsS + 1) * (j - 1) + i;

					indices[index++] = a;
					indices[index++] = c;
					indices[index++] = b;
					indices[index++] = a;
					indices[index++] = d;
					indices[index++] = c;
				}
			}
		}

		float[] textureCoords = null;
		if (mCreateTextureCoords) {
			int numUvs = (mSegmentsL + 1) * (mSegmentsS + 1) * 2;
			textureCoords = new float[numUvs];

			numUvs = 0;
			for (j = 0; j <= mSegmentsL; ++j) {
				for (i = mSegmentsS; i >= 0; --i) {
					textureCoords[numUvs++] = (float) i / mSegmentsS;
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
