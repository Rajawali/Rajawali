package rajawali.primitives;

import rajawali.BaseObject3D;

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
public class Plane extends BaseObject3D {

	protected float mWidth;
	protected float mHeight;
	protected int mSegmentsW;
	protected int mSegmentsH;
	private boolean mCreateTextureCoords;
	private boolean mCreateVertexColorBuffer;

	/**
	 * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
	 */
	public Plane() {
		this(1f, 1f, 1, 1, true, false);
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
	 */
	public Plane(float width, float height, int segmentsW, int segmentsH)
	{
		this(width, height, segmentsW, segmentsH, true, false);
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
	 * @param createTextureCoordinates
	 *            A boolean that indicates whether the texture coordinates should be calculated or not.
	 * @param createVertexColorBuffer
	 *            A boolean that indicates whether a vertex color buffer should be created or not.
	 */
	public Plane(float width, float height, int segmentsW, int segmentsH, boolean createTextureCoordinates,
			boolean createVertexColorBuffer) {
		super();
		mWidth = width;
		mHeight = height;
		mSegmentsW = segmentsW;
		mSegmentsH = segmentsH;
		mCreateTextureCoords = createTextureCoordinates;
		mCreateVertexColorBuffer = createVertexColorBuffer;
		init();
	}

	private void init() {
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
				vertices[vertexCount] = ((float) i / (float) mSegmentsW - 0.5f) * mWidth;
				vertices[vertexCount + 1] = ((float) j / (float) mSegmentsH - 0.5f) * mHeight;
				vertices[vertexCount + 2] = 0;

				if (mCreateTextureCoords) {
					textureCoords[texCoordCount++] = (float) i / (float) mSegmentsW;
					textureCoords[texCoordCount++] = 1.0f - (float) j / (float) mSegmentsH;
				}

				normals[vertexCount] = 0;
				normals[vertexCount + 1] = 0;
				normals[vertexCount + 2] = 1;

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

				indices[indexCount++] = (int) ul;
				indices[indexCount++] = (int) ur;
				indices[indexCount++] = (int) lr;

				indices[indexCount++] = (int) ul;
				indices[indexCount++] = (int) lr;
				indices[indexCount++] = (int) ll;
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

		setData(vertices, normals, textureCoords, colors, indices);
		
		vertices = null;
		normals = null;
		textureCoords = null;
		colors = null;
		indices = null;
	}
}
