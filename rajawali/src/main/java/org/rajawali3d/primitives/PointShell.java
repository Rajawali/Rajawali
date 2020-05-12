package org.rajawali3d.primitives;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;

public class PointShell extends Object3D {

    public PointShell() {
        super();
        init(512, 1);
    }

    public PointShell(int number, float radius) {
        super();
        init(number, radius);
    }

    @Override
    protected void preRender() {
        super.preRender();
    }

    public void init(int numPoints, float radius) {

        float[] vertices = new float[numPoints * 3];
        float[] normals = new float[numPoints * 3];
        int[] indices = new int[numPoints * 3];
        float[] colors = new float[numPoints * 4];
        float[] textureCoords = new float[numPoints * 2];
        int texel = (int)Math.ceil(Math.sqrt(numPoints));

        for (int i = 0; i < numPoints; ++i) {
            Vector3 n = new Vector3(Math.random()*2-1, Math.random()*2-1, Math.random()*2-1);
            n.normalize();

            normals[i * 3 + 0] = (float)n.x;
            normals[i * 3 + 1] = (float)n.y;
            normals[i * 3 + 2] = (float)n.z;

            Vector3 v = n.clone();
            v.multiply(radius);

            vertices[i * 3 + 0] = (float)v.x;
            vertices[i * 3 + 1] = (float)v.y;
            vertices[i * 3 + 2] = (float)v.z;

            indices[i * 3 + 0] = i;
            indices[i * 3 + 1] = i;
            indices[i * 3 + 2] = i;

            float randColor = (float) Math.random();

            colors[i * 4 + 0] = randColor * randColor;
            colors[i * 4 + 1] = randColor * randColor;
            colors[i * 4 + 2] = randColor * randColor;
            colors[i * 4 + 3] = 1.0f;

            textureCoords[i * 2 + 0] = i%texel;
            textureCoords[i * 2 + 1] = i/texel;
        }

        setData(vertices, normals, null, colors, indices, true);
    }
}
