package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import androidx.annotation.NonNull;
import androidx.annotation.FloatRange;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.TextureManager;

public class FlowmapTexturePlugin extends AShader implements IMaterialPlugin {
    FlowmapFragmentShaderFragment mFragmentShader;

    public FlowmapTexturePlugin(@NonNull ATexture texture) {
        TextureManager.getInstance().addTexture(texture);
        mFragmentShader = new FlowmapFragmentShaderFragment();
        mFragmentShader.setFactor(0);
        mFragmentShader.setFlowmap(texture);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_TEXTURE;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return null;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return mFragmentShader;
    }

    @Override
    public void bindTextures(int i) {
        mFragmentShader.bindTextures(i);
    }

    @Override
    public void unbindTextures() {

    }

    public void setFactor(double factor) {
        mFragmentShader.setFactor(factor);
    }

    public enum FlowmapShaderVar implements IGlobalShaderVar {
        U_FLOWMAP("uFlowmap", DataType.SAMPLER2D),
        U_FLOW_FACTOR("uFlowFactor", DataType.FLOAT);

        private final String mVarString;
        private final DataType mDataType;

        FlowmapShaderVar(String varString, DataType dataType) {
            mVarString = varString;
            mDataType = dataType;
        }

        public String getVarString() {
            return mVarString;
        }

        public DataType getDataType() {
            return mDataType;
        }
    }

    static class FlowmapFragmentShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "FLOWMAP_FRAGMENT_SHADER_FRAGMENT";

        private RSampler2D muFlowmap;
        private int muFlowmapHandle;
        private ATexture mFlowmap;

        private float mFactor = 0;
        private RFloat muFactor;
        private int muFactorHandle;

        public FlowmapFragmentShaderFragment() {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            initialize();
        }

        @Override
        public void initialize()
        {
            super.initialize();
            muFlowmap = (RSampler2D) addUniform(FlowmapShaderVar.U_FLOWMAP);
            muFactor = (RFloat) addUniform(FlowmapShaderVar.U_FLOW_FACTOR);
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
            super.setLocations(programHandle);
            muFlowmapHandle = getUniformLocation(programHandle, FlowmapShaderVar.U_FLOWMAP);
            muFactorHandle = getUniformLocation(programHandle, FlowmapShaderVar.U_FLOW_FACTOR);
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muFactorHandle, mFactor);
        }

        @Override
        public void bindTextures(int nextIndex) {
            if (mFlowmap != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + nextIndex);
                GLES20.glBindTexture(mFlowmap.getGLTextureType(), mFlowmap.getTextureId());
                GLES20.glUniform1i(muFlowmapHandle, nextIndex);
            }
        }

        @Override
        public void unbindTextures() {
            if (mFlowmap != null)
                GLES20.glBindTexture(mFlowmap.getGLTextureType(), 0);
        }

        @Override
        public void main() {
            RVec2 vTextureCoord = (RVec2) getGlobal(DefaultShaderVar.V_TEXTURE_COORD);
            RVec2 gTextureCoord = (RVec2) getGlobal(DefaultShaderVar.G_TEXTURE_COORD);

            RVec4 flowTexel = new RVec4("flowTexel");
            RVec2 flowCoord = new RVec2("flowCoord");
            RVec3 flowResult = new RVec3("flowResult");
            flowCoord.assign(vTextureCoord);
            if(mFlowmap.transformEnabled()) {
                RMat3 transform = (RMat3) getGlobal(DefaultShaderVar.U_TRANSFORM, 0);
                flowResult.assign(transform.multiply(castVec3(vTextureCoord, 1)));
                flowCoord.assign(flowResult.xy());
            }
            flowTexel.assign(texture2D(muFlowmap, flowCoord));
            flowTexel.xy().assignMultiply(castVec2(muFactor,muFactor));
            gTextureCoord.assign(vTextureCoord.add(flowTexel.xy()));
        }

        public void setFlowmap(@NonNull ATexture flowmap) {
            mFlowmap = flowmap;
            int[] genTextureNames = new int[1];
            GLES20.glGenTextures(1, genTextureNames, 0);
            mFlowmap.setTextureId(genTextureNames[0]);
        }

        void setFactor(double factor) {
            mFactor = (float) factor;
        }
    }
}
