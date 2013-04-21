package rajawali.primitives;

import rajawali.BaseObject3D;

public class Plane extends BaseObject3D {

	protected float mWidth;
	protected float mHeight;
	protected int mSegmentsW;
	protected int mSegmentsH;

	public Plane() {
		this(1f, 1f, 1, 1);
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
		int i, j;
		int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
		float[] vertices = new float[numVertices * 3];
		float[] textureCoords = new float[numVertices * 2];
		float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
		int[] indices = new int[mSegmentsW * mSegmentsH * 6];
		int vertexCount = 0;
		int texCoordCount = 0;

		for (i = 0; i <= mSegmentsW; i++) {
			for (j = 0; j <= mSegmentsH; j++) {
				vertices[vertexCount] = ((float) i / (float) mSegmentsW - 0.5f) * mWidth;
				vertices[vertexCount + 1] = ((float) j / (float) mSegmentsH - 0.5f) * mHeight;
				vertices[vertexCount + 2] = 0;

				textureCoords[texCoordCount++] = (float) i / (float) mSegmentsW;
				textureCoords[texCoordCount++] = 1.0f - (float) j / (float) mSegmentsH;

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

		int numColors = numVertices * 4;
		for (j = 0; j < numColors; j += 4)
		{
			colors[j] = 1.0f;
			colors[j + 1] = 1.0f;
			colors[j + 2] = 1.0f;
			colors[j + 3] = 1.0f;
		}

		setData(vertices, normals, textureCoords, colors, indices);
	}
}
