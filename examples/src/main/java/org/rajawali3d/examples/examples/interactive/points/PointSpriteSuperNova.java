package org.rajawali3d.examples.examples.interactive.points;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;

public class PointSpriteSuperNova extends Object3D {

    private PointSpriteSuperNovaMaterialPlugin mMaterialPlugin;

    public PointSpriteSuperNova() {
        super();
        init(100000,5);
    }

    public PointSpriteSuperNova(int number, float diameter) {
        super();
        init(number, diameter);
    }

    @Override
    protected void preRender() {
        super.preRender();
    }

    public void init(int numSprites, float spriteDiameter) {
        try {
            setTransparent(true);
            setBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            setDrawingMode(GLES20.GL_POINTS);
            Material mSpritesMat = new Material();
            mSpritesMat.enableTime(true);

            mMaterialPlugin = new PointSpriteSuperNovaMaterialPlugin(spriteDiameter);
            mSpritesMat.addPlugin(mMaterialPlugin);

            setMaterial(mSpritesMat);

            float[] vertices = new float[numSprites * 3];
            float[] normals = new float[numSprites * 3];
            int[] indices = new int[numSprites * 1];
            float[] colors = new float[numSprites * 4];

            for (int i = 0; i < numSprites; ++i) {
                double d = Math.random();
                Vector3 v = new Vector3(Math.random()*2-1, Math.random()*2-1, Math.random()*2-1);
                v.normalize();
                v.multiply(d*d);
                int randColor = 0xff000000 + (int) (0xffffff * Math.random());

                vertices[i * 3 + 0] = (float)v.x;
                vertices[i * 3 + 1] = (float)v.y;
                vertices[i * 3 + 2] = (float)v.z;

                normals[i * 3 + 0] = 0;
                normals[i * 3 + 1] = 0;
                normals[i * 3 + 2] = 1;

                indices[i] = i;

                colors[i * 4 + 0] = Color.red(randColor) / 255f;
                colors[i * 4 + 1] = Color.green(randColor) / 255f;
                colors[i * 4 + 2] = Color.blue(randColor) / 255f;
                colors[i * 4 + 3] = 1.0f;
            }

            setData(vertices, normals, null, colors, indices, true);

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }

    public void reload() {
        super.reload();
    }

    public PointSpriteSuperNovaMaterialPlugin getMaterialPlugin()
    {
        return mMaterialPlugin;
    }

    public class PointSpriteSuperNovaMaterialPlugin implements IMaterialPlugin {
        private SpriteSuperNovaVertexShaderFragment mVertexShader;
        private SpriteSuperNovaFragmentShaderFragment mFragmentShader;

        public PointSpriteSuperNovaMaterialPlugin(float diameter)
        {
            mVertexShader = new SpriteSuperNovaVertexShaderFragment(diameter);
            mFragmentShader = new SpriteSuperNovaFragmentShaderFragment();
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

        private class SpriteSuperNovaVertexShaderFragment extends AShader implements IShaderFragment
        {
            public final static String SHADER_ID = "SPRITE_SUPERNOVA_VERTEX";
            float mDiameter;

            public SpriteSuperNovaVertexShaderFragment(float diameter)
            {
                super(ShaderType.VERTEX_SHADER_FRAGMENT);
                mDiameter = diameter;
                initialize();
            }

            @Override
            public void main() {
                RFloat u_time = (RFloat) getGlobal(DefaultShaderVar.U_TIME);
                RVec4 a_position = (RVec4) getGlobal(DefaultShaderVar.A_POSITION);
                RVec4 g_position = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);

                g_position.assign(a_position.multiply(castVec4(castVec3(u_time), 1)));
                GL_POINT_SIZE.assign(mDiameter);
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

        private class SpriteSuperNovaFragmentShaderFragment extends AShader implements IShaderFragment
        {
            public final static String SHADER_ID = "SPRITE_SUPERNOVA_FRAGMENT";

            public SpriteSuperNovaFragmentShaderFragment()
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
                RVec3 color = new RVec3("color");
                color.assign(castVec3("1.-d"));
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
}

