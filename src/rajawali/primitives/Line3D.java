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
package rajawali.primitives;

import java.util.Stack;

import rajawali.Object3D;
import rajawali.math.vector.Vector3;
import android.graphics.Color;
import android.opengl.GLES20;

/**
 * The Line3D takes a list of Vector3 points, thickness and a color.
 * <p>
 * Usage:
 * <pre><code>
 * Stack&lt;Vector3&gt; points = new Stack&lt;Vector3&gt;();
 * int[] colors = new int[3];
 * 
 * points.add(new Vector3(-2, 0, 1));
 * points.add(new Vector3(-1, -1, 2));
 * points.add(new Vector3(0, 2, 4));
 * 
 * colors[0] = 0xffff0000; // red
 * colors[1] = 0xff00ff00; // green
 * colors[2] = 0xffffff00; // yellow
 * 
 * Line3D line = new Line3D(points, 1, colors);
 * SimpleMaterial material = new SimpleMaterial();
 * material.setUseVertexColor(true);
 * line.setMaterial(material);
 * getCurrentScene.addChild(line);
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class Line3D extends Object3D {
	private Stack<Vector3> mPoints;
	private float mThickness;
	private int[] mColors;
	
	/**
	 * Creates a line primitive.
	 * 
	 * @param points
	 * @param thickness
	 */
	public Line3D(Stack<Vector3> points, float thickness)
	{
		this(points, thickness, null);
	}
	
	/**
	 * Creates a line primitive with a single color.
	 * 
	 * @param points
	 * @param thickness
	 * @param color
	 */
	public Line3D(Stack<Vector3> points, float thickness, int color) 
	{
		this(points, thickness, null);
		setColor(color);
	}
	
	/**
	 * Creates a line primitive with a specified color for each point.
	 * 
	 * @param points
	 * @param thickness
	 * @param colors
	 */
	public Line3D(Stack<Vector3> points, float thickness, int[] colors)
	{
		super();
		mPoints = points;
		mThickness = thickness;
		mColors = colors;
		if(colors != null && colors.length != points.size())
			throw new RuntimeException("The number of line points and colors is not the same.");
		init();
	}
	
	public Vector3 getPoint(int point) {
		return mPoints.get(point);
	}
	
	private void init() {
		setDoubleSided(true);
		setDrawingMode(GLES20.GL_LINE_STRIP);
		
		int numVertices = mPoints.size();
		
		float[] vertices = new float[numVertices * 3];
		int[] indices = new int[numVertices];
		float[] colors = null;
		
		if(mColors != null)
			colors = new float[mColors.length * 4];
		
		for(int i=0; i<numVertices; i++) {
			Vector3 point = mPoints.get(i);
			int index = i * 3;
			vertices[index] = (float) point.x;
			vertices[index+1] = (float) point.y;
			vertices[index+2] = (float) point.z;
			index = i * 2;
			index = i * 4;
			indices[i] = (short)i;
			
			if(mColors != null)
			{
				int color = mColors[i];
				int colorIndex = i * 4;
				colors[colorIndex] = Color.red(color) / 255.f;
				colors[colorIndex + 1] = Color.green(color) / 255.f;
				colors[colorIndex + 2] = Color.blue(color) / 255.f;
				colors[colorIndex + 3] = Color.alpha(color) / 255.f;
			}
		}
		
		setData(vertices, null, null, colors, indices);
		
		vertices = null;
		colors = null;
		indices = null;
	}
	
	public void preRender() {
		super.preRender();
		GLES20.glLineWidth(mThickness);
	}
}
