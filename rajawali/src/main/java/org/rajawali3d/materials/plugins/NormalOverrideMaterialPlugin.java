package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.animation.IKeyframes;
import org.rajawali3d.animation.IInterpolatable;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.math.vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class NormalOverrideMaterialPlugin implements IMaterialPlugin, IInterpolatable {
    NormalOverrideVertexShaderFragment mVertexShader;

    public enum OverrideShaderVar implements AShaderBase.IGlobalShaderVar {
        U_NORMAL_OVERRIDE_ACTIVE("uOverrideActive", AShaderBase.DataType.BOOL),
        A_NORMAL_OVERRIDE("aNormalOverride", AShaderBase.DataType.VEC3);

        private final String mVarString;
        private final AShaderBase.DataType mDataType;

        OverrideShaderVar(String varString, AShaderBase.DataType dataType) {
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

    public NormalOverrideMaterialPlugin(IKeyframes<Double, Vector3[]> keyframes) {
        mVertexShader = new NormalOverrideVertexShaderFragment(keyframes);
    }

    @Override
    public void enableInterpolation(boolean value) {
        mVertexShader.enableInterpolation(value);
    }

    @Override
    public void interpolate(double factor) {
        mVertexShader.interpolate(factor);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_TRANSFORM;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return mVertexShader;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return null;
    }

    @Override
    public void bindTextures(int i) {
        mVertexShader.bindTextures(i);
    }

    @Override
    public void unbindTextures() {

    }

    static class NormalOverrideVertexShaderFragment extends AShader implements IShaderFragment, IInterpolatable {
        static final String SHADER_ID = "NORMAL_INTERPOLATION_VERTEX_SHADER_FRAGMENT";

        double mInterpolation = 0;
        IKeyframes<Double, Vector3[]> mKeyframes;

        RBool muActive;
        boolean mActive = false;
        int muActiveHandle;

        RVec3 maNormalOverride;
        FloatBuffer maNormalOverrideBuffer;
        int maNormalOverrideBufferHandle;
        int maNormalOverrideHandle;

        public NormalOverrideVertexShaderFragment(IKeyframes<Double, Vector3[]> keyframes) {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            initialize(keyframes);
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        public void initialize(IKeyframes<Double, Vector3[]> keyframes) {
            super.initialize();
            mActive = false;
            muActive = (RBool) addUniform(OverrideShaderVar.U_NORMAL_OVERRIDE_ACTIVE);

            mInterpolation = 0;
            mKeyframes = keyframes;

            Vector3[] keyframe = (Vector3[]) mKeyframes.calculatePoint((double) 0);
            maNormalOverrideBuffer = ByteBuffer.allocateDirect(keyframe.length * 3 * Geometry3D.FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            maNormalOverride = (RVec3) addAttribute(OverrideShaderVar.A_NORMAL_OVERRIDE);
        }

        @Override
        public void enableInterpolation(boolean active) {
            mActive = active;
        }

        @Override
        public void interpolate(double factor) {
            mInterpolation = factor;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return null;
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1i(muActiveHandle, (mActive ? 1 : 0));
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muActiveHandle = getUniformLocation(programHandle, OverrideShaderVar.U_NORMAL_OVERRIDE_ACTIVE);

            int[] buff = new int[1];
            GLES20.glGenBuffers(1, buff, 0);
            maNormalOverrideBufferHandle = buff[0];
            status = GLES20.glGetError();

            GLES20.glBindAttribLocation(programHandle, maNormalOverrideBufferHandle, OverrideShaderVar.A_NORMAL_OVERRIDE.getVarString());
            status = GLES20.glGetError();

            maNormalOverrideHandle = GLES20.glGetAttribLocation(programHandle, OverrideShaderVar.A_NORMAL_OVERRIDE.getVarString());
            status = GLES20.glGetError();
        }

        int status;
        @Override
        public void bindTextures(int i) {
            maNormalOverrideBuffer.clear();
            for(Vector3 normal : (Vector3[]) mKeyframes.calculatePoint(mInterpolation)) {
                maNormalOverrideBuffer.put((float) normal.x);
                maNormalOverrideBuffer.put((float) normal.y);
                maNormalOverrideBuffer.put((float) normal.z);
            }
            maNormalOverrideBuffer.compact().position(0);

            if(maNormalOverrideHandle > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, maNormalOverrideBufferHandle);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, maNormalOverrideBuffer.capacity() * Geometry3D.FLOAT_SIZE_BYTES, maNormalOverrideBuffer, GLES20.GL_STATIC_DRAW);
                GLES20.glVertexAttribPointer(maNormalOverrideHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(maNormalOverrideHandle);
                status = GLES20.glGetError();
            }
        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            RVec3 normal = (RVec3)getGlobal(DefaultShaderVar.G_NORMAL);
            startif(new Condition(muActive, Operator.EQUALS, true)); {
                normal.assign(maNormalOverride);
            } endif();
        }
    }
}
