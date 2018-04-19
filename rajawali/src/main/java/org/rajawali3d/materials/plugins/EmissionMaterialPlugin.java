package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;

public class EmissionMaterialPlugin implements IMaterialPlugin {
    private final static String U_EMISSION_TEXTURE = "uEmissionTexture";
    private EmissionShaderFragment mFragmentShader;

    public EmissionMaterialPlugin(Texture emissionTexture) {
        TextureManager.getInstance().addTexture(emissionTexture);
        mFragmentShader = new EmissionShaderFragment();
        mFragmentShader.setEmissionTexture(emissionTexture);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return mFragmentShader.getInsertLocation();
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
        mFragmentShader.bindTextures(nextIndex);
    }

    @Override
    public void unbindTextures() {
        mFragmentShader.unbindTextures();
    }

    private final class EmissionShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "EMISSION_FRAGMENT_SHADER_FRAGMENT";

        private RSampler2D muEmissionTexture;
        private int muEmissionTextureHandle;
        private ATexture mEmissionTexture;

        public EmissionShaderFragment() {
            super();
            initialize();
        }

        public void setEmissionTexture(ATexture emissionTexture) {
            mEmissionTexture = emissionTexture;
            int[] genTextureNames = new int[1];
            GLES20.glGenTextures(1, genTextureNames, 0);
            mEmissionTexture.setTextureId(genTextureNames[0]);
        }

        @Override
        public void initialize() {
            super.initialize();
            muEmissionTexture = (RSampler2D) addUniform(U_EMISSION_TEXTURE, DataType.SAMPLER2D);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muEmissionTextureHandle = getUniformLocation(programHandle, U_EMISSION_TEXTURE);
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
            if(mEmissionTexture != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + nextIndex);
                GLES20.glBindTexture(mEmissionTexture.getGLTextureType(), mEmissionTexture.getTextureId());
                GLES20.glUniform1i(muEmissionTextureHandle, nextIndex);
            }
        }

        @Override
        public void unbindTextures() {
            if(mEmissionTexture != null)
                GLES20.glBindTexture(mEmissionTexture.getGLTextureType(), 0);
        }


        @Override
        public void main() {
            RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
            RVec4 emissionCol = new RVec4("emissionCol");
            emissionCol.assign(texture2D(muEmissionTexture, textureCoord));
            RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            color.assignAdd(emissionCol);
        }
    }
}
