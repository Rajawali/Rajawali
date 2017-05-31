package org.rajawali3d.debug;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.nio.FloatBuffer;
import java.util.Stack;

public class DebugBoundingBox extends DebugObject3D {
    private Vector3[] mBBoxVertices;

    public DebugBoundingBox() {
        super(Color.CYAN, 1);
    }

    public DebugBoundingBox(ALight light, int color, int lineThickness) {
        super(color, lineThickness);
    }

    public void updateBoundingBox(BoundingBox boundingBox) {
        if(mBBoxVertices == null) {
            mBBoxVertices = new Vector3[8];
            mPoints = new Stack<>();

            for(int i=0; i<16; i++) {
                mPoints.push(new Vector3());
                if(i < 8)
                    mBBoxVertices[i] = new Vector3();
            }

            init(true);

            getGeometry().changeBufferUsage(mGeometry.getVertexBufferInfo(), GLES20.GL_DYNAMIC_DRAW);

            setMaterial(new Material());
        }

        updateBox(boundingBox);
    }

    private void updateBox(BoundingBox bb) {
        FloatBuffer b = mGeometry.getVertices();
        int index = 0;

        bb.copyPoints(mBBoxVertices);

        addVertexToBuffer(b, index++, mBBoxVertices[0]);
        addVertexToBuffer(b, index++, mBBoxVertices[1]);
        addVertexToBuffer(b, index++, mBBoxVertices[2]);
        addVertexToBuffer(b, index++, mBBoxVertices[3]);
        addVertexToBuffer(b, index++, mBBoxVertices[0]);
        addVertexToBuffer(b, index++, mBBoxVertices[4]);
        addVertexToBuffer(b, index++, mBBoxVertices[5]);
        addVertexToBuffer(b, index++, mBBoxVertices[1]);
        addVertexToBuffer(b, index++, mBBoxVertices[5]);
        addVertexToBuffer(b, index++, mBBoxVertices[6]);
        addVertexToBuffer(b, index++, mBBoxVertices[2]);
        addVertexToBuffer(b, index++, mBBoxVertices[6]);
        addVertexToBuffer(b, index++, mBBoxVertices[7]);
        addVertexToBuffer(b, index++, mBBoxVertices[3]);
        addVertexToBuffer(b, index++, mBBoxVertices[7]);
        addVertexToBuffer(b, index++, mBBoxVertices[4]);

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
}
