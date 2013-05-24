package rajawali.primitives;

import java.util.Stack;

import rajawali.BaseObject3D;
import rajawali.math.Vector3;
import android.opengl.GLES20;

public class Line3D extends BaseObject3D {
	private Stack<Vector3> mPoints;
	private float mThickness;
	
	public Line3D(Stack<Vector3> points, float thickness, int color) {
		super();
		mPoints = points;
		mThickness = thickness;
		setColor(color);
		init();
	}	
	
	private void init() {
		setDoubleSided(true);
		setDrawingMode(GLES20.GL_LINE_STRIP);
		
		int numVertices = mPoints.size();
		
		float[] vertices = new float[numVertices * 3];
		float[] textureCoords = new float[numVertices * 2];
		float[] normals = new float[numVertices * 3];
		int[] indices = new int[numVertices];
		
		for(int i=0; i<numVertices; i++) {
			Vector3 point = mPoints.get(i);
			int index = i * 3;
			vertices[index] = point.x;
			vertices[index+1] = point.y;
			vertices[index+2] = point.z;
			normals[index] = 0;
			normals[index+1] = 0;
			normals[index+2] = 1;
			index = i * 2;
			textureCoords[index] = 0;
			textureCoords[index+1] = 0;
			index = i * 4;
			indices[i] = (short)i;
		}
		
		setData(vertices, normals, textureCoords, null, indices);
	}
	
	public void preRender() {
		super.preRender();
		GLES20.glLineWidth(mThickness);
	}
}
