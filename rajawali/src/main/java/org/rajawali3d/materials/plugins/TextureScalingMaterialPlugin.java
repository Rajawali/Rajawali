package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class TextureScalingMaterialPlugin  extends AShader implements IMaterialPlugin {
    private TextureScalingVertexShaderFragment mVertexShader;

    public TextureScalingMaterialPlugin(float x, float y, float z) {
        mVertexShader = new TextureScalingVertexShaderFragment(x,y,z);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_TRANSFORM;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return this.mVertexShader;
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

    class TextureScalingVertexShaderFragment extends AShader implements IShaderFragment {
        static final String SHADER_ID = "TEXTURE_SCALING_VERTEX_SHADER_FRAGMENT";
        float scaleX=1, scaleY=1, scaleZ=1;

        TextureScalingVertexShaderFragment() {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            scaleX = scaleY = scaleZ = 1;
        }
        
        TextureScalingVertexShaderFragment(float x, float y, float z) {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            scaleX = x;
            scaleY = y;
            scaleZ = z;
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
        public void bindTextures(int i) {

        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            RVec3 a_normal = (RVec3) getGlobal(DefaultShaderVar.A_NORMAL);
            RVec2 a_texture_coord = (RVec2) getGlobal(DefaultShaderVar.A_TEXTURE_COORD);
            RVec2 g_texture_coord = (RVec2) getGlobal(DefaultShaderVar.G_TEXTURE_COORD);

            RVec3 x_axis = new RVec3("x_axis"); x_axis.assign(1,0,0);
            RVec3 y_axis = new RVec3("y_axis"); y_axis.assign(0,1,0);
            RVec3 z_axis = new RVec3("z_axis"); z_axis.assign(0,0,1);

            RFloat dotX = new RFloat("dotX");
            dotX.assign(abs(dot(a_normal,x_axis)));
            RFloat dotY = new RFloat("dotY");
            dotY.assign(abs(dot(a_normal,y_axis)));
            RFloat dotZ = new RFloat("dotZ");
            dotZ.assign(abs(dot(a_normal,z_axis)));

            g_texture_coord.assign(0,0);

            g_texture_coord.s().assignAdd(a_texture_coord.s().multiply(scaleY).multiply(dotX));
            g_texture_coord.t().assignAdd(a_texture_coord.t().multiply(scaleZ).multiply(dotX));

            g_texture_coord.s().assignAdd(a_texture_coord.s().multiply(scaleZ).multiply(dotY));
            g_texture_coord.t().assignAdd(a_texture_coord.t().multiply(scaleX).multiply(dotY));

            g_texture_coord.s().assignAdd(a_texture_coord.s().multiply(scaleX).multiply(dotZ));
            g_texture_coord.t().assignAdd(a_texture_coord.t().multiply(scaleY).multiply(dotZ));
        }
    }
}

