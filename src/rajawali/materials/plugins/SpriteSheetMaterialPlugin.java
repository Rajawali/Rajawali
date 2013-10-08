package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import android.opengl.GLES20;
import android.os.SystemClock;


public class SpriteSheetMaterialPlugin implements IMaterialPlugin {
	private SpriteSheetVertexShaderFragment mVertexShader;

	public SpriteSheetMaterialPlugin(int numTilesX, int numTilesY, int numFrames)
	{
        this(numTilesX, numTilesY, 30,numFrames);
	}

	public SpriteSheetMaterialPlugin(int numTilesX, int numTilesY, float fps, int numFrames)
	{
        mVertexShader = new SpriteSheetVertexShaderFragment();
        mVertexShader.setNumTiles(numTilesX, numTilesY);
        mVertexShader.setNumFrames(numFrames);
		mVertexShader.setFPS(fps);
	}

	public SpriteSheetMaterialPlugin(int numTilesX, int numTilesY, long[] frameDurations)
	{
		this(numTilesX, numTilesY, frameDurations.length);
		mVertexShader.setFrameDurations(frameDurations);
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

	public void pause(int frameIndex) {
		mVertexShader.pause(frameIndex);
	}

	public boolean isPlaying() {
		return mVertexShader.isPlaying();
	}

	public void setLoop(boolean loop) {
		mVertexShader.setLoop(loop);
	}

	public boolean getLoop() {
		return mVertexShader.getLoop();
	}

    public void setPingPong(boolean pingPong) {
        mVertexShader.setPingPong(pingPong);
    }

    public boolean getPingPong() {
        return mVertexShader.getPingPong();
    }

	private final class SpriteSheetVertexShaderFragment extends AShader implements IShaderFragment
	{
		public final static String SHADER_ID = "SPRITE_SHEET_VERTEX_SHADER_FRAGMENT";

		private static final String U_CURRENT_FRAME = "uCurrentFrame";
		private static final String U_NUM_TILES = "uNumTiles";

		private RFloat muCurrentFrame;
		private RVec2 muNumTiles;

		private int muCurrentFrameHandle;
		private int muNumTilesHandle;

		private float mCurrentFrame;
		private float[] mNumTiles = new float[2];

		private long mStartTime;
		private boolean mIsPlaying = false;
		private float mFrameIndexStop = 0;
		private int mNumFrames;
		private long[] mFrameDurations;
		private int mDuration;
		private boolean mLoop = true;
        private boolean mPingPong=false;

		public SpriteSheetVertexShaderFragment()
		{
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
			initialize();
		}

		@Override
		public void initialize()
		{
			super.initialize();

			muCurrentFrame = (RFloat) addUniform(U_CURRENT_FRAME, DataType.FLOAT);
			muNumTiles = (RVec2) addUniform(U_NUM_TILES, DataType.VEC2);
		}

		@Override
		public void setLocations(int programHandle) {
			muCurrentFrameHandle = getUniformLocation(programHandle, U_CURRENT_FRAME);
			muNumTilesHandle = getUniformLocation(programHandle, U_NUM_TILES);
		}

		@Override
		public void applyParams() {
			super.applyParams();

			if ((mIsPlaying) || (!mIsPlaying && mFrameIndexStop != mCurrentFrame)) {
				if (mFrameDurations != null && mFrameDurations.length > 0) {
					int time;
                    if(mPingPong){
                         time = (int) Math.floor((SystemClock.elapsedRealtime() - mStartTime) % getDurationPingPong());
                    }else{
                         time = (int) Math.floor((SystemClock.elapsedRealtime() - mStartTime) % mDuration);
                    }

					mCurrentFrame = getFrame(time);
				}
				if (!mLoop && mCurrentFrame == (mNumFrames - 1)) {
					pause();
				}
			}
			GLES20.glUniform1f(muCurrentFrameHandle, mCurrentFrame);
			GLES20.glUniform2fv(muNumTilesHandle, 1, mNumTiles, 0);
		}

		@Override
		public void main() {
			RVec2 gTextureCoord = (RVec2) getGlobal(DefaultShaderVar.G_TEXTURE_COORD);

			RFloat tileSizeX = new RFloat("tileSizeX");
			tileSizeX.assign(1.f / mNumTiles[0]);
			RFloat tileSizeY = new RFloat("tileSizeY");
			tileSizeY.assign(1.f / mNumTiles[1]);

			RFloat texSOffset = new RFloat("texSOffset", gTextureCoord.s().multiply(tileSizeX));
			RFloat texTOffset = new RFloat("texTOffset", gTextureCoord.t().multiply(tileSizeY));
			gTextureCoord.s().assign(mod(muCurrentFrame, muNumTiles.x()).multiply(tileSizeX).add(texSOffset));
			gTextureCoord.t().assign(tileSizeY.multiply(floor(muCurrentFrame.divide(muNumTiles.x()))).add(texTOffset));
		}

		public void setNumTiles(float numTilesX, float numTilesY) {
			mNumTiles[0] = numTilesX;
			mNumTiles[1] = numTilesY;
		}

		public void setFPS(float fps)
		{
            long[] frameDurations = new long[mNumFrames];
            for(int i=0;i<mNumFrames;i++){
                frameDurations[i]=(long)(1000.0f/fps);
            }
            setFrameDurations(frameDurations);
		}

		public void setFrameDurations(long[] frameDurations) {
			mFrameDurations = frameDurations;
			mDuration = 0;
			for (int i = 0; i < mNumFrames; i++)
			{
				mDuration += frameDurations[i];
			}
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
			mFrameIndexStop = mCurrentFrame;
		}

		public void pause(int frameIndex) {
			mIsPlaying = false;
			mFrameIndexStop = frameIndex;
		}

		public boolean isPlaying() {
			return mIsPlaying;
		}

		public String getShaderId() {
			return SHADER_ID;
		}

		private int getFrame(int time) {
			int index=0;
			int timeFrame = 0;
            if(mPingPong && time> mDuration){
                time = (mDuration-(int)mFrameDurations[mNumFrames-1])-(time-mDuration);
            }

			while (index < mNumFrames && time > (timeFrame + mFrameDurations[index])) {
				timeFrame += mFrameDurations[index];
				index++;
			}
			return index;
		}

		public void setLoop(boolean loop) {
			mLoop = loop;
		}

		public boolean getLoop() {
			return mLoop;
		}

        public void setPingPong(boolean pingPong) {
            mPingPong=pingPong;
        }

        public boolean getPingPong() {
            return mPingPong;
        }

        private int getDurationPingPong(){
            int duration;
            duration=mDuration*2;
            duration-=mFrameDurations[0];
            duration-=mFrameDurations[mNumFrames-1];
            return duration;
        }
	}
}