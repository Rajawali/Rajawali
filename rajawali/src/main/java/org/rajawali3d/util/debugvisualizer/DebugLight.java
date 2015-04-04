package org.rajawali3d.util.debugvisualizer;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;

import java.util.Stack;

/**
 * @author dennis.ippel
 */
public class DebugLight extends DebugObject3D {
    private ALight mLight;
    private Line3D mCircle;
    private Line3D mLine;
    private int mColor;

    public DebugLight(ALight light) {
        this(light, Color.YELLOW, 1);
    }

    public DebugLight(ALight light, int color, int lineThickness) {
        super(color, lineThickness);
        mLight = light;
        mColor = color;
    }

    private void updateLightTransform(Camera camera) {
        if(mCircle == null)
            createLines();

        mCircle.setLookAt(camera.getPosition());
        mCircle.setScale(mPosition.distanceTo(camera.getPosition()) * 0.1f);
        mCircle.setPosition(mLight.getPosition());

        mLine.setPosition(mLight.getPosition());
        mLine.setOrientation(mLight.getOrientation());
    }

    private void createLines() {
        Stack<Vector3> points = new Stack<>();

        float segmentSize = 10;
        float radius = .2f;
        int count = 0;
        for(int i=0; i<360; i+=segmentSize) {
            if(count++ % 2 == 0) continue;

            float radians1 = (float)MathUtil.degreesToRadians(i);
            float radians2 = (float)MathUtil.degreesToRadians(i + segmentSize);

            Vector3 p1 = new Vector3();
            p1.x = Math.cos(radians1) * radius;
            p1.y = Math.sin(radians1) * radius;

            Vector3 p2 = new Vector3();
            p2.x = Math.cos(radians2) * radius;
            p2.y = Math.sin(radians2) * radius;

            points.add(p1);
            points.add(p2);
        }

        Material material = new Material();

        mCircle = new Line3D(points, mLineThickness, mColor);
        mCircle.setMaterial(material);
        mCircle.setDrawingMode(GLES20.GL_LINES);
        mCircle.enableLookAt();
        addChild(mCircle);

        points = new Stack<>();

        if(mLight.getLightType() == mLight.DIRECTIONAL_LIGHT || mLight.getLightType() == mLight.SPOT_LIGHT)
        {
            for(int i=0; i<20; i+=2) {
                Vector3 p1 = new Vector3();
                p1.z = i * 0.5f;
                Vector3 p2 = new Vector3();
                p2.z = (i + 1) * 0.5f;
                points.add(p1);
                points.add(p2);
            }

            mLine = new Line3D(points, mLineThickness, mColor);
            mLine.setMaterial(material);
            mLine.setDrawingMode(GLES20.GL_LINES);
            mLine.enableLookAt();
            addChild(mLine);
        }
    }

    public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix,
                       final Matrix4 vMatrix, final Matrix4 parentMatrix, Material sceneMaterial) {
        updateLightTransform(camera);

        super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, sceneMaterial);
    }
}
