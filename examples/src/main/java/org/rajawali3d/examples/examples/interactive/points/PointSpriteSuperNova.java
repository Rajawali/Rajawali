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

    Bitmap createSprite(float diameter) {
        Bitmap bitmap = Bitmap.createBitmap(5,5, Bitmap.Config.ARGB_8888);
        for(int y=0; y<Math.ceil(diameter); y++) {
            for(int x=0; x<Math.ceil(diameter); x++) {
                float dx = x/diameter-0.5f;
                float dy = y/diameter-0.5f;
                int p = 255-Math.round(255f*(float)Math.sqrt(dx*dx+dy*dy));
                bitmap.setPixel(x,y,Color.argb(p,p,p,p));
            }
        }
        return bitmap;
    }

    public void init(int numSprites, float spriteDiameter) {
        try {
            setTransparent(true);
            setBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            setDrawingMode(GLES20.GL_POINTS);
            Texture mSpritesTex = new Texture("sprite", createSprite(spriteDiameter));
            Material mSpritesMat = new Material();
            mSpritesMat.addTexture(mSpritesTex);
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
            public void initialize()
            {
                super.initialize();
            }

            @Override
            public void main() {
                mShaderSB.append("\n" +
                        "mat4 m = mat4(0.);\n" +
                        "m[0][0] = uTime;\n" +
                        "m[1][1] = uTime;\n" +
                        "m[2][2] = uTime;\n" +
                        "m[3][3] = 1.;\n" +
                        "\n" +
                        "gl_PointSize = " +  mDiameter + ";\n" +
                        "\n");
                RVec4 g_position = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);
                g_position.assign("aPosition * m");

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
            public void initialize()
            {
                super.initialize();
            }

            @Override
            public void main() {
                RVec4 g_color = (RVec4) getGlobal(AShaderBase.DefaultShaderVar.G_COLOR);
                g_color.assign("vColor * texture2D(sprite, gl_PointCoord)");
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
