package rajawali;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import rajawali.lights.ALight;
import rajawali.materials.AMaterial;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

public class BaseObject3D implements IObject3D, Comparable<BaseObject3D> {
	public static String TAG = "AlbumCoverWallpaper";

	protected final int FLOAT_SIZE_BYTES = 4;
	protected final int SHORT_SIZE_BYTES = 2;

	protected Number3D mPosition, mRotation, mScale;

	protected float[] mMVPMatrix = new float[16];
	protected float[] mMMatrix = new float[16];
	protected float[] mProjMatrix;

	protected float[] mScalematrix = new float[16];
	protected float[] mTranslateMatrix = new float[16];
	protected float[] mRotateMatrix = new float[16];
	protected float[] mRotateMatrixTmp = new float[16];
	protected float[] mTmpMatrix = new float[16];

	protected FloatBuffer mVertices;
	protected FloatBuffer mNormals;
	protected FloatBuffer mTextureCoords;
	protected FloatBuffer mColors;
	protected ShortBuffer mIndices;
	protected int mNumIndices;
	protected int mNumVertices;

	protected AMaterial mMaterial;
	protected ALight mLight;
	protected float mAlpha;

	protected ArrayList<BaseObject3D> mChildren;
	protected int mNumChildren;
	protected String mName;

	protected boolean mDoubleSided = false;
	protected boolean mTransparent = false;
	protected boolean mForcedDepth = false;
	protected boolean mHasCubemapTexture = false;
	protected int mDrawingMode = GLES20.GL_TRIANGLES;

	protected boolean mIsContainerOnly = true;

	public BaseObject3D() {
		mChildren = new ArrayList<BaseObject3D>();
		mPosition = new Number3D();
		mRotation = new Number3D();
		mScale = new Number3D(1, 1, 1);
	}

	public BaseObject3D(String name) {
		this();
		mName = name;
	}

	public BaseObject3D(SerializedObject3D ser) {
		this();
		setData(ser.getVertices(), ser.getNormals(), ser.getTextureCoords(),
				ser.getColors(), ser.getIndices());
	}

