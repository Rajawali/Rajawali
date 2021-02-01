package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class ExplodingMaterialPlugin implements IMaterialPlugin {
    ExplodingVertexShaderFragment mVertexShader;

    public enum ExplodingShaderVar implements AShaderBase.IGlobalShaderVar {
        U_EXPLODE_FACTOR("uExplodeFactor", AShaderBase.DataType.FLOAT);

        private String mVarString;
        private AShaderBase.DataType mDataType;

        ExplodingShaderVar(String varString, AShaderBase.DataType dataType) {
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

    public ExplodingMaterialPlugin() {
        mVertexShader = new ExplodingVertexShaderFragment();
    }

    public void setFactor(double factor) {
        mVertexShader.setFactor((float)(factor*factor));
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

    }

    @Override
    public void unbindTextures() {

    }

    class ExplodingVertexShaderFragment extends AShader implements IShaderFragment {
        static final String SHADER_ID = "EXPLODING_VERTEX_SHADER_FRAGMENT";

        float mFactor = 0;

        RFloat muFactor;
        int muFactorHandle;

        public ExplodingVertexShaderFragment() {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            mFactor = 0;
            initialize();
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void initialize() {
            super.initialize();
            muFactor = (RFloat) addUniform(ExplodingShaderVar.U_EXPLODE_FACTOR);
        }

        public void setFactor(float factor) {
            mFactor = factor;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return null;
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muFactorHandle, mFactor);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muFactorHandle = getUniformLocation(programHandle, ExplodingShaderVar.U_EXPLODE_FACTOR);

            int buff[] = new int[2];
            GLES20.glGenBuffers(2, buff, 0);
        }

        @Override
        public void bindTextures(int i) {
        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            RVec4 position = (RVec4)getGlobal(DefaultShaderVar.G_POSITION);
            RVec3 normal = (RVec3)getGlobal(DefaultShaderVar.G_NORMAL);
            position.assignAdd(castVec4(normal, 0).multiply(muFactor));
        }
    }
}

