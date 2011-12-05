package rajawali;

import java.io.Serializable;

public class SerializedObject3D implements Serializable {
	private static final long serialVersionUID = 5264861128471177349L;

	protected float[] mVertices;
	protected float[] mNormals;
	protected float[] mTextureCoords;
	protected float[] mColors;
	protected short[] mIndices;
	
	public SerializedObject3D(int numVertices, int numNormals, int numTextureCoords, int numColors, int numIndices) {
		mVertices = new float[numVertices];
		mNormals = new float[numNormals];
		mTextureCoords = new float[numTextureCoords];
		mColors = new float[numColors];
		mIndices = new short[numIndices];
	}
	
	public float[] getVertices() {
		return mVertices;
	}
	public void setVertices(float[] vertices) {
		this.mVertices = vertices;
	}
	public float[] getNormals() {
		return mNormals;
	}
	public void setNormals(float[] normals) {
		this.mNormals = normals;
	}
	public float[] getTextureCoords() {
		return mTextureCoords;
	}
	public void setTextureCoords(float[] textureCoords) {
		this.mTextureCoords = textureCoords;
	}
	public short[] getIndices() {
		return mIndices;
	}
	public void setIndices(short[] indices) {
		this.mIndices = indices;
	}

	public float[] getColors() {
		return mColors;
	}

	public void setColors(float[] colors) {
		this.mColors = colors;
	}
}
