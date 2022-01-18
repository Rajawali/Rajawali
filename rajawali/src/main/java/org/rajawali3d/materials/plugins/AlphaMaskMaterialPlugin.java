package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import androidx.annotation.FloatRange;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.math.MathUtil;

public class AlphaMaskMaterialPlugin implements IMaterialPlugin {
    private AlphaMaskShaderFragment mFragmentShader;

    public enum AlphaMaskShaderVar implements AShaderBase.IGlobalShaderVar {
        U_ALPHA_THRESHOLD("uAlphaThreshold", AShaderBase.DataType.FLOAT);

        private String mVarString;
        private AShaderBase.DataType mDataType;

        AlphaMaskShaderVar(String varString, AShaderBase.DataType dataType) {
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

    public AlphaMaskMaterialPlugin(@FloatRange(from = 0, to = 1) float threshold) {
        mFragmentShader = new AlphaMaskShaderFragment(threshold);
    }

    public void setThreshold(@FloatRange(from = 0, to = 1) float threshold) {
        mFragmentShader.setThreshold(threshold);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_LIGHTING;
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
    public void bindTextures(int nextIndex) { }

    @Override
    public void unbindTextures() { }

    private final class AlphaMaskShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "ALPHA_MASK_FRAGMENT_SHADER_FRAGMENT";

        private float mAlphaThreshold = 0.5f;
        private RFloat muAlphaThreshold;
        private int muAlphaThresholdHandle;

        public AlphaMaskShaderFragment(float mAlphaThreshold) {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            this.mAlphaThreshold = mAlphaThreshold;
            initialize();
        }

        @Override
        public void initialize() {
            super.initialize();
            muAlphaThreshold = (RFloat) addUniform(AlphaMaskShaderVar.U_ALPHA_THRESHOLD);
        }

        public void setThreshold(@FloatRange(from = 0, to = 1) float threshold) {
            mAlphaThreshold = MathUtil.clamp(threshold, 0, 1);
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.PRE_LIGHTING;
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muAlphaThresholdHandle, mAlphaThreshold);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muAlphaThresholdHandle = getUniformLocation(programHandle, AlphaMaskShaderVar.U_ALPHA_THRESHOLD);
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int nextIndex) {

        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            startif(new Condition(color.a(), Operator.LESS_THAN_EQUALS, muAlphaThreshold));
            {
                discard();
            }
            endif();
        }
    }
}
