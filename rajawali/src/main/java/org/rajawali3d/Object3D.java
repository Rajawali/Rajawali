/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.MaterialManager;
import org.rajawali3d.materials.textures.TextureAtlas;
import org.rajawali3d.materials.textures.TexturePacker.Tile;
import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.GLU;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.visitors.INode;
import org.rajawali3d.visitors.INodeVisitor;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is the main object that all other 3D objects inherit from.
 *
 * @author dennis.ippel
 *
 */
public class Object3D extends ATransformable3D implements Comparable<Object3D>, INode {

    public static final int RED = 0;
    public static final int GREEN = 1;
    public static final int BLUE = 2;
    public static final int ALPHA = 3;

	protected final Matrix4 mMVPMatrix = new Matrix4();

	protected final Matrix4 mMVMatrix = new Matrix4();
	protected Matrix4 mPMatrix;
	protected Matrix4 mParentMatrix;
	protected final Matrix4 mRotationMatrix = new Matrix4();

	protected float[] mColor;

	protected Material mMaterial;

	protected Geometry3D mGeometry;
	protected Object3D mParent;
	protected List<Object3D> mChildren;
	protected String mName;

	protected boolean mDoubleSided = false;
	protected boolean mBackSided = false;
	protected boolean mTransparent = false;
	protected boolean mForcedDepth = false;
	protected boolean mHasCubemapTexture = false;
	protected boolean mIsVisible = true;
	protected boolean mShowBoundingVolume = false;
	protected boolean mOverrideMaterialColor = false;
	protected int mDrawingMode = GLES20.GL_TRIANGLES;
	protected int mElementsBufferType = GLES20.GL_UNSIGNED_INT;

	protected boolean mIsContainerOnly = true;
	protected int mPickingColor;
	protected boolean mIsPickingEnabled = false;
	protected float[] mPickingColorArray;

	protected boolean mFrustumTest = false;
	protected boolean mIsInFrustum;

	protected boolean mRenderChildrenAsBatch = false;
	protected boolean mIsPartOfBatch = false;
	protected boolean mManageMaterial = true;

	protected boolean mEnableBlending = false;
	protected int mBlendFuncSFactor;
	protected int mBlendFuncDFactor;
	protected boolean mEnableDepthTest = true;
	protected boolean mEnableDepthMask = true;

	public Object3D() {
		super();
		mChildren = Collections.synchronizedList(new CopyOnWriteArrayList<Object3D>());
		mGeometry = new Geometry3D();
		mColor = new float[] { 0, 1, 0, 1.0f};
	}

	public Object3D(String name) {
		this();
		mName = name;
	}

	/**
	 * Passes the data to the Geometry3D instance. Vertex Buffer Objects (VBOs) will be created.
	 *
	 * @param vertexBufferInfo
	 *            The handle to the vertex buffer
	 * @param normalBufferInfo
	 *            The handle to the normal buffer
	 * @param textureCoords
	 *            A float array containing texture coordinates
	 * @param colors
	 *            A float array containing color values (rgba)
	 * @param indices
	 *            An integer array containing face indices
     * @param createVBOs
     *            A boolean controlling if the VBOs are create immediately.
	 */
	public void setData(BufferInfo vertexBufferInfo, BufferInfo normalBufferInfo, float[] textureCoords,
			float[] colors, int[] indices, boolean createVBOs) {
		mGeometry.setData(vertexBufferInfo, normalBufferInfo, textureCoords, colors, indices, createVBOs);
		mIsContainerOnly = false;
		mElementsBufferType = mGeometry.areOnlyShortBuffersSupported() ? GLES20.GL_UNSIGNED_SHORT
				: GLES20.GL_UNSIGNED_INT;
	}

	/**
	 * Passes the data to the Geometry3D instance. Vertex Buffer Objects (VBOs) will be created.
	 *
	 * @param vertices
	 *            A float array containing vertex data
	 * @param normals
	 *            A float array containing normal data
	 * @param textureCoords
	 *            A float array containing texture coordinates
	 * @param colors
	 *            A float array containing color values (rgba)
	 * @param indices
	 *            An integer array containing face indices
     * @param createVBOs
     *            A boolean controlling if the VBOs are create immediately.
	 */
	public void setData(float[] vertices, float[] normals, float[] textureCoords, float[] colors, int[] indices, boolean createVBOs) {
		setData(vertices, GLES20.GL_STATIC_DRAW, normals, GLES20.GL_STATIC_DRAW, textureCoords, GLES20.GL_STATIC_DRAW,
				colors, GLES20.GL_STATIC_DRAW, indices, GLES20.GL_STATIC_DRAW, createVBOs);
	}

