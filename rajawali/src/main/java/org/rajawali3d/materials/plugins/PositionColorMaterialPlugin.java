package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class PositionColorMaterialPlugin extends AShader implements IMaterialPlugin {
    private PositionColorVertexShaderFragment mVertexShader;

    public PositionColorMaterialPlugin() {
        mVertexShader = new PositionColorVertexShaderFragment();
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

    class PositionColorVertexShaderFragment extends AShader implements IShaderFragment {
        static final String SHADER_ID = "POSITION_COLOR_VERTEX_SHADER_FRAGMENT";

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
            RVec4 a_position = (RVec4) getGlobal(DefaultShaderVar.A_POSITION);
            RVec4 g_color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            RVec3 unit = new RVec3("unit");
            unit.assign(normalize(a_position.xyz()));
            RVec4 n_color = new RVec4("n_color");
            n_color.assign(1);
            n_color.r().assign(max(unit.x(),0));
            n_color.g().assign(max(unit.y(),0));
            n_color.b().assign(max(unit.z(),0));
            g_color.assign(n_color);
        }
    }
}
