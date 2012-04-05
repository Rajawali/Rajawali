package rajawali;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Color;
import android.opengl.GLES20;

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
	
	protected int mVertexBufferHandle;
	protected int mIndexBufferHandle;
	protected int mTexCoordBufferHandle;
	protected int mColorBufferHandle;
	protected int mNormalBufferHandle;
	
	protected enum BufferType {
		FLOAT_BUFFER,
		SHORT_BUFFER
	}
	
	public Geometry3D() {
		super();
		mName = "";
	}
	
	public Geometry3D(String name) {
		this();
		mName = name;
	}
	
	public void copyFromGeometry3D(Geometry3D geom) {
		this.mName = geom.getName();
		this.mNumIndices = geom.getNumIndices();
		this.mNumVertices = geom.getNumVertices();
		this.mVertexBufferHandle = geom.getVertexBufferHandle();
		this.mIndexBufferHandle = geom.getIndexBufferHandle();
		this.mTexCoordBufferHandle = geom.getTexCoordBufferHandle();
		this.mColorBufferHandle = geom.getColorBufferHandle();
		this.mNormalBufferHandle = geom.getNormalBufferHandle();
	}
	
	public void setData(int vertexBufferHandle, int normalBufferHandle,
			float[] textureCoords, float[] colors, short[] indices) {
		if(textureCoords == null || textureCoords.length == 0)
			textureCoords = new float[(mNumVertices / 3) * 2];
		setTextureCoords(textureCoords);
		if(colors == null || colors.length == 0)
			setColors(0xff000000 + (int)(Math.random() * 0xffffff));
		else
			setColors(colors);	
		setIndices(indices);
		
		mVertexBufferHandle = vertexBufferHandle;
		mNormalBufferHandle = normalBufferHandle;
		
		mTextureCoords.compact().position(0);
		mColors.compact().position(0);
		mIndices.compact().position(0);
		
		mTexCoordBufferHandle 	= createBuffer(BufferType.FLOAT_BUFFER, mTextureCoords, GLES20.GL_ARRAY_BUFFER);
		mColorBufferHandle 		= createBuffer(BufferType.FLOAT_BUFFER, mColors,		GLES20.GL_ARRAY_BUFFER);
		mIndexBufferHandle 		= createBuffer(BufferType.SHORT_BUFFER, mIndices,		GLES20.GL_ELEMENT_ARRAY_BUFFER);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        
        mTextureCoords.limit(0);	mTextureCoords = null;
        mColors.limit(0);			mColors = null;
        mIndices.limit(0);			mIndices = null;
	}
	
	public void setData(float[] vertices, float[] normals,
			float[] textureCoords, float[] colors, short[] indices) {
		setVertices(vertices);
		setNormals(normals);
		if(textureCoords == null || textureCoords.length == 0)
			textureCoords = new float[(vertices.length / 3) * 2];
		
		setTextureCoords(textureCoords);
		if(colors == null || colors.length == 0)
			setColors(0xff000000 + (int)(Math.random() * 0xffffff));
		else
			setColors(colors);	
		setIndices(indices);

		createBuffers();
	}
	
	public void createBuffers() {
		mVertices.compact().position(0);
		mNormals.compact().position(0);
		mTextureCoords.compact().position(0);
		mColors.compact().position(0);
		mIndices.compact().position(0);
		
		mVertexBufferHandle 	= createBuffer(BufferType.FLOAT_BUFFER, mVertices,		GLES20.GL_ARRAY_BUFFER);
		mNormalBufferHandle 	= createBuffer(BufferType.FLOAT_BUFFER, mNormals,		GLES20.GL_ARRAY_BUFFER);
		mTexCoordBufferHandle 	= createBuffer(BufferType.FLOAT_BUFFER, mTextureCoords, GLES20.GL_ARRAY_BUFFER);
		mColorBufferHandle 		= createBuffer(BufferType.FLOAT_BUFFER, mColors,		GLES20.GL_ARRAY_BUFFER);
		mIndexBufferHandle 		= createBuffer(BufferType.SHORT_BUFFER, mIndices,		GLES20.GL_ELEMENT_ARRAY_BUFFER);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        
        mVertices.limit(0);			mVertices = null;
        mNormals.limit(0);			mNormals = null;
        mTextureCoords.limit(0);	mTextureCoords = null;
        mColors.limit(0);			mColors = null;
        mIndices.limit(0);			mIndices = null;
	}
	
	public void createVertexAndNormalBuffersOnly() {
		mVertices.compact().position(0);
		mNormals.compact().position(0);
		
		mVertexBufferHandle 	= createBuffer(BufferType.FLOAT_BUFFER, mVertices,		GLES20.GL_ARRAY_BUFFER);
		mNormalBufferHandle 	= createBuffer(BufferType.FLOAT_BUFFER, mNormals,		GLES20.GL_ARRAY_BUFFER);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mVertices.limit(0);			mVertices = null;
        mNormals.limit(0);			mNormals = null;
	}
	
	protected int createBuffer(BufferType type, Buffer buffer, int target) {
		int buff[] = new int[1];
		GLES20.glGenBuffers(1, buff, 0);
		int handle = buff[0];
		int byteSize = type == BufferType.FLOAT_BUFFER ? 4 : 2;
		
		GLES20.glBindBuffer(target, handle);
		GLES20.glBufferData(target, buffer.limit() * byteSize, buffer, GLES20.GL_STATIC_DRAW);
		return handle;		
	}

	public void setVertices(float[] vertices) {
		setVertices(vertices, false);
	}
	
	public void setVertices(float[] vertices, boolean override) {
		if(mVertices == null || override == true) {
			if(mVertices != null) {
				mVertices.clear();
			}
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
			mNormals.position(0);
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
	
	public void setColors(int color) {
		setColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
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
		setColor(r, g, b, a, false);
	}
	
	public void setColor(float r, float g, float b, float a, boolean createNewBuffer) {
		if(mColors == null || mColors.limit() == 0)
			mColors = ByteBuffer.allocateDirect(mNumVertices * 4 * FLOAT_SIZE_BYTES)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		mColors.position(0);
		
		while(mColors.remaining() > 3) {
			mColors.put(r);
			mColors.put(g);
			mColors.put(b);
			mColors.put(a);
		}
		mColors.position(0);
		
		if(createNewBuffer == true) {
			mColorBufferHandle 	= createBuffer(BufferType.FLOAT_BUFFER, mColors,		GLES20.GL_ARRAY_BUFFER);
		}
		
		if(mColorBufferHandle > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mColorBufferHandle);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mColors.limit() * FLOAT_SIZE_BYTES, mColors, GLES20.GL_STATIC_DRAW);
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

	public int getVertexBufferHandle() {
		return mVertexBufferHandle;
	}

	public void setVertexBufferHandle(int vertexBufferHandle) {
		this.mVertexBufferHandle = vertexBufferHandle;
	}

	public int getIndexBufferHandle() {
		return mIndexBufferHandle;
	}

	public void setIndexBufferHandle(int indexBufferHandle) {
		this.mIndexBufferHandle = indexBufferHandle;
	}

	public int getTexCoordBufferHandle() {
		return mTexCoordBufferHandle;
	}

	public void setTexCoordBufferHandle(int texCoordBufferHandle) {
		this.mTexCoordBufferHandle = texCoordBufferHandle;
	}

	public int getColorBufferHandle() {
		return mColorBufferHandle;
	}

	public void setColorBufferHandle(int colorBufferHandle) {
		this.mColorBufferHandle = colorBufferHandle;
	}

	public int getNormalBufferHandle() {
		return mNormalBufferHandle;
	}

	public void setNormalBufferHandle(int normalBufferHandle) {
		this.mNormalBufferHandle = normalBufferHandle;
	}
}
