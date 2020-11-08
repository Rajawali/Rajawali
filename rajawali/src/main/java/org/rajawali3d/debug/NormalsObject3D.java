package org.rajawali3d.debug;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.fbx.FBXValues;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class NormalsObject3D extends DebugObject3D {
    float normalLength;

    public NormalsObject3D(Object3D o){
        this(o, 1, 0.2f, 0x00FFFF00);
    }

    public NormalsObject3D(Object3D o, int lineThinkness){
        this(o, lineThinkness, 0.2f, 0x00FFFF00);
    }

    public NormalsObject3D(Object3D o, int lineThinkness, float normalLength){
        this(o, lineThinkness, normalLength, 0x00FFFF00);
    }

    public NormalsObject3D(Object3D o, int lineThinkness, float normalLength, int color) {
        super(color, lineThinkness);
        this.normalLength = normalLength;

        float[] v = new float[o.getGeometry().getNumVertices()*3];
        float[] n = new float[o.getGeometry().getNumVertices()*3];
        int[] idx = new int[o.getGeometry().getNumVertices()*2];
        o.getGeometry().getVertices().get(v);
        o.getGeometry().getNormals().get(n);

        float[] vn = uniteVN(v, n, idx);

        this.setData(vn, null, null, null, idx, false);
        this.setName(o.getName()+"_normals");
        Material mat = new Material();
        mat.setDiffuseMethod(new DiffuseMethod.Lambert());
        mat.setColor((int)(Math.random() * 0xffffff));
        this.setMaterial(mat);
        this.setDrawingMode(GLES20.GL_LINES);
    }

    private float[] uniteVN(float[] v,float[] n, int[] idxOut)
    {
        float[] ret = new float[v.length+n.length];
        int vCount = v.length/3;
        for (int i=0; i < vCount; ++i){
            idxOut[2*i] = 2*i;
            ret[6*i+0] = v[3*i+0];
            ret[6*i+1] = v[3*i+1];
            ret[6*i+2] = v[3*i+2];
            idxOut[2*i+1] = 2*i+1;
            ret[6*i+3] = v[3*i+0]+n[3*i+0]*normalLength;
            ret[6*i+4] = v[3*i+1]+n[3*i+1]*normalLength;
            ret[6*i+5] = v[3*i+2]+n[3*i+2]*normalLength;
        }
        return ret;
    }
}
