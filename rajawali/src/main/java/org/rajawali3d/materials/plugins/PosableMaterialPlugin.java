package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;
import androidx.annotation.FloatRange;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;

import static org.rajawali3d.math.MathUtil.clamp;

import java.nio.FloatBuffer;

public class PosableMaterialPlugin implements IMaterialPlugin {
    PosingVertexShaderFragment mVertexShader;

    public enum PosingShaderVar implements AShaderBase.IGlobalShaderVar {
        U_POSE_INTERPOLATION("uPoseInterpolation", AShaderBase.DataType.FLOAT),
        A_POSE_POSITION("aPosePosition", AShaderBase.DataType.VEC3),
        A_POSE_NORMAL("aPoseNormal", AShaderBase.DataType.VEC3);

        private String mVarString;
        private AShaderBase.DataType mDataType;

        PosingShaderVar(String varString, AShaderBase.DataType dataType) {
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

    public PosableMaterialPlugin(Geometry3D geometry) {
        mVertexShader = new PosingVertexShaderFragment(geometry);
    }

    public void setInterpolation(@FloatRange(from = 0.0d, to = 1.0d) double interpolation) {
        mVertexShader.setInterpolation((float)clamp(interpolation, 0, 1));
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

    class PosingVertexShaderFragment extends AShader implements IShaderFragment {
        static final String SHADER_ID = "POSING_VERTEX_SHADER_FRAGMENT";

        FloatBuffer mVertices;
        FloatBuffer mNormals;

        RVec3 maPosePosition;
        FloatBuffer maPosePositionBuffer;
        int maPosePositionBufferHandle;
        int maPosePositionHandle;

        RVec3 maPoseNormal;
        FloatBuffer maPoseNormalBuffer;
        int maPoseNormalBufferHandle;
        int maPoseNormalHandle;

        RFloat muInterpolation;
        float mInterpolation = 0;
        int muInterpolationHandle;

        public PosingVertexShaderFragment(Geometry3D geometry) {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            initialize(geometry);
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        public void initialize(Geometry3D geometry) {
            super.initialize();
            mInterpolation = 0;
            muInterpolation = (RFloat) addUniform(PosingShaderVar.U_POSE_INTERPOLATION);

            maPosePositionBuffer = geometry.getVertices();
            maPosePosition = (RVec3) addAttribute(PosingShaderVar.A_POSE_POSITION);
            maPosePositionBuffer.compact().position(0);

            maPoseNormalBuffer = geometry.getNormals();
            maPoseNormal = (RVec3) addAttribute(PosingShaderVar.A_POSE_NORMAL);
            maPoseNormalBuffer.compact().position(0);
        }

        public void setInterpolation(float interpolation) {
            if(interpolation<0) interpolation=0;
            if(interpolation>1) interpolation=1;
            mInterpolation = interpolation;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return null;
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muInterpolationHandle, mInterpolation);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muInterpolationHandle = getUniformLocation(programHandle, PosingShaderVar.U_POSE_INTERPOLATION);

            int buff[] = new int[2];
            GLES20.glGenBuffers(2, buff, 0);
            maPosePositionBufferHandle = buff[0];
            maPoseNormalBufferHandle = buff[1];
            status = GLES20.glGetError();

            GLES20.glBindAttribLocation(programHandle, maPosePositionBufferHandle, PosingShaderVar.A_POSE_POSITION.getVarString());
            GLES20.glBindAttribLocation(programHandle, maPoseNormalBufferHandle, PosingShaderVar.A_POSE_NORMAL.getVarString());
            status = GLES20.glGetError();

            maPosePositionHandle = GLES20.glGetAttribLocation(programHandle, PosingShaderVar.A_POSE_POSITION.getVarString());
            maPoseNormalHandle = GLES20.glGetAttribLocation(programHandle, PosingShaderVar.A_POSE_NORMAL.getVarString());
            status = GLES20.glGetError();
        }

        int status;
        @Override
        public void bindTextures(int i) {
            if(maPosePositionHandle > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, maPosePositionBufferHandle);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, maPosePositionBuffer.capacity() * Geometry3D.FLOAT_SIZE_BYTES, maPosePositionBuffer, GLES20.GL_STATIC_DRAW);
                GLES20.glVertexAttribPointer(maPosePositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(maPosePositionHandle);
                status = GLES20.glGetError();
            }

            if(maPoseNormalHandle > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, maPoseNormalBufferHandle);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, maPoseNormalBuffer.capacity() * Geometry3D.FLOAT_SIZE_BYTES, maPoseNormalBuffer, GLES20.GL_STATIC_DRAW);
                GLES20.glVertexAttribPointer(maPoseNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(maPoseNormalHandle);
                status = GLES20.glGetError();
            }
        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            RVec4 position = (RVec4)getGlobal(DefaultShaderVar.G_POSITION);
            RVec3 normal = (RVec3)getGlobal(DefaultShaderVar.G_NORMAL);
            RVec4 aPosition = (RVec4)getGlobal(DefaultShaderVar.A_POSITION);
            RVec3 aNormal = (RVec3)getGlobal(DefaultShaderVar.A_NORMAL);

            position.assign(mix(aPosition, castVec4(maPosePosition, 1), muInterpolation));
            normal.assign(mix(aNormal, maPoseNormal, muInterpolation));
        }
    }
}

