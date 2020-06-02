package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.FloatRange;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class IndividualMaterialPlugin implements IMaterialPlugin {
    public static final String BIRTH = "birth";
    public static final String DEATH = "death";

    private BlackbodyVertexShaderFragment mVertexShaderFragment;
    private BlackbodyFragmentShaderFragment mFragmentShaderFragment;

    public IndividualMaterialPlugin(int numPoints, float start, float end, float lifetime, @FloatRange(from = 0.0d, to = 1.0d) float randomness) {
        mVertexShaderFragment = new BlackbodyVertexShaderFragment(numPoints, start, end, lifetime, randomness);
        mFragmentShaderFragment = new BlackbodyFragmentShaderFragment();
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_LIGHTING;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return mVertexShaderFragment;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return mFragmentShaderFragment;
    }

    @Override
    public void bindTextures(int i) {
        mVertexShaderFragment.bindTextures(i);
        mFragmentShaderFragment.bindTextures(i);
    }

    @Override
    public void unbindTextures() {

    }

    private class BlackbodyVertexShaderFragment extends AShader implements IShaderFragment {
        public static final String SHADER_ID = "BLACKBODY_VERTEX_FRAGMENT";
        public static final String A_LIFETIME = "aLifetime";

        int maLivesHandle;
        int mLivesBufferHandle;
        RVec2 maLife;
        RFloat mvBirth;
        RFloat mvDeath;

        int mNumPoints;
        float mStart;
        float mEnd;
        float mLifetime;
        float mRandomness;
        FloatBuffer mLivesBuffer;
        Random r = new Random();

        public BlackbodyVertexShaderFragment(int numPoints, float start, float end, float lifetime, @FloatRange(from = 0.0d, to = 1.0d) float randomness) {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            mNumPoints = numPoints;
            mStart = start;
            mEnd = end;
            mLifetime = lifetime;
            mRandomness = Math.min(Math.max(randomness,0),1);

            initialize();
        }

        float variation(@FloatRange(from = 0.0d, to = 1.0d) float randomness) {
            return 1 + (2 * r.nextFloat() - 1) * randomness;
        }

        @Override
        public void initialize() {
            super.initialize();

            mLivesBuffer = ByteBuffer
                    .allocateDirect(mNumPoints * 2 * Geometry3D.FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            for (int i = 0; i<mNumPoints; i++) {
                mLivesBuffer.put(mStart + mEnd * variation(mRandomness));
                mLivesBuffer.put(mStart + mEnd + mLifetime * variation(mRandomness));
            }
            mLivesBuffer.position(0);

            maLife = (RVec2) addAttribute(A_LIFETIME, DataType.VEC2);
            mvBirth = (RFloat) addVarying(BIRTH, DataType.FLOAT);
            mvDeath = (RFloat) addVarying(DEATH, DataType.FLOAT);
        }

        @Override
        public void main() {
            mvBirth.assign(maLife.s());
            mvDeath.assign(maLife.t());
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return null;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void setLocations(int programHandle) {
            Log.i(getClass().getSimpleName(), "setLocations");
            super.setLocations(programHandle);

            int buff[] = new int[1];
            GLES20.glGenBuffers(1, buff, 0);
            mLivesBufferHandle = buff[0];

            GLES20.glBindAttribLocation(programHandle, mLivesBufferHandle, A_LIFETIME);

            maLivesHandle = getAttribLocation(programHandle, A_LIFETIME);
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

            // Tell OpenGL the array contains 1 floats (birth) for each vertex
            GLES20.glVertexAttribPointer(maLivesHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
        }

        @Override
        public void unbindTextures() {

        }
    }

    private class BlackbodyFragmentShaderFragment extends AShader implements IShaderFragment {
        public static final String SHADER_ID = "BLACKBODY_FRAGMENT_FRAGMENT";
        RFloat mvBirth;
        RFloat mvDeath;

        public BlackbodyFragmentShaderFragment() {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            initialize();
        }

        @Override
        public void initialize() {
            super.initialize();
            mvBirth = (RFloat) addVarying(BIRTH, DataType.FLOAT);
            mvDeath = (RFloat) addVarying(DEATH, DataType.FLOAT);
        }

        @Override
        public void main() {
            RFloat uTime = (RFloat) getGlobal(DefaultShaderVar.U_TIME);
            RVec4 gColor = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);

            RFloat age = new RFloat("age");
            age.assign(uTime.divide(mvDeath));
            RFloat heat = new RFloat("heat");
            heat.assign(clamp(cos(age.multiply((float)Math.PI/2)), 0, 1));

            gColor.r().assignMultiply(heat.multiply(0.5f).add(0.5f));
            gColor.g().assignMultiply(heat.multiply(0.75f).add(0.25f));
            gColor.b().assignMultiply(heat);

            startif(new Condition(uTime, Operator.LESS_THAN, mvBirth));
            {
                discard();
            }
            endif();
            startif(new Condition(uTime, Operator.GREATER_THAN, mvDeath));
            {
                discard();
            }
            endif();

        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return null;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int i) {

        }

        @Override
        public void unbindTextures() {

        }
    }

}
