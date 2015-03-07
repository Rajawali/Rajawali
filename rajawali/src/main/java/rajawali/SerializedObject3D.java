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
package rajawali;

import java.io.Serializable;

public class SerializedObject3D implements Serializable {
	private static final long serialVersionUID = 5264861128471177349L;

	protected float[] mVertices;
	protected float[] mNormals;
	protected float[] mTextureCoords;
	protected float[] mColors;
	protected int[] mIndices;
	protected float[][] mFrameNormals;
	protected float[][] mFrameVertices;
	protected String[] mFrameNames;
	
	public SerializedObject3D(int numVertices, int numNormals, int numTextureCoords, int numColors, int numIndices) {
		mVertices = new float[numVertices];
		mNormals = new float[numNormals];
		mTextureCoords = new float[numTextureCoords];
		mColors = new float[numColors];
		mIndices = new int[numIndices];
	}
	
	public float[] getVertices() {
		return mVertices;
	}
	public void setVertices(float[] vertices) {
		this.mVertices = vertices;
	}
	public float[] getNormals() {
		return mNormals;
	}
	public void setNormals(float[] normals) {
		this.mNormals = normals;
	}
	public float[] getTextureCoords() {
		return mTextureCoords;
	}
	public void setTextureCoords(float[] textureCoords) {
		this.mTextureCoords = textureCoords;
	}
	public int[] getIndices() {
		return mIndices;
	}
	public void setIndices(int[] indices) {
		this.mIndices = indices;
	}

	public float[] getColors() {
		return mColors;
	}

	public void setColors(float[] colors) {
		this.mColors = colors;
	}
	
	public void setFrameNames(String[] frameNames) {
		this.mFrameNames = frameNames;
	}
	
	public String[] getFrameNames() {
		return mFrameNames;
	}
	
	public void setFrameVertices(float[][] frameVertices) {
		this.mFrameVertices = frameVertices;
	}
	
	public float[][] getFrameVertices() {
		return mFrameVertices;
	}
	
	public void setFrameNormals(float[][] frameNormals) {
		this.mFrameNormals = frameNormals;
	}
	
	public float[][] getFrameNormals() {
		return mFrameNormals;
	}
}
