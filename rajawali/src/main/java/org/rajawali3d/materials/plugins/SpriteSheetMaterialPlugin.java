package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import android.opengl.GLES20;
import android.os.SystemClock;

public class SpriteSheetMaterialPlugin implements IMaterialPlugin {
	private SpriteSheetVertexShaderFragment mVertexShader;
	
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


	public SpriteSheetMaterialPlugin(int numTilesX, int numTilesY, float fps, int numFrames)
	{
		mVertexShader = new SpriteSheetVertexShaderFragment();
		mVertexShader.setNumTiles(numTilesX, numTilesY);
		mVertexShader.setFPS(fps);
		mVertexShader.setNumFrames(numFrames);
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
	
	public void play() {
		mVertexShader.play();
	}
	
	public void pause() {
		mVertexShader.pause();
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}

	private final class SpriteSheetVertexShaderFragment extends AShader implements IShaderFragment
	{
		public final static String SHADER_ID = "SPRITE_SHEET_VERTEX_SHADER_FRAGMENT";
		
		private RVec2 muTileSize;
		private RVec2 muTileOffset;
		
		private int muTileSizeHandle;
		private int muTileOffsetHandle;
		
		private float[] mTileSize   = { 1, 1 };
		private float[] mTileOffset = { 0, 0 };
		
		private int mNumCols = 1;
		private int mNumRows = 1;
		private int mCurrentFrame = 0;
		private long mStartTime = 0;
		private boolean mIsPlaying = false;
		private float mFPS = 30;
		private int mNumFrames = 1;
		
		public SpriteSheetVertexShaderFragment()
		{
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
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
			
			if(mIsPlaying) {
				long t = SystemClock.elapsedRealtime() - mStartTime;
				mCurrentFrame = (int)Math.floor(t * (mFPS / 1000.f));
				mCurrentFrame %= mNumFrames;

				int col = mCurrentFrame % mNumCols;
				int row = mCurrentFrame / mNumRows;
				mTileOffset[0] = col * mTileSize[0];
				mTileOffset[1] = row * mTileSize[1];
				GLES20.glUniform2fv(muTileSizeHandle, 1, mTileSize, 0);
				GLES20.glUniform2fv(muTileOffsetHandle, 1, mTileOffset, 0);
			}
		}
		
		@Override
		public void main() {
			RVec2 gTextureCoord = (RVec2) getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
			gTextureCoord.assignMultiply(muTileSize);
			gTextureCoord.assignAdd(muTileOffset);
		}
		
		public void setNumTiles(int numCols, int numRows) {
			if(numCols > 0) {
				mNumCols = numCols;
				mTileSize[0] = 1/(float)numCols;
			}
			if(numRows > 0) {
				mNumRows = numRows;
				mTileSize[1] = 1/(float)numRows;
			}
		}
		
		public void setFPS(float fps)
		{
			mFPS = fps;
		}
		
		public void setNumFrames(int numFrames) {
			mNumFrames = numFrames;
		}

		public void play() {
			mStartTime = SystemClock.elapsedRealtime();
			mIsPlaying = true;
		}
		
		public void pause() {
			mIsPlaying = false;
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
