package rajawali.primitives;

import java.util.Stack;

import rajawali.BaseObject3D;
import rajawali.math.Number3D;
import android.graphics.Color;
import android.opengl.GLES20;

public class Line3D extends BaseObject3D {
	private Stack<Number3D> mPoints;
	private float mThickness;
	private int mLineColor;
	
	public Line3D(Stack<Number3D> points, float thickness, int color) {
		super();
		mPoints = points;
		mThickness = thickness;
		mLineColor = color;
		init();
	}	
	
	private void init() {
		setDoubleSided(true);
		setDrawingMode(GLES20.GL_LINE_STRIP);
		
		int numVertices = mPoints.size();
		
		float[] vertices = new float[numVertices * 3];
		float[] textureCoords = new float[numVertices * 2];
		float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
		int[] indices = new int[numVertices];
		float r = Color.red(mLineColor) / 255f;
		float g = Color.green(mLineColor) / 255f;
		float b = Color.blue(mLineColor) / 255f;
		float a = Color.alpha(mLineColor) / 255f;
		
		for(int i=0; i<numVertices; i++) {
			Number3D point = mPoints.get(i);
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
			colors[index] = r;
			colors[index+1] = g;
			colors[index+2] = b;
			colors[index+3] = a;
			indices[i] = (short)i;
		}
		
		setData(vertices, normals, textureCoords, colors, indices);
	}
	
	public void preRender() {
		super.preRender();
		GLES20.glLineWidth(mThickness);
	}
}
