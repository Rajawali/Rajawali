package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class NormalColorMaterialPlugin  extends AShader implements IMaterialPlugin {
    private NormalColorVertexShaderFragment mVertexShader;

    public NormalColorMaterialPlugin() {
        mVertexShader = new NormalColorVertexShaderFragment();
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

    class NormalColorVertexShaderFragment extends AShader implements IShaderFragment {
        static final String SHADER_ID = "NORMAL_COLOR_VERTEX_SHADER_FRAGMENT";

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

        @Override
        public void main() {
            RVec3 a_normal = (RVec3) getGlobal(DefaultShaderVar.A_NORMAL);
            RVec4 g_color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            RVec4 n_color = new RVec4("n_color");
            n_color.assign(1);
            n_color.r().assign(max(a_normal.x(),0));
            n_color.g().assign(max(a_normal.y(),0));
            n_color.b().assign(max(a_normal.z(),0));
            n_color.r().assignAdd(enclose(min(a_normal.y(),0).add(min(a_normal.z(),0))).divide(-2));
            n_color.g().assignAdd(enclose(min(a_normal.z(),0).add(min(a_normal.x(),0))).divide(-2));
            n_color.b().assignAdd(enclose(min(a_normal.x(),0).add(min(a_normal.y(),0))).divide(-2));
            g_color.assign(n_color);
        }
    }
}
