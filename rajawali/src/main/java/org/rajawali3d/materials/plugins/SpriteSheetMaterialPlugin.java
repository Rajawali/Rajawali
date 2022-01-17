package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.math.MathUtil;

import android.opengl.GLES20;
import android.os.SystemClock;

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

	// plays backwards when startFrame > endFrame
	public void setRange(int startFrame, int endFrame, float fps) {
		mVertexShader.setRange(startFrame,endFrame,fps);
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
		
		private int mNumCols = 1;
		private int mNumRows = 1;
		private float mFPS = 15;
		private int mStartFrame = 0;
		private int mEndFrame;
		
		public SpriteSheetVertexShaderFragment(int numCols, int numRows)
		{
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
			if(numCols>0) mNumCols = numCols;
			if(numRows>0) mNumRows = numRows;
			mEndFrame = mNumCols * mNumRows;
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
			if(mStartFrame==mEndFrame) return;

			long mStartTime = 0;
			long t = SystemClock.elapsedRealtime() - mStartTime;
			int mCurrentFrame = (int) Math.floor(t * mFPS / 1e3f);
			if(mEndFrame>mStartFrame) {
				mCurrentFrame %= (mEndFrame - mStartFrame);
				mCurrentFrame += mStartFrame;
			} else { // running backwards
				mCurrentFrame %= (mStartFrame - mEndFrame);
				mCurrentFrame = (mStartFrame - mCurrentFrame);
			}

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

		// plays backwards when startFrame > endFrame
		public void setRange(int startFrame, int endFrame, float fps) {
			mStartFrame = MathUtil.clamp(startFrame, 0, mNumCols*mNumRows-1);
			mEndFrame = MathUtil.clamp(endFrame, 0, mNumCols*mNumRows-1);
			if(fps>0) mFPS = fps;
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
