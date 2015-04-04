package org.rajawali3d.util.debugvisualizer;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import java.nio.FloatBuffer;
import java.util.Stack;

/**
 * @author dennis.ippel
 */
public class DebugCamera extends DebugObject3D {
    private Camera mCamera;
    protected Vector3[] mFrustumCornersTransformed;

    public DebugCamera(Camera camera) {
        this(camera, Color.BLUE, 1);
    }

    public DebugCamera(Camera camera, int color, int lineThickness) {
        super(color, lineThickness);
        mCamera = camera;
    }

    public void updateFrustum() {
        mCamera.setProjectionMatrix(mRenderer.getOverrideViewportWidth(), mRenderer.getOverrideViewportHeight());
        if (mPoints == null) {
            if(!mCamera.isInitialized()) return;

            mPoints = new Stack<>();
            mFrustumCornersTransformed = new Vector3[8];
            for(int i=0; i<16; i++) {
                if(i < 8)
                    mFrustumCornersTransformed[i] = new Vector3();
                mPoints.push(new Vector3());
            }

            init(true);

            getGeometry().changeBufferUsage(mGeometry.getVertexBufferInfo(), GLES20.GL_DYNAMIC_DRAW);

            setMaterial(new Material());
        }

        mCamera.getFrustumCorners(mFrustumCornersTransformed, true, true);

        FloatBuffer b = mGeometry.getVertices();
        int index = 0;

        addVertexToBuffer(b, index++, mFrustumCornersTransformed[0]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[1]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[2]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[3]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[0]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[4]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[5]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[1]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[5]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[6]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[2]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[6]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[7]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[3]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[7]);
        addVertexToBuffer(b, index++, mFrustumCornersTransformed[4]);

        mGeometry.changeBufferData(
                mGeometry.getVertexBufferInfo(),
                mGeometry.getVertices(), 0);
    }

    private void addVertexToBuffer(FloatBuffer b, int index, Vector3 vertex) {
        int vertIndex = index * 3;

        b.put(vertIndex, (float) vertex.x);
        b.put(vertIndex + 1, (float) vertex.y);
        b.put(vertIndex + 2, (float) vertex.z);
    }

    public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix,
                       final Matrix4 vMatrix, final Matrix4 parentMatrix, Material sceneMaterial) {
        updateFrustum();

        super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, sceneMaterial);
    }
}
