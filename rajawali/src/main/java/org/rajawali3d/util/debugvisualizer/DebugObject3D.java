package org.rajawali3d.util.debugvisualizer;

import android.graphics.Color;

import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.renderer.RajawaliRenderer;

/**
 * @author dennis.ippel
 */
public class DebugObject3D extends Line3D {
    protected RajawaliRenderer mRenderer;

    public DebugObject3D() {
        this(Color.YELLOW, 1);
    }

    public DebugObject3D(int color, int lineThickness) {
        setColor(color);
        mLineThickness = lineThickness;
    }

    public void setRenderer(RajawaliRenderer renderer) {
        mRenderer = renderer;
    }
}
