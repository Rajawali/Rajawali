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
package org.rajawali3d.primitives;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;

/**
 * Basic primitive allowing for the creation of an n-sided regular
 * polygonal cone, as a frustum or to a point with a specified slant
 * angle or aspect ratio. The cone is created about the positive
 * y axis with the vanishing point at (0, height, 0).
 * 
 * NOTE: This still needs a lot of work. Texture coordinates are not correct.
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class NPrism extends Object3D {

	protected int mSideCount;
	protected double mRadiusBase;
	protected double mRadiusTop;
	protected double mMinorBase;
	protected double mMinorTop;
	protected double mHeight;
	protected double mEccentricity;
	
	private static final Vector3 UP = new Vector3(0, 1, 0);
	private static final Vector3 DOWN = new Vector3(0, -1, 0);

    private int mVertexIndex;
    private int mTextureIndex;
    private int mNormalIndex;
    private int mColorIndex;

	/**
	 * Creates a terminated prism.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radius Double the radius of the base.
	 * @param height Double the height of the prism.
	 */
	public NPrism(int sides, double radius, double height) {
		this(sides, 0, radius, height);
	}
	
	/**
	 * Creates a frustum like prism.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radiusTop Double the radius of the top.
	 * @param radiusBase Double the radius of the base.
	 * @param height Double the height of the prism.
	 */
	public NPrism(int sides, double radiusTop, double radiusBase, double height) {
		this(sides, radiusTop, radiusBase, 0.0, height, true);
	}

    /**
     * Creates a frustum like prism with elliptical base rather than circular.
     * The major axis is equivalent to the radius specified and the minor axis
     * is computed from the eccentricity.
     *
     * @param sides        Integer number of sides to the prism.
     * @param radiusTop    Double the radius of the top.
     * @param radiusBase   Double the radius of the base.
     * @param eccentricity Double the eccentricity of the ellipse.
     * @param height       Double the height of the prism.
     */
    public NPrism(int sides, double radiusTop, double radiusBase, double eccentricity, double height) {
        this(sides, radiusTop, radiusBase, eccentricity, height, true);
    }
	
	/**
	 * Creates a frustum like prism with elliptical base rather than circular. 
	 * The major axis is equivalent to the radius specified and the minor axis
	 * is computed from the eccentricity.
	 * 
	 * @param sides Integer number of sides to the prism.
	 * @param radiusTop Double the radius of the top.
	 * @param radiusBase Double the radius of the base.
	 * @param eccentricity Double the eccentricity of the ellipse.
	 * @param height Double the height of the prism.
     * @param createVBOs Boolean If true, the VBOs are created immediately.
	 */
	public NPrism(int sides, double radiusTop, double radiusBase, double eccentricity, double height, boolean createVBOs) {
		if (sides < 3) throw new IllegalArgumentException("Prisms must have at least 3 sides!");
		if ((eccentricity < 0) || (eccentricity >= 1)) throw new IllegalArgumentException("Eccentricity must be in the range [0,1)");
		mSideCount = sides;
		mEccentricity = eccentricity;
		mRadiusTop = radiusTop;
		mMinorTop = calculateMinorAxis(mRadiusTop);
		mRadiusBase = radiusBase;
		mMinorBase = calculateMinorAxis(mRadiusBase);
		mHeight = height;
		init(createVBOs);
	}
	
	protected double calculateMinorAxis(double major) {
		return Math.sqrt(Math.pow(major, 2.0)*(1 - Math.pow(mEccentricity, 2.0)));
	}

    private void setIndices(final int triangle) {
        mVertexIndex = 9*triangle; // 3 vertices per triangle
        mTextureIndex = 6*triangle;
        mNormalIndex = 9*triangle;
        mColorIndex = 12*triangle;
    }

	protected void init(boolean createVBOs) {
		int vertex_count = 6*mSideCount + 6*mSideCount; // Six vertices per side plus 3 per side per top/bottom
		int tri_count = 2*mSideCount + 2*mSideCount; //2 per side plus 1 per side per top/bottom

		int triangle = 0;
		float[] vertices = new float[3*vertex_count];
		float[] normals = new float[3*vertex_count];
		float[] texture = new float[2*vertex_count];
		float[] colors = new float[4*vertex_count];
		int[] indices = new int[3*tri_count];
		
		double angle_delta = 2*Math.PI/mSideCount;

		// Populate the vertices
        int indexIndex = 0; // Increments by 3

        // If the side count is even, we want to start at half the angle or we will appear rotated.
		final double angle0 = (mSideCount % 2 == 0) ? angle_delta/2.0 : 0;
		
        final Vector3 vertex0 = new Vector3();
        final Vector3 vertex1 = new Vector3();
        final Vector3 vertex2 = new Vector3();
        final Vector3 scratch0 = new Vector3();
        final Vector3 scratch1 = new Vector3();
        final Vector3 temp_normal = new Vector3();
		for (int side = 0; side < mSideCount; ++side) {
			// Handle the side
            // Even Triangle
            setIndices(triangle);
            vertex0.x = mRadiusTop * Math.cos(angle0 + side * angle_delta);
            vertex0.y = mHeight/2.0;
            vertex0.z = mMinorTop * Math.sin(angle0 + side * angle_delta);
            vertex1.x = mRadiusTop * Math.cos(angle0 + (side + 1) * angle_delta);
            vertex1.y = vertex0.y;
            vertex1.z = mMinorTop * Math.sin(angle0 + (side + 1) * angle_delta);
            vertex2.x = mRadiusBase * Math.cos(angle0 + side * angle_delta);
            vertex2.y = -vertex0.y;
            vertex2.z = mMinorBase * Math.sin(angle0 + side * angle_delta);

            scratch0.subtractAndSet(vertex0, vertex1);
            scratch1.subtractAndSet(vertex0, vertex2);
            temp_normal.crossAndSet(scratch1, scratch0);
            temp_normal.normalize();
            // Vertex 0
            vertices[mVertexIndex] = (float) vertex0.x;
            vertices[mVertexIndex + 1] = (float) vertex0.y;
            vertices[mVertexIndex + 2] = (float) vertex0.z;
            normals[mNormalIndex] = (float) temp_normal.x;
            normals[mNormalIndex + 1] = (float) temp_normal.y;
            normals[mNormalIndex + 2] = (float) temp_normal.z;
            texture[mTextureIndex] = (float) Math.cos(angle0 + side * angle_delta);
            texture[mTextureIndex + 1] = 1.0f;
            // Vertex 1
            vertices[mVertexIndex + 3] = (float) vertex1.x;
            vertices[mVertexIndex + 4] = (float) vertex1.y;
            vertices[mVertexIndex + 5] = (float) vertex1.z;
            normals[mNormalIndex + 3] = (float) temp_normal.x;
            normals[mNormalIndex + 4] = (float) temp_normal.y;
            normals[mNormalIndex + 5] = (float) temp_normal.z;
            texture[mTextureIndex + 2] = (float) Math.cos(angle0 + (side + 1) * angle_delta);
            texture[mTextureIndex + 3] = 1.0f;
            // Vertex 2
            vertices[mVertexIndex + 6] = (float) vertex2.x;
            vertices[mVertexIndex + 7] = (float) vertex2.y;
            vertices[mVertexIndex + 8] = (float) vertex2.z;
            normals[mNormalIndex + 6] = (float) temp_normal.x;
            normals[mNormalIndex + 7] = (float) temp_normal.y;
            normals[mNormalIndex + 8] = (float) temp_normal.z;
            texture[mTextureIndex + 4] = (float) Math.cos(angle0 + side * angle_delta);
            texture[mTextureIndex + 5] = 0.0f;
            indices[indexIndex] = indexIndex;
            indices[indexIndex + 1] = indexIndex + 1;
            indices[indexIndex + 2] = indexIndex + 2;
            indexIndex += 3;
            ++triangle;

            // Odd Triangle
            setIndices(triangle);
            vertex0.x = vertex2.x;
            vertex0.y = vertex2.y;
            vertex0.z = vertex2.z;
            // Vertex 1 is the same as the even triangle
            vertex2.x = mRadiusBase * Math.cos(angle0 + (side + 1) * angle_delta);
            vertex2.y = -mHeight/2.0;
            vertex2.z = mMinorBase * Math.sin(angle0 + (side + 1) * angle_delta);
            scratch0.subtractAndSet(vertex2, vertex0);
            scratch1.subtractAndSet(vertex2, vertex1);
            temp_normal.crossAndSet(scratch1, scratch0);
            temp_normal.normalize();
            // Vertex 0
            vertices[mVertexIndex] = (float) vertex0.x;
            vertices[mVertexIndex + 1] = (float) vertex0.y;
            vertices[mVertexIndex + 2] = (float) vertex0.z;
            normals[mNormalIndex] = (float) temp_normal.x;
            normals[mNormalIndex + 1] = (float) temp_normal.y;
            normals[mNormalIndex + 2] = (float) temp_normal.z;
            texture[mTextureIndex] = (float) Math.cos(angle0 + side * angle_delta);
            texture[mTextureIndex + 1] = 0.0f;
            // Vertex 1
            vertices[mVertexIndex + 3] = (float) vertex1.x;
            vertices[mVertexIndex + 4] = (float) vertex1.y;
            vertices[mVertexIndex + 5] = (float) vertex1.z;
            normals[mNormalIndex + 3] = (float) temp_normal.x;
            normals[mNormalIndex + 4] = (float) temp_normal.y;
            normals[mNormalIndex + 5] = (float) temp_normal.z;
            texture[mTextureIndex + 2] = (float) Math.cos(angle0 + (side + 1) * angle_delta);
            texture[mTextureIndex + 3] = 1.0f;
            // Vertex 2
            vertices[mVertexIndex + 6] = (float) vertex2.x;
            vertices[mVertexIndex + 7] = (float) vertex2.y;
            vertices[mVertexIndex + 8] = (float) vertex2.z;
            normals[mNormalIndex + 6] = (float) temp_normal.x;
            normals[mNormalIndex + 7] = (float) temp_normal.y;
            normals[mNormalIndex + 8] = (float) temp_normal.z;
            texture[mTextureIndex + 4] = (float) Math.cos(angle0 + (side + 1) * angle_delta);
            texture[mTextureIndex + 5] = 0.0f;
            indices[indexIndex] = indexIndex;
            indices[indexIndex + 1] = indexIndex + 1;
            indices[indexIndex + 2] = indexIndex + 2;
            indexIndex += 3;
            ++triangle;

            // Handle the top
            setIndices(triangle);
            vertex0.x = mRadiusTop * Math.cos(angle0 + side * angle_delta);
            vertex0.y = mHeight / 2.0;
            vertex0.z = mMinorTop * Math.sin(angle0 + side * angle_delta);
            vertex1.x = 0;
            vertex1.y = vertex0.y;
            vertex1.z = 0;
            vertex2.x = mRadiusTop * Math.cos(angle0 + (side + 1) * angle_delta);
            vertex2.y = vertex0.y;
            vertex2.z = mMinorTop * Math.sin(angle0 + (side + 1) * angle_delta);
            temp_normal.x = 0;
            temp_normal.y = 1.0;
            temp_normal.z = 0;
            // Vertex 0
            vertices[mVertexIndex] = (float) vertex0.x;
            vertices[mVertexIndex + 1] = (float) vertex0.y;
            vertices[mVertexIndex + 2] = (float) vertex0.z;
            normals[mNormalIndex] = (float) temp_normal.x;
            normals[mNormalIndex + 1] = (float) temp_normal.y;
            normals[mNormalIndex + 2] = (float) temp_normal.z;
            texture[mTextureIndex] = (float) Math.cos(angle0 + side * angle_delta);
            texture[mTextureIndex + 1] = 1.0f;
            // Vertex 1
            vertices[mVertexIndex + 3] = (float) vertex1.x;
            vertices[mVertexIndex + 4] = (float) vertex1.y;
            vertices[mVertexIndex + 5] = (float) vertex1.z;
            normals[mNormalIndex + 3] = (float) temp_normal.x;
            normals[mNormalIndex + 4] = (float) temp_normal.y;
            normals[mNormalIndex + 5] = (float) temp_normal.z;
            texture[mTextureIndex + 2] = (float) Math.cos(angle0 + (side + 1) * angle_delta);
            texture[mTextureIndex + 3] = 1.0f;
            // Vertex 2
            vertices[mVertexIndex + 6] = (float) vertex2.x;
            vertices[mVertexIndex + 7] = (float) vertex2.y;
            vertices[mVertexIndex + 8] = (float) vertex2.z;
            normals[mNormalIndex + 6] = (float) temp_normal.x;
            normals[mNormalIndex + 7] = (float) temp_normal.y;
            normals[mNormalIndex + 8] = (float) temp_normal.z;
            texture[mTextureIndex + 4] = (float) Math.cos(angle0 + side * angle_delta);
            texture[mTextureIndex + 5] = 1.0f;
            indices[indexIndex] = indexIndex;
            indices[indexIndex + 1] = indexIndex + 1;
            indices[indexIndex + 2] = indexIndex + 2;
            indexIndex += 3;
            ++triangle;

            // Handle the bottom
            setIndices(triangle);
            vertex0.x = mRadiusBase * Math.cos(angle0 + side * angle_delta);
            vertex0.y = -mHeight / 2.0;
            vertex0.z = mMinorBase * Math.sin(angle0 + side * angle_delta);
            vertex1.x = 0;
            vertex1.y = vertex0.y;
            vertex1.z = 0;
            vertex2.x = mRadiusBase * Math.cos(angle0 + (side + 1) * angle_delta);
            vertex2.y = vertex0.y;
            vertex2.z = mMinorBase * Math.sin(angle0 + (side + 1) * angle_delta);
            temp_normal.x = 0;
            temp_normal.y = -1.0;
            temp_normal.z = 0;
            // Vertex 0
            vertices[mVertexIndex] = (float) vertex0.x;
            vertices[mVertexIndex + 1] = (float) vertex0.y;
            vertices[mVertexIndex + 2] = (float) vertex0.z;
            normals[mNormalIndex] = (float) temp_normal.x;
            normals[mNormalIndex + 1] = (float) temp_normal.y;
            normals[mNormalIndex + 2] = (float) temp_normal.z;
            texture[mTextureIndex] = (float) Math.cos(angle0 + side * angle_delta);
            texture[mTextureIndex + 1] = 1.0f;
            // Vertex 1
            vertices[mVertexIndex + 3] = (float) vertex1.x;
            vertices[mVertexIndex + 4] = (float) vertex1.y;
            vertices[mVertexIndex + 5] = (float) vertex1.z;
            normals[mNormalIndex + 3] = (float) temp_normal.x;
            normals[mNormalIndex + 4] = (float) temp_normal.y;
            normals[mNormalIndex + 5] = (float) temp_normal.z;
            texture[mTextureIndex + 2] = (float) Math.cos(angle0 + (side + 1) * angle_delta);
            texture[mTextureIndex + 3] = 1.0f;
            // Vertex 2
            vertices[mVertexIndex + 6] = (float) vertex2.x;
            vertices[mVertexIndex + 7] = (float) vertex2.y;
            vertices[mVertexIndex + 8] = (float) vertex2.z;
            normals[mNormalIndex + 6] = (float) temp_normal.x;
            normals[mNormalIndex + 7] = (float) temp_normal.y;
            normals[mNormalIndex + 8] = (float) temp_normal.z;
            texture[mTextureIndex + 4] = (float) Math.cos(angle0 + side * angle_delta);
            texture[mTextureIndex + 5] = 0.0f;
            indices[indexIndex] = indexIndex;
            indices[indexIndex + 1] = indexIndex + 1;
            indices[indexIndex + 2] = indexIndex + 2;
            indexIndex += 3;
            ++triangle;
		}

		//Populate the colors
		for (int i = 0, j = 4*vertex_count; i < j; ++i) {
			colors[i] = 1.0f;
		}

		setData(vertices, normals, texture, colors, indices, createVBOs);
	}
}