package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.math.MathUtil;

import android.opengl.GLES20;
import android.os.SystemClock;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

public class SpriteSheetMaterialPlugin implements IMaterialPlugin {
	private final SpriteSheetVertexShaderFragment mVertexShader;
	
	public enum SpriteSheetShaderVar implements AShaderBase.IGlobalShaderVar {
		U_TILE_SIZE("uTileSize", AShaderBase.DataType.VEC2),
		U_TILE_OFFSET("uTileOffset", AShaderBase.DataType.VEC2);

		private final String mVarString;
		private final AShaderBase.DataType mDataType;

		SpriteSheetShaderVar(String varString, AShaderBase.DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}

		public String getVarString() {
			return mVarString;
		}

		public AShaderBase.DataType getDataType() {
			return mDataType;
		}
	}

	public void selectFrame(@FloatRange(from=0) double frame) {
		mVertexShader.selectFrame(Math.floor(frame));
	}

	public SpriteSheetMaterialPlugin(int numCols, int numRows)
	{
		mVertexShader = new SpriteSheetVertexShaderFragment(numCols, numRows);
	}
	
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.PRE_LIGHTING;
	}

	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	public IShaderFragment getFragmentShaderFragment() {
		return null;
	}

	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}

	private static final class SpriteSheetVertexShaderFragment extends AShader implements IShaderFragment
	{
		public final static String SHADER_ID = "SPRITE_SHEET_VERTEX_SHADER_FRAGMENT";
		
		private RVec2 muTileSize;
		private RVec2 muTileOffset;
		
		private int muTileSizeHandle;
		private int muTileOffsetHandle;
		
		private final float[] mTileSize   = { 1, 1 };
		private final float[] mTileOffset = { 0, 0 };

		private int mCurrentFrame = 0;
		private int mNumCols = 1;
		private int mNumRows = 1;
		
		public SpriteSheetVertexShaderFragment(@IntRange(from=0) int numCols, @IntRange(from=0) int numRows)
		{
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
			if(numCols>0) mNumCols = numCols;
			if(numRows>0) mNumRows = numRows;
			initialize();
		}

		@Override
		public void initialize()
		{
			super.initialize();
			muTileSize = (RVec2) addUniform(SpriteSheetShaderVar.U_TILE_SIZE);
			muTileOffset = (RVec2) addUniform(SpriteSheetShaderVar.U_TILE_OFFSET);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muTileSizeHandle = getUniformLocation(programHandle, SpriteSheetShaderVar.U_TILE_SIZE);
			muTileOffsetHandle = getUniformLocation(programHandle, SpriteSheetShaderVar.U_TILE_OFFSET);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();

			int col = mCurrentFrame % mNumCols;
			int row = mCurrentFrame / mNumRows;
			mTileOffset[0] = col;
			mTileOffset[1] = row;
			GLES20.glUniform2fv(muTileSizeHandle, 1, mTileSize, 0);

			mTileSize[0] = 1f/mNumCols;
			mTileSize[1] = 1f/mNumRows;
			GLES20.glUniform2fv(muTileOffsetHandle, 1, mTileOffset, 0);
		}
		
		@Override
		public void main() {
			RVec2 gTextureCoord = (RVec2) getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
			gTextureCoord.assignAdd(muTileOffset);
			gTextureCoord.assignMultiply(muTileSize);
		}

		public void selectFrame(@FloatRange(from=0) double frame) {
			mCurrentFrame = MathUtil.clamp((int)frame,0,mNumCols*mNumRows-1);
		}

		public String getShaderId() {
			return SHADER_ID;
		}
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.IGNORE;
		}
		
		@Override
		public void bindTextures(int nextIndex) {}
		@Override
		public void unbindTextures() {}
	}
}
