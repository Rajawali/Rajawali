package org.rajawali3d.materials.plugins;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;

/*
 * The fresnel equations describe the relationship between 
 * the view angle of a surface and the amount of reflectivity observeed. 
 *
 * This plugin implements a simplified approximation of the frsnel equations,
 * variables include fresnel color, bias, scale and power.
 * 
*/
public class FresnelMaterialPlugin extends AShader implements IMaterialPlugin {
    private FresnelFragmentShaderFragment mFragmentShader;

    public FresnelMaterialPlugin(int color) {
        mFragmentShader = new FresnelFragmentShaderFragment(color);
    }

    public FresnelMaterialPlugin(int color, float bias, float scale, float power) {
        mFragmentShader = new FresnelFragmentShaderFragment(color, bias, scale, power);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_TRANSFORM;
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

    }

    @Override
    public void unbindTextures() {

    }

    public void setColor(final int color) {
        mFragmentShader.setColor(color);
    }

    public void setBias(final float bias) {
        mFragmentShader.setBias(bias);
    }

    public void setScale(final float scale) {
        mFragmentShader.setScale(scale);
    }

    public void setAlpha(final float power) {
        mFragmentShader.setPower(power);
    }

    class FresnelFragmentShaderFragment extends AShader implements IShaderFragment {
        final static String SHADER_ID = "FRESNEL_FRAGMENT_SHADER_FRAGMENT";

        private final static String U_FRESNEL_COLOR = "uFresnelColor";
        private final static String U_BIAS = "uBias";
        private final static String U_SCALE = "uScale";
        private final static String U_EXPONENT = "uExponent";

        private float mBias;
        private float mScale;
        private float mExponent;
        private  float[] mFresnelColor;

        private RVec3 muFresnelColor;
        private RFloat muBias;
        private RFloat muScale;
        private RFloat muExponent;

        private int muFresnelColorHandle;
        private int muBiasHandle;
        private int muScaleHandle;
        private int muExponentHandle;

        FresnelFragmentShaderFragment(int color) {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            mFresnelColor = new float[] { Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f };
            mBias = 1.0f;
            mScale = 1.0f;
            mExponent = 1.0f;
            initialize();
        }

        FresnelFragmentShaderFragment(final int color, final float bias, final float scale, final float power) {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            mFresnelColor = new float[] { Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f };
            mBias = bias;
            mScale = scale;
            mExponent = power;
            initialize();
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return null;
        }

        @Override
        public String getShaderId() {
            return null;
        }

        @Override
        public void bindTextures(int i) {

        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            super.main();
            RFloat u_color_influence = (RFloat) getGlobal(DefaultShaderVar.U_COLOR_INFLUENCE);
            RMat4 inverseV = (RMat4) getGlobal(DefaultShaderVar.U_INVERSE_VIEW_MATRIX);
            RVec3 normal = (RVec3) getGlobal(DefaultShaderVar.V_NORMAL);
            RVec3 eyeDir = (RVec3) getGlobal(DefaultShaderVar.V_EYE_DIR);
            RVec4 g_color = (RVec4) getGlobal(AShaderBase.DefaultShaderVar.G_COLOR);

            RVec3 worldspace_normal = new RVec3("worldspace_normal");
            worldspace_normal.assign("vec3(uInverseViewMatrix * vec4(vNormal,0.0))");

            // get the dot product between the normal and the view direction"
            RFloat fresnel = new RFloat("fresnel");
            fresnel.assign("dot(normalize(vEyeDir), normalize(worldspace_normal))");

            // process the fresnel value with bias, scale, and power inputs
            RFloat value = new RFloat("value");
            value.assign(clamp(pow(fresnel.add(muBias), muExponent).multiply(muScale), 0,1));

            //apply the fresnel value to the emission
            g_color.rgb().assignAdd(muFresnelColor.multiply(value));
        }

        @Override
        public void initialize() {
            super.initialize();
            muFresnelColor = (RVec3) addUniform(U_FRESNEL_COLOR, DataType.VEC3);
            muBias = (RFloat) addUniform(U_BIAS, DataType.FLOAT);
            muScale = (RFloat) addUniform(U_SCALE, DataType.FLOAT);
            muExponent = (RFloat) addUniform(U_EXPONENT, DataType.FLOAT);

        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muFresnelColorHandle = getUniformLocation(programHandle, U_FRESNEL_COLOR);
            muBiasHandle = getUniformLocation(programHandle, U_BIAS);
            muScaleHandle = getUniformLocation(programHandle, U_SCALE);
            muExponentHandle = getUniformLocation(programHandle, U_EXPONENT);
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform3fv(muFresnelColorHandle, 1, mFresnelColor, 0);
            GLES20.glUniform1f(muBiasHandle, mBias);
            GLES20.glUniform1f(muScaleHandle, mScale);
            GLES20.glUniform1f(muExponentHandle, mExponent);
        }

        public void setColor(final int color) {
            mFresnelColor = new float[] { Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f };
        }

        public void setBias(final float bias) {
            mBias = bias;
        }

        public void setScale(final float scale) {
            mScale = scale;
        }

        public void setPower(final float exponent) {
            mExponent = exponent;
        }
    }
}
