package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;
import androidx.annotation.FloatRange;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class AlphaMaterialPlugin implements IMaterialPlugin {
    private AlphaFragmentShaderFragment mFragmentShader;

    public AlphaMaterialPlugin() {
        mFragmentShader = new AlphaFragmentShaderFragment();
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.POST_TRANSFORM;
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
    public void bindTextures(int nextIndex) {

    }

    @Override
    public void unbindTextures() {

    }

    public void setAlpha(float farPlane) {
        mFragmentShader.setAlpha(farPlane);
    }
}

final class AlphaFragmentShaderFragment extends AShader implements IShaderFragment {
    public final static String SHADER_ID = "ALPHA_FRAGMENT_SHADER_FRAGMENT";
    private final static String U_ALPHA = "uAlpha";

    private float mAlpha = 0.5f;
    private RFloat muAlpha;
    private int muAlphaHandle;

    public AlphaFragmentShaderFragment() {
        super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
        initialize();
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.POST_TRANSFORM;
    }

    @Override
    public void initialize() {
        super.initialize();
        muAlpha = (RFloat) addUniform(U_ALPHA, DataType.FLOAT);
    }

    @Override
    public void setLocations(int programHandle) {
        super.setLocations(programHandle);
        muAlphaHandle = getUniformLocation(programHandle, U_ALPHA);
    }

    @Override
    public void applyParams() {
        super.applyParams();
        GLES20.glUniform1f(muAlphaHandle, mAlpha);
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
        color.a().assignMultiply(muAlpha);
    }

    public void setAlpha(@FloatRange(from = 0.0, to = 1.0) final float alpha) {
        mAlpha = alpha;
        if(mAlpha>1) mAlpha=1;
        if(mAlpha<0) mAlpha=0;
    }
}
