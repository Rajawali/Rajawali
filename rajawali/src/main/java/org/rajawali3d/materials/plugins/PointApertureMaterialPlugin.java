package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class PointApertureMaterialPlugin implements IMaterialPlugin {
    private ApertureVertexShaderFragment mVertexShader;
    private ApertureFragmentShaderFragment mFragmentShader;

    public PointApertureMaterialPlugin(float aperture)
    {
        mVertexShader = new ApertureVertexShaderFragment(aperture);
        mFragmentShader = new ApertureFragmentShaderFragment();
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_LIGHTING;
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
    public void bindTextures(int nextIndex) {}
    @Override
    public void unbindTextures() {}

    private class ApertureVertexShaderFragment extends AShader implements IShaderFragment
    {
        public final static String SHADER_ID = "APERTURE_VERTEX_FRAGMENT";
        float mAperture;

        public ApertureVertexShaderFragment(float aperture)
        {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            mAperture = aperture;
            initialize();
        }

        @Override
        public void main() {
            RVec4 a_position = (RVec4) getGlobal(DefaultShaderVar.A_POSITION);
            RVec4 g_position = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);

            g_position.assign(a_position);
            mShaderSB.append("gl_PointSize = " + mAperture + ";\n");
        }

        @Override
        public void setLocations(int programHandle) {
        }

        @Override
        public void applyParams() {
            super.applyParams();
        }


        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.IGNORE;
        }

        @Override
        public void bindTextures(int nextIndex) {}

        @Override
        public void unbindTextures() {}
    }

    private class ApertureFragmentShaderFragment extends AShader implements IShaderFragment
    {
        public final static String SHADER_ID = "APERTURE_FRAGMENT_FRAGMENT";

        public ApertureFragmentShaderFragment()
        {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            initialize();
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void main() {
            RVec4 v_color = (RVec4) getGlobal(AShaderBase.DefaultShaderVar.V_COLOR);
            RVec4 g_color = (RVec4) getGlobal(AShaderBase.DefaultShaderVar.G_COLOR);

            RFloat d = new RFloat("d");
            d.assign(clamp(length(castVec2("gl_PointCoord*2.-1.")), 0,1));
            d.assign(pow(d,v_color.r()));
            RVec3 color = new RVec3("color");
            color.assign(castVec3(d.multiply(-1).add(1)));
            g_color.assign(v_color.multiply(castVec4(color, 1)));
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.IGNORE;
        }

        @Override
        public void bindTextures(int nextIndex) {}

        @Override
        public void unbindTextures() {}
    }
}

