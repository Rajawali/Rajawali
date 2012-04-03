package rajawali;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Geometry3D {
	public static final int FLOAT_SIZE_BYTES = 4;
	public static final int SHORT_SIZE_BYTES = 2;
	
	protected FloatBuffer mVertices;
	protected FloatBuffer mNormals;
	protected FloatBuffer mTextureCoords;
	protected FloatBuffer mColors;
	protected ShortBuffer mIndices;
	protected int mNumIndices;
	protected int mNumVertices;
	protected String mName;
	
	public Geometry3D() {
		super();
		mName = "";
	}
	
	public Geometry3D(String name) {
		this();
		mName = name;
	}
	
	public void setData(float[] vertices, float[] normals,
			float[] textureCoords, float[] colors, short[] indices) {
		setVertices(vertices);
		setNormals(normals);
		setTextureCoords(textureCoords);
		if (colors != null)
			setColors(colors);	
		setIndices(indices);
	}

	public void setVertices(float[] vertices) {
		if(mVertices == null) {
			mVertices = ByteBuffer
					.allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			mVertices.put(vertices).position(0);
			mNumVertices = vertices.length / 3;
		} else {
			mVertices.put(vertices);
		}
	}
	
	public void setVertices(FloatBuffer vertices) {
		vertices.position(0);
		float[] v = new float[vertices.capacity()];
		vertices.get(v);
		setVertices(v);
	}
	
	public FloatBuffer getVertices() {
		return mVertices;
	}
	
	public void setNormals(float[] normals) {
		if(mNormals == null) {
			mNormals = ByteBuffer.allocateDirect(normals.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			mNormals.put(normals).position(0);
		} else {
			mNormals.put(normals);
		}
	}
	
	public void setNormals(FloatBuffer normals) {
		normals.position(0);
		float[] n = new float[normals.capacity()];
		normals.get(n);
		setNormals(n);
	}

	
	public FloatBuffer getNormals() {
		return mNormals;
	}
	
	public void setIndices(short[] indices) {
		if(mIndices == null) {
			mIndices = ByteBuffer.allocateDirect(indices.length * SHORT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asShortBuffer();
			mIndices.put(indices).position(0);
	
			mNumIndices = indices.length;
		} else {
			mIndices.put(indices);
		}
	}
	
	public ShortBuffer getIndices() {
		return mIndices;
	}
	
	public void setTextureCoords(float[] textureCoords) {
		if(mTextureCoords == null) {
			mTextureCoords = ByteBuffer
					.allocateDirect(textureCoords.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			mTextureCoords.put(textureCoords).position(0);
		} else {
			mTextureCoords.put(textureCoords);
		}
	}
	
	public FloatBuffer getTextureCoords() {
		return mTextureCoords;
	}
	
	public void setColors(float[] colors) {
		if(mColors == null) {
			mColors = ByteBuffer
					.allocateDirect(colors.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			mColors.put(colors).position(0);
		} else {
			mColors.put(colors);
		}
	}
	
	public FloatBuffer getColors() {
		return mColors;
	}
	
	public int getNumIndices() {
		return mNumIndices;
	}

	public int getNumVertices() {
		return mNumVertices;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public void setColor(float r, float g, float b, float a) {
		if(mColors == null || mColors.capacity() == 0)
			mColors = ByteBuffer.allocateDirect(mNumVertices * 4 * FLOAT_SIZE_BYTES)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		mColors.position(0);
		
		while(mColors.remaining() > 3) {
			mColors.put(r);
			mColors.put(g);
			mColors.put(b);
			mColors.put(a);
		}
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Geometry3D indices: ");
		buff.append(mIndices.capacity());
		buff.append(", vertices: ");
		buff.append(mVertices.capacity());
		buff.append(", normals: ");
		buff.append(mNormals.capacity());
		buff.append(", uvs: ");
		buff.append(mTextureCoords.capacity());
		//buff.append(", colors: ");
		//buff.append(mColors.capacity());
		return buff.toString();
	}
}
