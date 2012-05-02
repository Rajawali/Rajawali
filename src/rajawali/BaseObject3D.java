package rajawali;

import java.util.ArrayList;
import java.util.Stack;

import rajawali.bounds.BoundingBox;
import rajawali.lights.ALight;
import rajawali.materials.AMaterial;
import rajawali.materials.ColorPickerMaterial;
import rajawali.materials.TextureInfo;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import rajawali.util.RajLog;
import rajawali.visitors.INode;
import rajawali.visitors.INodeVisitor;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;

/**
 * This is the main object that all other 3D objects inherit from.
 * 
 * @author dennis.ippel
 *
 */
/**
 * @author dennis.ippel
 *
 */
public class BaseObject3D extends ATransformable3D implements Comparable<BaseObject3D>, INode {
	protected float[] mMVPMatrix = new float[16];
	protected float[] mMMatrix = new float[16];
	protected float[] mProjMatrix;

	protected float[] mScalematrix = new float[16];
	protected float[] mTranslateMatrix = new float[16];
	protected float[] mRotateMatrix = new float[16];
	protected float[] mRotateMatrixTmp = new float[16];
	protected float[] mTmpMatrix = new float[16];

	protected AMaterial mMaterial;
	protected Stack<ALight> mLights;

	protected Geometry3D mGeometry;
	protected ArrayList<BaseObject3D> mChildren;
	protected int mNumChildren;
	protected String mName;

	protected boolean mAdditive = false;
	protected boolean mDoubleSided = false;
	protected boolean mTransparent = false;
	protected boolean mForcedDepth = false;
	protected boolean mHasCubemapTexture = false;
	protected boolean mIsVisible = true;
	protected boolean mShowBoundingVolume = false;
	protected int mDrawingMode = GLES20.GL_TRIANGLES;

	protected boolean mIsContainerOnly = true;
	protected int mPickingColor;
	protected boolean mIsPickingEnabled = false;
	protected float[] mPickingColorArray;

	protected boolean mFrustumTest = false;
	protected boolean mIsInFrustum;
	
	protected boolean mRenderChildrenAsBatch = false;
	protected boolean mIsPartOfBatch = false;
		
	private int i;

	public BaseObject3D() {
		super();
		mChildren = new ArrayList<BaseObject3D>();
		mGeometry = new Geometry3D();
		mLights = new Stack<ALight>();
	}

	public BaseObject3D(String name) {
		this();
		mName = name;
	}

	/**
	 * Creates a BaseObject3D from a serialized file. A serialized file can be a BaseObject3D but also
	 * a VertexAnimationObject3D.
	 * 
	 * A serialized file can be created by the MeshExporter class. Example: 
	 * 	<code>
	  	Cube cube = new Cube(2);
	 	MeshExporter exporter = new MeshExporter(cube);
	 	exporter.export("myobject.ser", ExportType.SERIALIZED);
	 	</code>
	 * This saves the serialized file to the SD card.
	 * 
	 * @param ser
	 */
	public BaseObject3D(SerializedObject3D ser) {
		this();
		setData(ser.getVertices(), ser.getNormals(), ser.getTextureCoords(), ser.getColors(), ser.getIndices());
	}

	
	/**
	 * Passes the data to the Geometry3D instance. Vertex Buffer Objects (VBOs) will be created.
	 * 
	 * @param vertexBufferHandle	The handle to the vertex buffer
	 * @param normalBufferHandle	The handle to the normal buffer
	 * @param textureCoords			A float array containing texture coordinates
	 * @param colors				A float array containing color values (rgba)
	 * @param indices				An integer array containing face indices
	 */
	public void setData(int vertexBufferHandle, int normalBufferHandle, float[] textureCoords, float[] colors, int[] indices) {
		mGeometry.setData(vertexBufferHandle, normalBufferHandle, textureCoords, colors, indices);
		mIsContainerOnly = false;
	}

