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
package rajawali.materials.shaders;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.plugins.SkeletalAnimationMaterialPlugin.SkeletalAnimationShaderVar;
import rajawali.materials.shaders.fragments.animation.SkeletalAnimationVertexShaderFragment;
import rajawali.math.Matrix4;
import android.graphics.Color;
import android.opengl.GLES20;

public class VertexShader extends AShader {

	private RMat4 muMVPMatrix;
	private RMat3 muNormalMatrix;
	@SuppressWarnings("unused")
	private RMat4 muModelMatrix;
	private RMat4 muModelViewMatrix;
	private RVec4 muColor;

	private RVec2 maTextureCoord;
	private RVec3 maNormal;
	private RVec4 maPosition;
	private RVec4 maVertexColor;

	private RVec2 mvTextureCoord;
	private RVec3 mvCubeTextureCoord;
	private RVec3 mvNormal;
	private RVec4 mvColor;
	private RVec3 mvEyeDir;

	private RVec4 mgPosition;
	private RVec3 mgNormal;
	private RVec4 mgColor;
	private RVec2 mgTextureCoord;

	private int muMVPMatrixHandle;
	private int muNormalMatrixHandle;
	private int muModelMatrixHandle;
	private int muModelViewMatrixHandle;
	private int muColorHandle;
	private int muTimeHandle;

	private int maTextureCoordHandle;
	@SuppressWarnings("unused")
	private int maCubeTextureCoordHandle;
	private int maNormalHandle;
	private int maPositionHandle;
	private int maVertexColorBufferHandle;

	private float[] mColor = new float[] { 1, 0, 0, 1 };
	private float mTime;
	@SuppressWarnings("unused")
	private List<ALight> mLights;
	private boolean mHasCubeMaps;
	private boolean mUseVertexColors;
	private boolean mTimeEnabled;

	public VertexShader()
	{
		super(ShaderType.VERTEX);
	}
	
	public VertexShader(int resourceId)
	{
		super(ShaderType.VERTEX, resourceId);
	}
	
	public VertexShader(String shaderString)
	{
		super(ShaderType.VERTEX, shaderString);
	}

	@Override
	public void initialize()
	{
		super.initialize();

		addPrecisionQualifier(DataType.FLOAT, Precision.MEDIUMP);

		// -- uniforms

		muMVPMatrix = (RMat4) addUniform(DefaultShaderVar.U_MVP_MATRIX);
		muNormalMatrix = (RMat3) addUniform(DefaultShaderVar.U_NORMAL_MATRIX);
		muModelMatrix = (RMat4) addUniform(DefaultShaderVar.U_MODEL_MATRIX);
		muModelViewMatrix = (RMat4) addUniform(DefaultShaderVar.U_MODEL_VIEW_MATRIX);
		muColor = (RVec4) addUniform(DefaultShaderVar.U_COLOR);
		if(mTimeEnabled)
			addUniform(DefaultShaderVar.U_TIME);

		// -- attributes

		maTextureCoord = (RVec2) addAttribute(DefaultShaderVar.A_TEXTURE_COORD);
		maNormal = (RVec3) addAttribute(DefaultShaderVar.A_NORMAL);
		maPosition = (RVec4) addAttribute(DefaultShaderVar.A_POSITION);
		if (mUseVertexColors)
			maVertexColor = (RVec4) addAttribute(DefaultShaderVar.A_VERTEX_COLOR);

		// -- varyings

		mvTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);
		if (mHasCubeMaps)
			mvCubeTextureCoord = (RVec3) addVarying(DefaultShaderVar.V_CUBE_TEXTURE_COORD);
		mvNormal = (RVec3) addVarying(DefaultShaderVar.V_NORMAL);
		mvColor = (RVec4) addVarying(DefaultShaderVar.V_COLOR);
		mvEyeDir = (RVec3) addVarying(DefaultShaderVar.V_EYE_DIR);

		// -- globals

