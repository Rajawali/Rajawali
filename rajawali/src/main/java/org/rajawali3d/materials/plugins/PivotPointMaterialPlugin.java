package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.math.vector.Vector3;

public class PivotPointMaterialPlugin implements IMaterialPlugin {
    OffsetVertexShaderFragment mVertexShader;

    public PivotPointMaterialPlugin(double x, double y, double z) {
        mVertexShader = new OffsetVertexShaderFragment(new Vector3(x, y, z));
    }

    public PivotPointMaterialPlugin(Vector3 offset) {
        mVertexShader = new OffsetVertexShaderFragment(offset);
    }

    public void setOffset(Vector3 offset) {
        mVertexShader.setOffset(offset);
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

    class OffsetVertexShaderFragment extends AShader implements IShaderFragment {
        static final String SHADER_ID = "OFFSET_VERTEX_SHADER_FRAGMENT";
        Vector3 mOffset;
        RVec4 muOffset;
        int muOffsetHandle;

        public OffsetVertexShaderFragment(Vector3 offset) {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            mOffset = offset;
            initialize();
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void initialize() {
            super.initialize();
            muOffset = (RVec4) addUniform(VerTexOffsetShaderVar.U_VERTEX_OFFSET);
        }

        public void setOffset(Vector3 offset) {
            mOffset = offset;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return null;
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform4f(muOffsetHandle, (float)mOffset.x, (float)mOffset.y, (float)mOffset.z, 0);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muOffsetHandle = getUniformLocation(programHandle, VerTexOffsetShaderVar.U_VERTEX_OFFSET);
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
            position.assignSubtract(muOffset);
        }
    }

    public enum VerTexOffsetShaderVar implements AShaderBase.IGlobalShaderVar {
        U_VERTEX_OFFSET("uVertexOffset", AShaderBase.DataType.VEC4);

        private String mVarString;
        private AShaderBase.DataType mDataType;

        VerTexOffsetShaderVar(String varString, AShaderBase.DataType dataType) {
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
}