	public void setData(float[] vertices, int verticesUsage, float[] normals, int normalsUsage, float[] textureCoords,
			int textureCoordsUsage,
			float[] colors, int colorsUsage, int[] indices, int indicesUsage, boolean createVBOs) {
		mGeometry.setData(vertices, verticesUsage, normals, normalsUsage, textureCoords, textureCoordsUsage, colors,
				colorsUsage, indices, indicesUsage, createVBOs);
		mIsContainerOnly = false;
		mElementsBufferType = mGeometry.areOnlyShortBuffersSupported() ? GLES20.GL_UNSIGNED_SHORT
				: GLES20.GL_UNSIGNED_INT;
	}

	/**
	 * Executed before the rendering process starts
	 */
	protected void preRender() {
		mGeometry.validateBuffers();
	}

	/**
	 * Renders the object with no parent matrix.
	 *
	 * @param camera The camera
	 * @param vpMatrix {@link Matrix4} The view-projection matrix
	 * @param projMatrix {@link Matrix4} The projection matrix
	 * @param vMatrix {@link Matrix4} The view matrix
	 * @param pickerInfo The current color picker info. This is only used when an object is touched.
	 */
	public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix,
			final Matrix4 vMatrix, Material sceneMaterial) {
		render(camera, vpMatrix, projMatrix, vMatrix, null, sceneMaterial);
	}

	/**
	 * Renders the object
	 *
	 * @param camera The camera
	 * @param vpMatrix {@link Matrix4} The view-projection matrix
	 * @param projMatrix {@link Matrix4} The projection matrix
	 * @param vMatrix {@link Matrix4} The view matrix
	 * @param parentMatrix {@link Matrix4} This object's parent matrix
	 * @param pickerInfo The current color picker info. This is only used when an object is touched.
	 */
	public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, final Matrix4 vMatrix,
			final Matrix4 parentMatrix, Material sceneMaterial) {
		if (!mIsVisible && !mRenderChildrenAsBatch)
			return;

		Material material = sceneMaterial == null ? mMaterial : sceneMaterial;
		preRender();

		// -- move view matrix transformation first
		boolean modelMatrixWasRecalculated = onRecalculateModelMatrix(parentMatrix);
		// -- calculate model view matrix;
		mMVMatrix.setAll(vMatrix).multiply(mMMatrix);
		//Create MVP Matrix from View-Projection Matrix
		mMVPMatrix.setAll(vpMatrix).multiply(mMMatrix);

        // Transform the bounding volumes if they exist
        if (mGeometry.hasBoundingBox()) mGeometry.getBoundingBox().transform(getModelMatrix());
        if (mGeometry.hasBoundingSphere()) mGeometry.getBoundingSphere().transform(getModelMatrix());

		mIsInFrustum = true; // only if mFrustrumTest == true it check frustum
		if (mFrustumTest && mGeometry.hasBoundingBox()) {
			BoundingBox bbox = mGeometry.getBoundingBox();
			if (!camera.getFrustum().boundsInFrustum(bbox)) {
				mIsInFrustum = false;
			}
		}

		if (!mIsContainerOnly && mIsInFrustum) {
			mPMatrix = projMatrix;
			if (mDoubleSided) {
				GLES20.glDisable(GLES20.GL_CULL_FACE);
			} else {
				GLES20.glEnable(GLES20.GL_CULL_FACE);
			     if (mBackSided) {
			          GLES20.glCullFace(GLES20.GL_FRONT);
			     } else {
			          GLES20.glCullFace(GLES20.GL_BACK);
			          GLES20.glFrontFace(GLES20.GL_CCW);
			     }
			}
			if (mEnableBlending) {
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(mBlendFuncSFactor, mBlendFuncDFactor);
			}
			if (!mEnableDepthTest) GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			else {
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthFunc(GLES20.GL_LESS);
			}

			GLES20.glDepthMask(mEnableDepthMask);

			if (!mIsPartOfBatch) {
				if (material == null) {
					RajLog.e("[" + this.getClass().getName()
							+ "] This object can't render because there's no material attached to it.");
					throw new RuntimeException(
							"This object can't render because there's no material attached to it.");
				}
				material.useProgram();

				setShaderParams(camera);
				material.bindTextures();
				if(mGeometry.hasTextureCoordinates())
					material.setTextureCoords(mGeometry.getTexCoordBufferInfo());
				if(mGeometry.hasNormals())
					material.setNormals(mGeometry.getNormalBufferInfo());
				if(mMaterial.usingVertexColors())
					material.setVertexColors(mGeometry.getColorBufferInfo());
				
				material.setVertices(mGeometry.getVertexBufferInfo());
			}
			material.setCurrentObject(this);
			if(mOverrideMaterialColor) {
                material.setColor(mColor);
            }
            material.applyParams();

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

			material.setMVPMatrix(mMVPMatrix);
			material.setModelMatrix(mMMatrix);
			material.setModelViewMatrix(mMVMatrix);

			if(mIsVisible) {
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGeometry.getIndexBufferInfo().bufferHandle);
				GLES20.glDrawElements(mDrawingMode, mGeometry.getNumIndices(), mElementsBufferType,	0);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
			}
			if (!mIsPartOfBatch && !mRenderChildrenAsBatch && sceneMaterial == null) {
				material.unbindTextures();
			}

			material.unsetCurrentObject(this);

			if (mEnableBlending) {
				GLES20.glDisable(GLES20.GL_BLEND);
			}

			if (mDoubleSided) {
				GLES20.glEnable(GLES20.GL_CULL_FACE);
			} else if (mBackSided) {
				GLES20.glCullFace(GLES20.GL_BACK);
			}
			if (!mEnableDepthTest) {
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthFunc(GLES20.GL_LESS);
			}
		}

		if (mShowBoundingVolume) {
			if (mGeometry.hasBoundingBox())
				mGeometry.getBoundingBox().drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, mMMatrix);
			if (mGeometry.hasBoundingSphere())
				mGeometry.getBoundingSphere().drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, mMMatrix);
		}
		// Draw children without frustum test
		for (int i = 0, j = mChildren.size(); i < j; i++) {
			Object3D child = mChildren.get(i);
            if(mRenderChildrenAsBatch || mIsPartOfBatch) {
                child.setPartOfBatch(true);
            }
            if(modelMatrixWasRecalculated) child.markModelMatrixDirty();
			child.render(camera, vpMatrix, projMatrix, vMatrix, mMMatrix, sceneMaterial);
		}

		if (mRenderChildrenAsBatch && sceneMaterial == null) {
			material.unbindTextures();
		}
	}

	/**
	 * This is where the parameters for the shaders are set. It is called every frame.
	 *
	 * @param camera
	 */
	protected void setShaderParams(Camera camera) {
	}

	protected void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			RajLog.e(op + ": glError " + error + " in class " + this.getClass().getName());
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	/**
	 * The reload method is called whenever the OpenGL context needs to be re-created. When the OpenGL context was lost,
	 * the vertex, uv coord, index etc data needs to be re-uploaded.
	 */
	public void reload() {
		if (!mIsContainerOnly) {
			mGeometry.reload();
		}

		for (int i = 0, j = mChildren.size(); i < j; i++)
			mChildren.get(i).reload();

		if (mGeometry.hasBoundingBox() && mGeometry.getBoundingBox().getVisual() != null)
			mGeometry.getBoundingBox().getVisual().reload();
		if (mGeometry.hasBoundingSphere() && mGeometry.getBoundingSphere().getVisual() != null)
			mGeometry.getBoundingSphere().getVisual().reload();
	}

    public boolean hasBoundingVolume() {
        return mGeometry.hasBoundingBox() || mGeometry.hasBoundingSphere();
    }

	public void isContainer(boolean isContainer) {
		mIsContainerOnly = isContainer;
	}

	public boolean isContainer() {
		return mIsContainerOnly;
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
	public void setScreenCoordinates(double x, double y, int viewportWidth, int viewportHeight, double eyeZ) {
		double[] r1 = new double[16];
		int[] viewport = new int[] { 0, 0, viewportWidth, viewportHeight };
		double[] modelMatrix = new double[16];
		Matrix.setIdentityM(modelMatrix, 0);

		GLU.gluUnProject(x, viewportHeight - y, 0.0, modelMatrix, 0, mPMatrix.getDoubleValues(), 0, viewport, 0, r1, 0);
		setPosition(r1[0] * eyeZ, r1[1] * -eyeZ, 0);
	}

	public boolean isDoubleSided() {
		return mDoubleSided;
	}

	public boolean isBackSided() {
		return mBackSided;
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	public void setDoubleSided(boolean doubleSided) {
		this.mDoubleSided = doubleSided;
	}

	public void setBackSided(boolean backSided) {
		this.mBackSided = backSided;
	}

	public boolean isTransparent() {
		return mTransparent;
	}

	/**
	 * Use this together with the alpha channel when calling BaseObject3D.setColor(): 0xaarrggbb. So for 50% transparent
	 * red, set transparent to true and call: * <code>setColor(0x7fff0000);</code>
	 *
	 * @param transparent
	 */
	public void setTransparent(boolean value) {
		mTransparent = value;
		mEnableBlending = value;
		setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		mEnableDepthMask = !value;
	}

	public int getDrawingMode() {
		return mDrawingMode;
	}

	/**
	 * Sets the OpenGL drawing mode. GLES20.GL_TRIANGLES is the default. Other values can be GL_LINES, GL_LINE_LOOP,
	 * GL_LINE_LOOP, GL_TRIANGLE_FAN, GL_TRIANGLE_STRIP
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
	public int compareTo(Object3D another) {
		if (mForcedDepth)
			return -1;
		if (mPosition.z < another.getZ())
			return 1;
		else if (mPosition.z > another.getZ())
			return -1;
		else
			return 0;
	}

	public void addChild(Object3D child) {
		if(child.getParent() != null)
			child.getParent().removeChild(child);
		mChildren.add(child);
		if (mRenderChildrenAsBatch)
			child.setPartOfBatch(true);
	}

	public boolean removeChild(Object3D child) {
		return mChildren.remove(child);
	}

	public Object3D getParent()
	{
		return mParent;
	}

	/**
	 * Retrieve the number of triangles of the object, recursive method
	 *
	 * @return int the total triangle count for the object.
	 */
	public int getNumTriangles() {
		int triangleCount = 0;

		for (int i = 0, j = getNumChildren(); i < j; i++) {
			Object3D child = getChildAt(i);
			if (child.getGeometry() != null && child.getGeometry().getVertices() != null && child.isVisible())
				if (child.getNumChildren() > 0) {
					triangleCount += child.getNumTriangles();
				} else {
					triangleCount += child.getGeometry().getVertices().limit() / 9;
				}
		}
		return triangleCount;
	}
	/**
	 * Retrieve the number of objects in the object, recursive method
	 *
	 * @return int the total object count for the object.
	 */
	public int getNumObjects() {
		int objectCount = 0;

		for (int i = 0, j = getNumChildren(); i < j; i++) {
			Object3D child = getChildAt(i);
			if (child.getGeometry() != null && child.getGeometry().getVertices() != null && child.isVisible())
				if (child.getNumChildren() > 0) {
					objectCount += child.getNumObjects() + 1;
				} else {
					objectCount++;
				}
		}
		return objectCount;
	}

	public int getNumChildren() {
		return mChildren.size();
	}

	public Object3D getChildAt(int index) {
		return mChildren.get(index);
	}

	public Object3D getChildByName(String name) {
		for (int i = 0, j = mChildren.size(); i < j; i++)
			if (mChildren.get(i).getName().equals(name))
				return mChildren.get(i);

		return null;
	}

	public Geometry3D getGeometry() {
		return mGeometry;
	}

	public Material getMaterial() {
		return mMaterial;
	}

	public void setMaterial(Material material) {
		if(material == null) return;
		MaterialManager.getInstance().addMaterial(material);
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

	protected void cloneTo(Object3D clone, boolean copyMaterial) {
        clone.setName(mName);
		clone.getGeometry().copyFromGeometry3D(mGeometry);
		clone.isContainer(mIsContainerOnly);
		if (copyMaterial) clone.setMaterial(mMaterial);
		clone.mElementsBufferType = mGeometry.areOnlyShortBuffersSupported() ? GLES20.GL_UNSIGNED_SHORT
				: GLES20.GL_UNSIGNED_INT;
		clone.mTransparent = this.mTransparent;
		clone.mEnableBlending = this.mEnableBlending;
		clone.mBlendFuncSFactor = this.mBlendFuncSFactor;
		clone.mBlendFuncDFactor = this.mBlendFuncDFactor;
		clone.mEnableDepthTest = this.mEnableDepthTest;
		clone.mEnableDepthMask = this.mEnableDepthMask;
	}

	public Object3D clone(boolean copyMaterial, boolean cloneChildren) {
		final Object3D clone = new Object3D();
		cloneTo(clone, copyMaterial);
        clone.setOrientation(mOrientation);
        clone.setScale(getScale());

		if(cloneChildren)
		{
			int childCount = this.getNumChildren();
			for(int i = 0; i < childCount; i++)
			{
				clone.addChild(this.getChildAt(i).clone(copyMaterial, cloneChildren));
			}
		}

		return clone;
	}

	public Object3D clone(boolean copyMaterial) {
        	return clone(copyMaterial, false);
	}

	public Object3D clone() {
		return clone(true);
	}

	public void setVisible(boolean visible) {
		mIsVisible = visible;
	}

    public void setAlpha(int alpha) {
        mColor[ALPHA] = alpha / 255.f;
    }

    public void setAlpha(float alpha) {
        mColor[ALPHA] = alpha;
    }

	public void setColor(int color) {
		mColor[RED] = Color.red(color) / 255.f;
		mColor[GREEN] = Color.green(color) / 255.f;
		mColor[BLUE] = Color.blue(color) / 255.f;
		mColor[ALPHA] = Color.alpha(color) / 255.f;
		mOverrideMaterialColor = true;
	}

	public void setColor(Vector3 color) {
		setColor(Color.rgb((int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255)));
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

	public boolean isPickingEnabled() {
		return mIsPickingEnabled;
	}

	public void setShowBoundingVolume(boolean showBoundingVolume) {
		this.mShowBoundingVolume = showBoundingVolume;
	}

	public void setFrustumTest(boolean value) {
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

	public void setBlendingEnabled(boolean value) {
		mEnableBlending = value;
	}

	public boolean isBlendingEnabled() {
		return mEnableBlending;
	}

	public void setBlendFunc(int sFactor, int dFactor) {
		mBlendFuncSFactor = sFactor;
		mBlendFuncDFactor = dFactor;
	}

	public void setDepthTestEnabled(boolean value) {
		mEnableDepthTest = value;
	}

	public boolean isDepthTestEnabled() {
		return mEnableDepthTest;
	}

	public void setDepthMaskEnabled(boolean value) {
		mEnableDepthMask = value;
	}

	public boolean isDepthMaskEnabled() {
		return mEnableDepthMask;
	}

	public Vector3 getWorldPosition() {
		if(mParentMatrix == null) return mPosition;
		Vector3 worldPos = mPosition.clone();
		worldPos.multiply(mParentMatrix);
		return worldPos;
	}

	public Matrix4 getModelViewProjectionMatrix() {
		return mMVPMatrix;
	}

	public Matrix4 getModelViewMatrix() {
		return mMVMatrix;
	}

	/**
	 * Maps the (x,y) coordinates of <code>tileName</code> in <code>atlas</code>
	 * to the TextureCoordinates of this BaseObject3D
	 *
	 * @param tileName
	 * @param atlas
	 */
	public void setAtlasTile(String tileName, TextureAtlas atlas) {
		Tile tile = atlas.getTileNamed(tileName);
		FloatBuffer fb = this.getGeometry().getTextureCoords();
		for(int i = 0; i < fb.capacity(); i++){
			double uvIn = fb.get(i);
			double uvOut;
			if(i%2 == 0)
				uvOut = (uvIn * (tile.width/atlas.getWidth())) + tile.x/atlas.getWidth();
			else
				uvOut = (uvIn * (tile.height/atlas.getHeight())) + tile.y/atlas.getHeight();
			fb.put(i, (float) uvOut);
		}
		mGeometry.changeBufferData(mGeometry.mTexCoordBufferInfo, fb, 0);

	}

	public void destroy() {
		if (mGeometry != null)
			mGeometry.destroy();
		if (mMaterial != null)
			MaterialManager.getInstance().removeMaterial(mMaterial);
		mMaterial = null;
		mGeometry = null;
		for (int i = 0, j = mChildren.size(); i < j; i++)
			mChildren.get(i).destroy();
		mChildren.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNodeMember#getBoundingVolume()
	 */
	@Override
	public IBoundingVolume getTransformedBoundingVolume() {
		IBoundingVolume volume = null;
		volume = mGeometry.getBoundingBox();
		calculateModelMatrix(null);
		volume.transform(mMMatrix);
		return volume;
	}
}