		mgPosition = (RVec4) addGlobal(DefaultShaderVar.G_POSITION);
		mgNormal = (RVec3) addGlobal(DefaultShaderVar.G_NORMAL);
		mgColor = (RVec4) addGlobal(DefaultShaderVar.G_COLOR);
		mgTextureCoord = (RVec2) addGlobal(DefaultShaderVar.G_TEXTURE_COORD);
	}

	@Override
	public void main() {
		mgPosition.assign(maPosition);
		mgNormal.assign(maNormal);
		mgTextureCoord.assign(maTextureCoord);
		if(mUseVertexColors)
			mgColor.assign(maVertexColor);
		else
			mgColor.assign(muColor);

		// -- do fragment stuff
		boolean hasSkeletalAnimation = false;

		for (int i = 0; i < mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			if(fragment.getInsertLocation() == PluginInsertLocation.POST_TRANSFORM) continue;
			fragment.setStringBuilder(mShaderSB);
			fragment.main();
			if(fragment.getShaderId().equals(SkeletalAnimationVertexShaderFragment.SHADER_ID))
				hasSkeletalAnimation = true;
		}

		if(hasSkeletalAnimation)
		{
			RMat4 transfMatrix = (RMat4) getGlobal(SkeletalAnimationShaderVar.G_BONE_TRANSF_MATRIX);
			GL_POSITION.assign(muMVPMatrix.multiply(transfMatrix).multiply(mgPosition));
			mvNormal.assign(normalize(muNormalMatrix.multiply(castMat3(transfMatrix)).multiply(mgNormal)));
		}
		else
		{
			GL_POSITION.assign(muMVPMatrix.multiply(mgPosition));
			mvNormal.assign(normalize(muNormalMatrix.multiply(mgNormal)));
		}
		
		mvTextureCoord.assign(mgTextureCoord);
		if (mHasCubeMaps)
			mvCubeTextureCoord.assign(castVec3(maPosition));
		mvColor.assign(mgColor);
		mvEyeDir.assign(castVec3(muModelViewMatrix.multiply(mgPosition)));
		
		for (int i = 0; i < mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			if(fragment.getInsertLocation() == PluginInsertLocation.POST_TRANSFORM) {
				fragment.setStringBuilder(mShaderSB);
				fragment.main();
			}
		}
	}

	@Override
	public void applyParams()
	{
		super.applyParams();

		GLES20.glUniform4fv(muColorHandle, 1, mColor, 0);
		GLES20.glUniform1f(muTimeHandle, mTime);
	}

	@Override
	public void setLocations(final int programHandle) {
		maTextureCoordHandle = getAttribLocation(programHandle, DefaultShaderVar.A_TEXTURE_COORD);
		maNormalHandle = getAttribLocation(programHandle, DefaultShaderVar.A_NORMAL);
		maPositionHandle = getAttribLocation(programHandle, DefaultShaderVar.A_POSITION);
		if (mUseVertexColors)
			maVertexColorBufferHandle = getAttribLocation(programHandle, DefaultShaderVar.A_VERTEX_COLOR);

		muMVPMatrixHandle = getUniformLocation(programHandle, DefaultShaderVar.U_MVP_MATRIX);
		muNormalMatrixHandle = getUniformLocation(programHandle, DefaultShaderVar.U_NORMAL_MATRIX);
		muModelMatrixHandle = getUniformLocation(programHandle, DefaultShaderVar.U_MODEL_MATRIX);
		muModelViewMatrixHandle = getUniformLocation(programHandle, DefaultShaderVar.U_MODEL_VIEW_MATRIX);
		muColorHandle = getUniformLocation(programHandle, DefaultShaderVar.U_COLOR);
		muTimeHandle = getUniformLocation(programHandle, DefaultShaderVar.U_TIME);

		super.setLocations(programHandle);
	}

	public void setVertices(final int vertexBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setTextureCoords(final int textureCoordBufferHandle) {
		if(maTextureCoordHandle < 0) return;
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordBufferHandle);
		GLES20.glEnableVertexAttribArray(maTextureCoordHandle);
		GLES20.glVertexAttribPointer(maTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setNormals(final int normalBufferHandle) {
		if(maNormalHandle < 0) return;
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
		GLES20.glEnableVertexAttribArray(maNormalHandle);
		GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
	}
	
	public void setVertexColors(final int vertexColorBufferHandle) {
		if(maVertexColorBufferHandle < 0) return;
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexColorBufferHandle);
		GLES20.glEnableVertexAttribArray(maVertexColorBufferHandle);
		GLES20.glVertexAttribPointer(maVertexColorBufferHandle, 4, GLES20.GL_FLOAT,
				false, 0, 0);
	}

	public void setMVPMatrix(float[] mvpMatrix) {
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0);
	}

	public void setModelMatrix(Matrix4 modelMatrix) {
		GLES20.glUniformMatrix4fv(muModelMatrixHandle, 1, false, modelMatrix.getFloatValues(), 0);
	}

	public void setNormalMatrix(float[] normalMatrix) {
		GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false, normalMatrix, 0);
	}

	public void setModelViewMatrix(float[] modelViewMatrix) {
		GLES20.glUniformMatrix4fv(muModelViewMatrixHandle, 1, false, modelViewMatrix, 0);
	}

	public void setColor(int color) {
		mColor[0] = (float) Color.red(color) / 255.f;
		mColor[1] = (float) Color.green(color) / 255.f;
		mColor[2] = (float) Color.blue(color) / 255.f;
		mColor[3] = (float) Color.alpha(color) / 255.f;
	}

	public void setColor(float[] color) {
		mColor[0] = color[0];
		mColor[1] = color[1];
		mColor[2] = color[2];
		mColor[3] = color[3];
	}

	public int getColor() {
		return Color.argb((int) (mColor[3] * 255), (int) (mColor[0] * 255), (int) (mColor[1] * 255),
				(int) (mColor[2] * 255));
	}

	public void setLights(List<ALight> lights)
	{
		mLights = lights;
	}
	
	public void hasCubeMaps(boolean value)
	{
		mHasCubeMaps = value;
	}
	
	public void useVertexColors(boolean value)
	{
		mUseVertexColors = value;
	}
	
	public void enableTime(boolean value)
	{
		mTimeEnabled = value;
	}
	
	public void setTime(float time)
	{
		mTime = time;
	}
}
