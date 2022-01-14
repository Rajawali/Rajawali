package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;

public class PointTexturePlugin implements IMaterialPlugin {
    private final static String U_POINT_TEXTURE = "uPointTexture";
    private final PointVertexShaderFragment mVertexShader;
    private final PointFragmentShaderFragment mFragmentShader;

    public PointTexturePlugin(Texture PointTexture, float aperture) {
        TextureManager.getInstance().addTexture(PointTexture);
        mVertexShader = new PointVertexShaderFragment(aperture);
        mFragmentShader = new PointFragmentShaderFragment(PointTexture);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return mFragmentShader.getInsertLocation();
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
    public void bindTextures(int nextIndex) {
        mFragmentShader.bindTextures(nextIndex);
    }

    @Override
    public void unbindTextures() {
        mFragmentShader.unbindTextures();
    }

    private static final class PointVertexShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "POINT_VERTEX_SHADER_FRAGMENT";
        float mAperture = 8.0f;

        public PointVertexShaderFragment(float aperture) {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            if(aperture > 0) mAperture = aperture;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.POST_TRANSFORM;
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
            GL_POINT_SIZE.assign(mAperture);
        }
    }

    private static final class PointFragmentShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "POINT_FRAGMENT_SHADER_FRAGMENT";

        private RSampler2D muPointTexture;
        private int muPointTextureHandle;
        private final ATexture mPointTexture;

        public PointFragmentShaderFragment(ATexture PointTexture) {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            mPointTexture = PointTexture;
            initialize();
        }

        @Override
        public void initialize() {
            super.initialize();
            int[] genTextureNames = new int[1];
            GLES20.glGenTextures(1, genTextureNames, 0);
            mPointTexture.setTextureId(genTextureNames[0]);
            muPointTexture = (RSampler2D) addUniform(U_POINT_TEXTURE, DataType.SAMPLER2D);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muPointTextureHandle = getUniformLocation(programHandle, U_POINT_TEXTURE);
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.POST_TRANSFORM;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int nextIndex) {
            if(mPointTexture != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + nextIndex);
                GLES20.glBindTexture(mPointTexture.getGLTextureType(), mPointTexture.getTextureId());
                GLES20.glUniform1i(muPointTextureHandle, nextIndex);
            }
        }

        @Override
        public void unbindTextures() {
            if(mPointTexture != null)
                GLES20.glBindTexture(mPointTexture.getGLTextureType(), 0);
        }

        @Override
        public void main() {
            RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            color.assign(texture2D(muPointTexture, GL_POINT_COORD));
        }
    }
}