	/**
	 * Passes the data to the Geometry3D instance. Vertex Buffer Objects (VBOs) will be created.
	 * 
	 * @param vertices				A float array containing vertex data
	 * @param normals				A float array containing normal data
	 * @param textureCoords			A float array containing texture coordinates
	 * @param colors				A float array containing color values (rgba)
	 * @param indices				An integer array containing face indices
	 */
	public void setData(float[] vertices, float[] normals, float[] textureCoords, float[] colors, int[] indices) {
		mGeometry.setData(vertices, normals, textureCoords, colors, indices);
		mIsContainerOnly = false;
	}

	/**
	 * Executed before the rendering process starts
	 */
	protected void preRender() {
	}

	public void render(Camera camera, float[] projMatrix, float[] vMatrix, ColorPickerInfo pickerInfo) {
		render(camera, projMatrix, vMatrix, null, pickerInfo);
	}

	/**
	 * Renders the object
	 * 
	 * @param camera				The camera
	 * @param projMatrix			The projection matrix
	 * @param vMatrix				The view matrix
	 * @param parentMatrix			This object's parent matrix
	 * @param pickerInfo			The current color picker info. This is only used when an object is touched.
	 */
	public void render(Camera camera, float[] projMatrix, float[] vMatrix, final float[] parentMatrix, ColorPickerInfo pickerInfo) {
		if (!mIsVisible)
			return;

		preRender();
		
		// -- move view matrix transformation first
		Matrix.setIdentityM(mMMatrix, 0);
		Matrix.setIdentityM(mScalematrix, 0);
		Matrix.scaleM(mScalematrix, 0, mScale.x, mScale.y, mScale.z);

		Matrix.setIdentityM(mRotateMatrix, 0);

		setOrientation();
		if(mLookAt == null) {
			mOrientation.toRotationMatrix(mRotateMatrix);
		} else {
			System.arraycopy(mLookAtMatrix, 0, mRotateMatrix, 0, 16);
		}

		Matrix.translateM(mMMatrix, 0, -mPosition.x, mPosition.y, mPosition.z);
		Matrix.setIdentityM(mTmpMatrix, 0);
		Matrix.multiplyMM(mTmpMatrix, 0, mMMatrix, 0, mScalematrix, 0);
		Matrix.multiplyMM(mMMatrix, 0, mTmpMatrix, 0, mRotateMatrix, 0);
		if (parentMatrix != null) {
			Matrix.multiplyMM(mTmpMatrix, 0, parentMatrix, 0, mMMatrix, 0);
			System.arraycopy(mTmpMatrix, 0, mMMatrix, 0, 16);
		}
		Matrix.multiplyMM(mMVPMatrix, 0, vMatrix, 0, mMMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, projMatrix, 0, mMVPMatrix, 0);
		
		mIsInFrustum = true; // only if mFrustrumTest == true it check frustum
		if (mFrustumTest && mGeometry.hasBoundingBox()) {
			BoundingBox bbox=mGeometry.getBoundingBox();
			bbox.transform(mMMatrix);
			if (!camera.mFrustum.boundsInFrustum(bbox)) {
				mIsInFrustum=false;
			}
		}
		if (!mIsContainerOnly && mIsInFrustum) {
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

			if (mAdditive) {
				// No depth testing
				GLES20.glClearDepthf(1.0f);
				GLES20.glDisable(GLES20.GL_DEPTH_TEST);
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
				GLES20.glDepthMask(false);
			}

			if (pickerInfo != null && mIsPickingEnabled) {
				ColorPickerMaterial pickerMat = pickerInfo.getPicker().getMaterial();
				pickerMat.setPickingColor(mPickingColorArray);
				pickerMat.useProgram();
				pickerMat.setCamera(camera);
				pickerMat.setVertices(mGeometry.getVertexBufferHandle());
			} else {
			  if(!mIsPartOfBatch) {
			    mMaterial.useProgram();
			    mMaterial.bindTextures();
			  
  				mMaterial.setTextureCoords(mGeometry.getTexCoordBufferHandle(), mHasCubemapTexture);
  				mMaterial.setNormals(mGeometry.getNormalBufferHandle());
  				mMaterial.setCamera(camera);
  				mMaterial.setVertices(mGeometry.getVertexBufferHandle());
			  }
			  mMaterial.setColors(mGeometry.getColorBufferHandle());
			}

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

			setShaderParams(camera);

			if(pickerInfo == null)
			{
				mMaterial.setMVPMatrix(mMVPMatrix);
				mMaterial.setModelMatrix(mMMatrix);
				mMaterial.setViewMatrix(vMatrix);

				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGeometry.getIndexBufferHandle());
				fix.android.opengl.GLES20.glDrawElements(mDrawingMode, mGeometry.getNumIndices(), GLES20.GL_UNSIGNED_INT, 0);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
				if(!mIsPartOfBatch) {
				  mMaterial.unbindTextures();
				}
			} else if (pickerInfo != null && mIsPickingEnabled) {
				ColorPickerMaterial pickerMat = pickerInfo.getPicker().getMaterial();
				pickerMat.setMVPMatrix(mMVPMatrix);
				pickerMat.setModelMatrix(mMMatrix);
				pickerMat.setViewMatrix(vMatrix);

				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGeometry.getIndexBufferHandle());
				fix.android.opengl.GLES20.glDrawElements(mDrawingMode, mGeometry.getNumIndices(), GLES20.GL_UNSIGNED_INT, 0);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

				pickerMat.unbindTextures();
			}
			GLES20.glDisable(GLES20.GL_CULL_FACE);
			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		}

		if (mShowBoundingVolume) {
			if (mGeometry.hasBoundingBox())
				mGeometry.getBoundingBox().drawBoundingVolume(camera, projMatrix, vMatrix, mMMatrix);
			if (mGeometry.hasBoundingSphere())
				mGeometry.getBoundingSphere().drawBoundingVolume(camera, projMatrix, vMatrix, mMMatrix);
		}
		//Draw children without frustum test
		for (i = 0; i < mNumChildren; ++i) {
			mChildren.get(i).render(camera, projMatrix, vMatrix, mMMatrix, pickerInfo);
		}
	}

	/**
	 * Optimized version of Matrix.rotateM(). Apparently the native version does a lot
	 * of float[] allocations.
	 * 
	 * @see http://groups.google.com/group/android-developers/browse_thread/thread/b30dd2a437cfb076?pli=1
	 * 
	 * @param m			The matrix
	 * @param mOffset	Matrix offset
	 * @param a			The angle
	 * @param x			x axis
	 * @param y			y axis
	 * @param z			z axis
	 */
	protected void rotateM(float[] m, int mOffset, float a, float x, float y, float z) {
		Matrix.setIdentityM(mRotateMatrixTmp, 0);
		Matrix.setRotateM(mRotateMatrixTmp, 0, a, x, y, z);
		System.arraycopy(m, 0, mTmpMatrix, 0, 16);
		Matrix.multiplyMM(m, mOffset, mTmpMatrix, mOffset, mRotateMatrixTmp, 0);
	}

	/**
	 * This is where the parameters for the shaders are set. It is called every frame.
	 * 
	 * @param camera
	 */
	protected void setShaderParams(Camera camera) {
		mMaterial.setLights(mLights);
	};
	
	/**
	 * Adds a texture to this object
	 * 
	 * @parameter textureInfo
	 */
	public void addTexture(TextureInfo textureInfo) {
		mMaterial.addTexture(textureInfo);
	}

	protected void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {

			RajLog.e(op + ": glError " + error + " in class " + this.getClass().getName());
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	/**
	 * The reload method is called whenever the OpenGL context needs to be re-created.
	 * When the OpenGL context was lost, the vertex, uv coord, index etc data needs
	 * to be re-uploaded.
	 */
	public void reload() {
		if(!mIsContainerOnly) {
			mMaterial.reload();
			mGeometry.reload();
		}

		for(int i=0; i<mNumChildren; i++) {
			mChildren.get(i).reload();
		}
		
		if(mGeometry.hasBoundingBox())
			mGeometry.getBoundingBox().getVisual().reload();
		if(mGeometry.hasBoundingSphere())
			mGeometry.getBoundingSphere().getVisual().reload();
	}
	
	public void isContainer(boolean isContainer) {
		mIsContainerOnly = isContainer;
	}

	public boolean isContainer() {
		return mIsContainerOnly;
	}

	public void setAdditive(boolean isAdditive) {
		this.mAdditive = isAdditive;
	}

	public boolean getAdditive() {
		return mAdditive;
	}
	
	/**
	 * Maps screen coordinates to object coordinates
	 * 
	 * @param x
	 * @param y
	 * @param viewportWidth
	 * @param viewportHeight
	 * @param eyeZ
	 */
	public void setScreenCoordinates(float x, float y, int viewportWidth, int viewportHeight, float eyeZ) {
		float[] r1 = new float[16];
		int[] viewport = new int[] { 0, 0, viewportWidth, viewportHeight };
		float[] modelMatrix = new float[16];
		Matrix.setIdentityM(modelMatrix, 0);

		GLU.gluUnProject(x, viewportHeight - y, 0.0f, modelMatrix, 0, mProjMatrix, 0, viewport, 0, r1, 0);
		setPosition(r1[0] * eyeZ, r1[1] * -eyeZ, 0);
	}

	public float[] getModelMatrix() {
		return mMMatrix;
	}
	
	public boolean isDoubleSided() {
		return mDoubleSided;
	}
	
	public boolean isVisible() {
		return mIsVisible;
	}

	public void setDoubleSided(boolean doubleSided) {
		this.mDoubleSided = doubleSided;
	}

	public boolean isTransparent() {
		return mTransparent;
	}

	/**
	 * Use this together with the alpha channel when calling BaseObject3D.setColor():
	 * 0xaarrggbb. So for 50% transparent red, set transparent to true and call:	 * 
	 * <code>setColor(0x7fff0000);</code>
	 * 
	 * @param transparent
	 */
	public void setTransparent(boolean transparent) {
		this.mTransparent = transparent;
	}

	public void setLights(Stack<ALight> lights) {
		mLights = lights;
		for (int i = 0; i < mChildren.size(); ++i)
			mChildren.get(i).setLights(lights);
	}
	
	/**
	 * Adds a light to this object.
	 * 
	 * @param light
	 */
	public void addLight(ALight light) {
		mLights.add(light);
		for (int i = 0; i < mChildren.size(); ++i)
			mChildren.get(i).setLights(mLights);
	}
	
	/**
	 * @deprecated Use addLight() instead
	 * @param light
	 */
	public void setLight(ALight light) {
		addLight(light);
	}

	/**
	 * @deprecated use getLight(int index) instead
	 * @return
	 */
	public ALight getLight() {
		return mLights.get(0);
	}
	
	public ALight getLight(int index) {
		return mLights.get(index);
	}

	public int getDrawingMode() {
		return mDrawingMode;
	}
	
	/**
	 * Sets the OpenGL drawing mode. GLES20.GL_TRIANGLES is the default. Other values
	 * can be GL_LINES, GL_LINE_LOOP, GL_LINE_LOOP, GL_TRIANGLE_FAN, GL_TRIANGLE_STRIP
	 * 
	 * 
	 * @param drawingMode
	 */
	public void setDrawingMode(int drawingMode) {
		this.mDrawingMode = drawingMode;
	}

	/**
	 * Compares one object's depth to another object's depth 
	 */
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
		if(mRenderChildrenAsBatch)
		  child.setPartOfBatch(true);
	}

	public void removeChild(BaseObject3D child) {
		if (mChildren.remove(child)) {
			mNumChildren--;
		}
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

	public Geometry3D getGeometry() {
		return mGeometry;
	}

	public void setMaterial(AMaterial material) {
		setMaterial(material, true);
	}

	public AMaterial getMaterial() {
		return mMaterial;
	}

	public void setMaterial(AMaterial material, boolean copyTextures) {
		if (mMaterial != null && copyTextures)
			mMaterial.copyTexturesTo(material);
		else if (mMaterial != null && !copyTextures)
			mMaterial.getTextureInfoList().clear();
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
		SerializedObject3D ser = new SerializedObject3D(
				mGeometry.getVertices().capacity(), 
				mGeometry.getNormals().capacity(), 
				mGeometry.getTextureCoords().capacity(), 
				mGeometry.getColors().capacity(), 
				mGeometry.getIndices().capacity());

		int i;

		for (i = 0; i < mGeometry.getVertices().capacity(); i++)
			ser.getVertices()[i] = mGeometry.getVertices().get(i);
		if(mGeometry.getNormals() != null)
			for (i = 0; i < mGeometry.getNormals().capacity(); i++)
				ser.getNormals()[i] = mGeometry.getNormals().get(i);
		for (i = 0; i < mGeometry.getTextureCoords().capacity(); i++)
			ser.getTextureCoords()[i] = mGeometry.getTextureCoords().get(i);
		for (i = 0; i < mGeometry.getColors().capacity(); i++)
			ser.getColors()[i] = mGeometry.getColors().get(i);
		for (i = 0; i < mGeometry.getIndices().capacity(); i++)
			ser.getIndices()[i] = mGeometry.getIndices().get(i);

		return ser;
	}

	public BaseObject3D clone() {
		BaseObject3D clone = new BaseObject3D();
		clone.getGeometry().copyFromGeometry3D(mGeometry);
		clone.isContainer(mIsContainerOnly);
		clone.setMaterial(mMaterial);
		return clone;
	}

	public void setVisible(boolean visible) {
		mIsVisible = visible;
	}

	public void setColor(int color) {
		setColor(color, false);
	}

	public void setColor(int color, boolean createNewBuffer) {
		mGeometry.setColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f, createNewBuffer);
	}

	public int getPickingColor() {
		return mPickingColor;
	}

	public void setPickingColor(int pickingColor) {
		if (mPickingColorArray == null)
			mPickingColorArray = new float[4];
		this.mPickingColor = pickingColor;
		mPickingColorArray[0] = Color.red(pickingColor) / 255f;
		mPickingColorArray[1] = Color.green(pickingColor) / 255f;
		mPickingColorArray[2] = Color.blue(pickingColor) / 255f;
		mPickingColorArray[3] = Color.alpha(pickingColor) / 255f;
		mIsPickingEnabled = true;
	}

	public void setShowBoundingVolume(boolean showBoundingVolume) {
		this.mShowBoundingVolume = showBoundingVolume;
	}
	
	public float[] getRotationMatrix() {
		return mRotateMatrix;
	}
	
	public void setFrustumTest(boolean value){
		mFrustumTest = value;
	}
	
	public void accept(INodeVisitor visitor) {
		visitor.apply(this);
	}
	
	public boolean isInFrustum() {
		return mIsInFrustum;
	}

  public boolean getRenderChildrenAsBatch()
  {
    return mRenderChildrenAsBatch;
  }

  public void setRenderChildrenAsBatch(boolean renderChildrenAsBatch)
  {
    this.mRenderChildrenAsBatch = renderChildrenAsBatch;
  }

  public boolean isPartOfBatch()
  {
    return mIsPartOfBatch;
  }

  public void setPartOfBatch(boolean isPartOfBatch)
  {
    this.mIsPartOfBatch = isPartOfBatch;
  }
}