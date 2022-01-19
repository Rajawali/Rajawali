package org.rajawali3d.materials.plugins;
import android.opengl.GLES20;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.MathUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class PointSpritePlugin implements IMaterialPlugin {
    private final PointSpriteVertexShaderFragment mVertexShader;
    private final PointSpriteFragmentShaderFragment mFragmentShader;

    public enum PointSpriteShaderVar implements AShaderBase.IGlobalShaderVar {
        U_APERTURE("uAperture", AShaderBase.DataType.FLOAT),
        U_START_FRAME("uStartFrame", AShaderBase.DataType.FLOAT),
        U_END_FRAME("uEndFrame", AShaderBase.DataType.FLOAT),

        U_POINT_SPRITE_TEXTURE("uPointSpriteTexture", AShaderBase.DataType.SAMPLER2D),
        V_BIRTH("vBirth", AShaderBase.DataType.FLOAT),
        V_DEATH("vDeath", AShaderBase.DataType.FLOAT),
        A_LIFETIME("aLifetime", AShaderBase.DataType.VEC2),
        V_TILE_SIZE("vTileSize", AShaderBase.DataType.VEC2),
        V_TILE_OFFSET("vTileOffset", AShaderBase.DataType.VEC2);

        private final String mVarString;
        private final AShaderBase.DataType mDataType;

        PointSpriteShaderVar(String varString, AShaderBase.DataType dataType) {
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

    public PointSpritePlugin(Texture SpriteSheet, int cols, int rows, FloatBuffer particleBuffer) {
        TextureManager.getInstance().addTexture(SpriteSheet);
        mVertexShader = new PointSpriteVertexShaderFragment(cols, rows, particleBuffer);
        mFragmentShader = new PointSpriteFragmentShaderFragment();
        mFragmentShader.setPointTexture(SpriteSheet);
    }

    public static class ParticleBuffer {
        private final Random mR = new Random();
        private final FloatBuffer mParticleBuffer;

        public ParticleBuffer(int numPoints, float start, float end, float lifetime, @FloatRange(from = 0.0D,to = 1.0D) float randomness) {
            mParticleBuffer = ByteBuffer
                    .allocateDirect(numPoints * 2 * Geometry3D.FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            for (int i = 0; i<numPoints; i++) {
                mParticleBuffer.put(start + end * variation(randomness));
                mParticleBuffer.put(start + end + lifetime * variation(randomness));
            }
            mParticleBuffer.position(0);
        }

        float variation(@FloatRange(from = 0.0d, to = 1.0d) float randomness) {
            return 1 + (2 * mR.nextFloat() - 1) * randomness;
        }

        public FloatBuffer getParticleBuffer() { return mParticleBuffer; }

        public int capacity() {
            return mParticleBuffer.capacity()/2;
        }
    }

    public void setRange(int startFrame, int endFrame, float aperture) {
        mVertexShader.setRange(startFrame, endFrame, aperture);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return mFragmentShader.getInsertLocation();
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return mVertexShader;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return mFragmentShader;
    }

    @Override
    public void bindTextures(int nextIndex) {
        mVertexShader.bindTextures(nextIndex);
        mFragmentShader.bindTextures(nextIndex);
    }

    @Override
    public void unbindTextures() {
        mFragmentShader.unbindTextures();
    }

    private static final class PointSpriteVertexShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "POINT_SPRITE_VERTEX_SHADER_FRAGMENT";
        float mAperture = 255.0f;
        int mStartFrame = 0;
        int mEndFrame;

        int mCols = 1;
        int mRows = 1;
        float[] mTileSize = { 1,1 };

        int muApertureHandle;
        int muStartFrameHandle;
        int muEndFrameHandle;
        int maLivesHandle;
        int mLivesBufferHandle;

        RFloat muAperture;
        RFloat muStartFrame;
        RFloat muEndFrame;
        RVec2 mvTileSize;
        RVec2 mvTileOffset;

        FloatBuffer mLivesBuffer;
        RVec2 maLife;
        RFloat mvBirth;
        RFloat mvDeath;

        public PointSpriteVertexShaderFragment(@IntRange(from = 0) int cols, @IntRange(from = 0) int rows, @NonNull FloatBuffer particleBuffer) {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            if(cols > 0)  mCols = cols;
            if(rows > 0) mRows = rows;
            mLivesBuffer = particleBuffer;
            initialize();
        }

        @Override
        public void initialize()
        {
            super.initialize();
            mTileSize[0] = 1f / mCols;
            mTileSize[1] = 1f / mRows;
            mStartFrame = 0;
            mEndFrame = mCols * mRows -1;
            muAperture = (RFloat) addUniform(PointSpriteShaderVar.U_APERTURE);
            muStartFrame = (RFloat) addUniform(PointSpriteShaderVar.U_START_FRAME);
            muEndFrame = (RFloat) addUniform(PointSpriteShaderVar.U_END_FRAME);
            mvTileSize = (RVec2) addVarying(PointSpriteShaderVar.V_TILE_SIZE);
            mvTileOffset = (RVec2) addVarying(PointSpriteShaderVar.V_TILE_OFFSET);
            maLife = (RVec2) addAttribute(PointSpriteShaderVar.A_LIFETIME);
            mvBirth = (RFloat) addVarying(PointSpriteShaderVar.V_BIRTH);
            mvDeath = (RFloat) addVarying(PointSpriteShaderVar.V_DEATH);
        }

        void setRange(@IntRange(from = 0) int startFrame, @IntRange(from = 0) int endFrame, @FloatRange(from = 1) float aperture) {
            mStartFrame = MathUtil.clamp(startFrame,0,mCols*mRows-1);
            mEndFrame = MathUtil.clamp(endFrame,0,mCols*mRows-1);
            mAperture = MathUtil.clamp(aperture, 1, 1024);
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.POST_TRANSFORM;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int i) {
            // bind buffer for positions and copy data into GL_ARRAY_BUFFER
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mLivesBufferHandle);

            // copy the buffer, and let OpenGL know that we don't plan to
            // change it (STATIC) and that it will be used for drawing (DRAW)
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mLivesBuffer.limit() * Geometry3D.FLOAT_SIZE_BYTES, mLivesBuffer, GLES20.GL_STATIC_DRAW);

            // Enable the attribute at that location
            GLES20.glEnableVertexAttribArray(maLivesHandle);

            // Tell OpenGL the array contains 2 floats (birth,death) for each vertex
            GLES20.glVertexAttribPointer(maLivesHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muApertureHandle, mAperture);
            GLES20.glUniform1f(muStartFrameHandle, mStartFrame);
            GLES20.glUniform1f(muEndFrameHandle, mEndFrame);
        }

        @Override
        public void setLocations(int programHandle) {
            int[] buff = new int[1];
            GLES20.glGenBuffers(1, buff, 0);
            mLivesBufferHandle = buff[0];

            GLES20.glBindAttribLocation(programHandle, mLivesBufferHandle, PointSpriteShaderVar.A_LIFETIME.getVarString());
            maLivesHandle = getAttribLocation(programHandle, PointSpriteShaderVar.A_LIFETIME);
            muApertureHandle = getUniformLocation(programHandle, PointSpriteShaderVar.U_APERTURE);
            muStartFrameHandle = getUniformLocation(programHandle, PointSpriteShaderVar.U_START_FRAME);
            muEndFrameHandle = getUniformLocation(programHandle, PointSpriteShaderVar.U_END_FRAME);
        }

        @Override
        public void main() {
            RFloat uTime = (RFloat) getGlobal(DefaultShaderVar.U_TIME);
            mvBirth.assign(maLife.s());
            mvDeath.assign(maLife.t());

            RFloat lifetime = new RFloat("lifetime", mvDeath.subtract(mvBirth));
            RFloat lived = new RFloat("lived", uTime.subtract(mvBirth));
            RFloat t = new RFloat("t", clamp(lived.divide(lifetime), 0, 1));
            RFloat frames = new RFloat(floor(mix(muStartFrame, muEndFrame, t)));
            RFloat col = new RFloat("row", mod(frames, new RFloat(mCols)));
            RFloat row = new RFloat("col", floor(divide(frames, new RFloat(mRows))));
            mvTileOffset.assign(castVec2(col,row));
            mvTileSize.assign(castVec2(1f/mCols, 1f/mRows));

            GL_POINT_SIZE.assign(muAperture);
        }
    }

    private static final class PointSpriteFragmentShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "POINT_SPRITE_FRAGMENT_SHADER_FRAGMENT";

        private RVec2 mvTileSize;
        private RVec2 mvTileOffset;
        private RSampler2D muPointSpriteTexture;
        private int muPointSpriteTextureHandle;
        private ATexture mPointSpriteTexture;

        RFloat mvBirth;
        RFloat mvDeath;

        public PointSpriteFragmentShaderFragment() {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            initialize();
        }

        public void setPointTexture(@NonNull ATexture PointTexture) {
            mPointSpriteTexture = PointTexture;
            int[] genTextureNames = new int[1];
            GLES20.glGenTextures(1, genTextureNames, 0);
            mPointSpriteTexture.setTextureId(genTextureNames[0]);
        }

        @Override
        public void initialize() {
            super.initialize();
            muPointSpriteTexture = (RSampler2D) addUniform(PointSpriteShaderVar.U_POINT_SPRITE_TEXTURE);
            mvTileSize = (RVec2) addVarying(PointSpriteShaderVar.V_TILE_SIZE);
            mvTileOffset = (RVec2) addVarying(PointSpriteShaderVar.V_TILE_OFFSET);

            mvBirth = (RFloat) addVarying(PointSpriteShaderVar.V_BIRTH);
            mvDeath = (RFloat) addVarying(PointSpriteShaderVar.V_DEATH);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muPointSpriteTextureHandle = getUniformLocation(programHandle, PointSpriteShaderVar.U_POINT_SPRITE_TEXTURE);
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.POST_TRANSFORM;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int nextIndex) {
            if(mPointSpriteTexture != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + nextIndex);
                GLES20.glBindTexture(mPointSpriteTexture.getGLTextureType(), mPointSpriteTexture.getTextureId());
                GLES20.glUniform1i(muPointSpriteTextureHandle, nextIndex);
            }
        }

        @Override
        public void unbindTextures() {
            if(mPointSpriteTexture != null)
                GLES20.glBindTexture(mPointSpriteTexture.getGLTextureType(), 0);
        }

        @Override
        public void main() {
            RFloat uTime = (RFloat) getGlobal(DefaultShaderVar.U_TIME);
            RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            startif(new Condition(uTime, Operator.LESS_THAN, mvBirth));
            {
                discard();
            }
            ifelseif(new Condition(uTime, Operator.GREATER_THAN, mvDeath));
            {
                discard();
            }
            ifelse();
            {
                RVec2 coords = new RVec2("coords", GL_POINT_COORD.add(mvTileOffset));
                color.assign(texture2D(muPointSpriteTexture, coords.multiply(mvTileSize)));
            }
            endif();
        }
    }
}
