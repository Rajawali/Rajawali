package org.rajawali3d.util.debugvisualizer;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.util.Stack;

public class GridFloor extends DebugObject3D {
    private float mSize;
    private int mNumLines;

    public GridFloor() {
        this(10);
    }

    public GridFloor(float size) {
        this(size, Color.WHITE, 1, 20);
    }

    public GridFloor(float size, int color, int lineThickness, int numLines) {
        super(color, lineThickness);
        mSize = size;
        mNumLines = numLines;
        createGridFloor();
    }

    private void createGridFloor() {
        final float sizeHalf = mSize * 0.5f;
        final float spacing = mSize / mNumLines;

        mPoints = new Stack<>();

        for(float z = -sizeHalf; z <= sizeHalf; z += spacing) {
            mPoints.add(new Vector3(-sizeHalf, 0, z));
            mPoints.add(new Vector3(sizeHalf, 0, z));
        }

        for(float x = -sizeHalf; x <= sizeHalf; x += spacing) {
            mPoints.add(new Vector3(x, 0, -sizeHalf));
            mPoints.add(new Vector3(x, 0, sizeHalf));
        }

        setMaterial(new Material());
        init(true);
        setDrawingMode(GLES20.GL_LINES);
    }
}