	public void setData(float[] vertices, float[] normals,
			float[] textureCoords, float[] colors, short[] indices) {
		mVertices = ByteBuffer
				.allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mVertices.put(vertices).position(0);

		mNormals = ByteBuffer.allocateDirect(normals.length * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mNormals.put(normals).position(0);

		mTextureCoords = ByteBuffer
				.allocateDirect(textureCoords.length * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTextureCoords.put(textureCoords).position(0);

		if (colors != null) {
			mColors = ByteBuffer
					.allocateDirect(colors.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			mColors.put(colors).position(0);
		}

		mIndices = ByteBuffer.allocateDirect(indices.length * SHORT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asShortBuffer();
		mIndices.put(indices).position(0);

		mNumIndices = indices.length;
		mNumVertices = vertices.length / 3;

		mIsContainerOnly = false;
	}

	public void render(Camera3D camera, float[] projMatrix, float[] vMatrix) {
		render(camera, projMatrix, vMatrix, null);
	}

	public void render(Camera3D camera, float[] projMatrix, float[] vMatrix,
			final float[] parentMatrix) {
		if (!mIsContainerOnly) {
			mProjMatrix = projMatrix;
			if (!mDoubleSided)
				GLES20.glEnable(GLES20.GL_CULL_FACE);
			if (mTransparent) {
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthMask(false);
			} else {
				GLES20.glDisable(GLES20.GL_BLEND);
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthMask(true);
			}

			mMaterial.useProgram();
			mMaterial.bindTextures();
			mMaterial.setCamera(camera);
			mMaterial.setVertices(mVertices);
			mMaterial.setTextureCoords(mTextureCoords, mHasCubemapTexture);
			mMaterial.setColors(mColors);
			mMaterial.setNormals(mNormals);

			setShaderParams();
		}

		doTransformations();

		Matrix.setIdentityM(mMMatrix, 0);
		Matrix.setIdentityM(mScalematrix, 0);
		Matrix.scaleM(mScalematrix, 0, mScale.x, mScale.y, mScale.z);

		Matrix.setIdentityM(mRotateMatrix, 0);

		rotateM(mRotateMatrix, 0, mRotation.x, 1.0f, 0.0f, 0.0f);
		rotateM(mRotateMatrix, 0, mRotation.y, 0.0f, 1.0f, 0.0f);
		rotateM(mRotateMatrix, 0, mRotation.z, 0.0f, 0.0f, 1.0f);

		Matrix.translateM(mMMatrix, 0, mPosition.x, mPosition.y, mPosition.z);
		Matrix.setIdentityM(mTmpMatrix, 0);
		Matrix.multiplyMM(mTmpMatrix, 0, mMMatrix, 0, mScalematrix, 0);
		Matrix.multiplyMM(mMMatrix, 0, mTmpMatrix, 0, mRotateMatrix, 0);
		if (parentMatrix != null) {
			Matrix.multiplyMM(mTmpMatrix, 0, parentMatrix, 0, mMMatrix, 0);
			System.arraycopy(mTmpMatrix, 0, mMMatrix, 0, 16);
		}
		Matrix.multiplyMM(mMVPMatrix, 0, vMatrix, 0, mMMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, projMatrix, 0, mMVPMatrix, 0);

		if (!mIsContainerOnly) {
			mMaterial.setMVPMatrix(mMVPMatrix);
			mMaterial.setModelMatrix(mMMatrix);
			mMaterial.setViewMatrix(vMatrix);

			mIndices.position(0);
			GLES20.glDrawElements(mDrawingMode, mNumIndices,
					GLES20.GL_UNSIGNED_SHORT, mIndices);

			GLES20.glDisable(GLES20.GL_CULL_FACE);
			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		}

		int i;
		for (i = 0; i < mNumChildren; ++i) {
			mChildren.get(i).render(camera, projMatrix, vMatrix, mMMatrix);
		}
	}

	protected void rotateM(float[] m, int mOffset, float a, float x, float y,
			float z) {
		Matrix.setIdentityM(mRotateMatrixTmp, 0);
		Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
		System.arraycopy(m, 0, mTmpMatrix, 0, 16);
		Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
	}

	protected void setShaderParams() {
		mMaterial.setLight(mLight);
	};

	protected void doTransformations() {
	}

	public void addTexture(TextureInfo textureInfo) {
		mMaterial.addTexture(textureInfo);
	}

	protected void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {

			Log.e(TAG, op + ": glError " + error + " in class "
					+ this.getClass().getName());
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	public void setPosition(float x, float y, float z) {
		mPosition.x = x;
		mPosition.y = y;
		mPosition.z = z;
	}

	public void setScreenCoordinates(float x, float y, int viewportWidth,
			int viewportHeight, float eyeZ) {
		float[] r1 = new float[16];
		int[] viewport = new int[] { 0, 0, viewportWidth, viewportHeight };
		float[] modelMatrix = new float[16];
		Matrix.setIdentityM(modelMatrix, 0);

		GLU.gluUnProject(x, viewportHeight - y, 0.0f, modelMatrix, 0,
				mProjMatrix, 0, viewport, 0, r1, 0);
		setPosition(r1[0] * eyeZ, r1[1] * -eyeZ, 0);
	}

	public float[] getModelMatrix() {
		return mMMatrix;
	}

	public void setX(float x) {
		mPosition.x = x;
	}

	public float getX() {
		return mPosition.x;
	}

	public void setY(float y) {
		mPosition.y = y;
	}

	public float getY() {
		return mPosition.y;
	}

	public void setZ(float z) {
		mPosition.z = z;
	}

	public float getZ() {
		return mPosition.z;
	}

	public void setRotation(float rotX, float rotY, float rotZ) {
		mRotation.x = rotX;
		mRotation.y = rotY;
		mRotation.z = rotZ;
	}

	public void setRotX(float rotX) {
		mRotation.x = rotX;
	}

	public float getRotX() {
		return mRotation.x;
	}

	public void setRotY(float rotY) {
		mRotation.y = rotY;
	}

	public float getRotY() {
		return mRotation.y;
	}

	public void setRotZ(float rotZ) {
		mRotation.z = rotZ;
	}

	public float getRotZ() {
		return mRotation.z;
	}

	public void setScale(float scale) {
		mScale.x = scale;
		mScale.y = scale;
		mScale.z = scale;
	}

	public void setScale(float scaleX, float scaleY, float scaleZ) {
		mScale.x = scaleX;
		mScale.y = scaleY;
		mScale.z = scaleZ;
	}

	public void setScaleX(float scaleX) {
		mScale.x = scaleX;
	}

	public float getScaleX() {
		return mScale.x;
	}

	public void setScaleY(float scaleY) {
		mScale.y = scaleY;
	}

	public float getScaleY() {
		return mScale.y;
	}

	public void setScaleZ(float scaleZ) {
		mScale.z = scaleZ;
	}

	public float getScaleZ() {
		return mScale.z;
	}

	public boolean isDoubleSided() {
		return mDoubleSided;
	}

	public void setDoubleSided(boolean doubleSided) {
		this.mDoubleSided = doubleSided;
	}

	public boolean isTransparent() {
		return mTransparent;
	}

	public void setTransparent(boolean transparent) {
		this.mTransparent = transparent;
	}

	public void setLight(ALight light) {
		mLight = light;
	}

	public ALight getLight() {
		return mLight;
	}

	public int getDrawingMode() {
		return mDrawingMode;
	}

	public void setDrawingMode(int drawingMode) {
		this.mDrawingMode = drawingMode;
	}

	public int compareTo(BaseObject3D another) {
		if (mForcedDepth)
			return -1;
		if (mPosition.z < another.getZ())
			return 1;
		else if (mPosition.z > another.getZ())
			return -1;
		else
			return 0;
	}

	public void addChild(BaseObject3D child) {
		mChildren.add(child);
		mNumChildren++;
	}

	public int getNumChildren() {
		return mNumChildren;
	}

	public BaseObject3D getChildAt(int index) {
		return mChildren.get(index);
	}

	public BaseObject3D getChildByName(String name) {
		for (int i = 0; i < mNumChildren; ++i)
			if (mChildren.get(i).getName().equals(name))
				return mChildren.get(i);

		return null;
	}

	public void setMaterial(AMaterial material) {
		setMaterial(material, true);
	}

	public void setMaterial(AMaterial material, boolean copyTextures) {
		if (mMaterial != null && copyTextures)
			mMaterial.copyTexturesTo(material);
		mMaterial = material;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public boolean isForcedDepth() {
		return mForcedDepth;
	}

	public void setForcedDepth(boolean forcedDepth) {
		this.mForcedDepth = forcedDepth;
	}

	public ArrayList<TextureInfo> getTextureInfoList() {
		int i;
		ArrayList<TextureInfo> ti = mMaterial.getTextureInfoList();

		for (i = 0; i < mNumChildren; ++i) {
			ti.addAll(mChildren.get(i).getTextureInfoList());
		}
		return ti;
	}

	public SerializedObject3D toSerializedObject3D() {
		SerializedObject3D ser = new SerializedObject3D(mVertices.capacity(),
				mNormals.capacity(), mTextureCoords.capacity(),
				mColors.capacity(), mIndices.capacity());

		int i;

		for (i = 0; i < mVertices.capacity(); i++)
			ser.getVertices()[i] = mVertices.get(i);
		for (i = 0; i < mNormals.capacity(); i++)
			ser.getNormals()[i] = mNormals.get(i);
		for (i = 0; i < mTextureCoords.capacity(); i++)
			ser.getTextureCoords()[i] = mTextureCoords.get(i);
		for (i = 0; i < mColors.capacity(); i++)
			ser.getTextureCoords()[i] = mColors.get(i);
		for (i = 0; i < mIndices.capacity(); i++)
			ser.getIndices()[i] = mIndices.get(i);

		return ser;
	}

	public float getAlpha() {
		return mAlpha;
	}

	public void setAlpha(float alpha) {
		this.mAlpha = alpha;
	}

	public void setColor(float r, float g, float b, float a) {
		int numColors = mNumVertices * 4;
		float[] colors = new float[numColors];

		for (int j = 0; j < numColors; j += 4) {
			colors[j] = r;
			colors[j + 1] = g;
			colors[j + 2] = b;
			colors[j + 3] = a;
		}
		mColors.position(0);
		// mColors.clear();
		mColors = ByteBuffer.allocateDirect(colors.length * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mColors.put(colors).position(0);
	}

	public void setColor(int color) {
		setColor(Color.red(color) / 255f, Color.green(color) / 255f,
				Color.blue(color) / 255f, Color.alpha(color) / 255f);
	}

	/**
	 * Make sure this line is in your AndroidManifer.xml file, under <manifest>:
	 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	 */
	public void serializeToSDCard(String fileName) {
		FileOutputStream fos;
		try {
			File sdcardStorage = Environment.getExternalStorageDirectory();
			String sdcardPath = sdcardStorage.getParent()
					+ java.io.File.separator + sdcardStorage.getName();

			File f = new File(sdcardPath + File.separator + fileName);
			fos = new FileOutputStream(f);
			ObjectOutputStream os = new ObjectOutputStream(fos);

			os.writeObject(toSerializedObject3D());
			os.close();
			Log.i(RajawaliRenderer.TAG, "Successfully serialized " + fileName + " to SD card.");
		} catch (Exception e) {
			Log.e(RajawaliRenderer.TAG, "Serializing " + fileName + " to SD card was unsuccessfull.");
			e.printStackTrace();
		}
	}
}
