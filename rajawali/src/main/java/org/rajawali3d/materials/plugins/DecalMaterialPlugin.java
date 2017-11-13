package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;


public class DecalMaterialPlugin implements IMaterialPlugin {
    private final static String U_DECAL_TEXTURE = "uDecalTexture";
    private final static String U_DECAL_OFFSET = "uDecalOffset";
    private final static String U_DECAL_REPEAT = "uDecalRepeat";

    private DecalFragmentShaderFragment mFragmentShader;

    public DecalMaterialPlugin(Texture decalMap) {
        TextureManager.getInstance().addTexture(decalMap);
        mFragmentShader = new DecalFragmentShaderFragment();
        mFragmentShader.setDecalMapTexture(decalMap);
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_LIGHTING;
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

    private final class DecalFragmentShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "DECAL_FRAGMENT_SHADER_FRAGMENT";

        private RSampler2D muDecalTexture;
        private RVec2 muDecalOffset;
        private RVec2 muDecalRepeat;

        private int muDecalTextureHandle;
        private int muDecalOffsetHandle;
        private int muDecalRepeatHandle;
        private ATexture mDecalTexture;

        public DecalFragmentShaderFragment() {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            initialize();
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.PRE_LIGHTING;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        public void setDecalMapTexture(ATexture decalMapTexture) {
            mDecalTexture = decalMapTexture;
            int[] genTextureNames = new int[1];
            GLES20.glGenTextures(1, genTextureNames, 0);
            mDecalTexture.setTextureId(genTextureNames[0]);
        }

        @Override
        public void initialize() {
            super.initialize();
            muDecalTexture = (RSampler2D) addUniform(U_DECAL_TEXTURE, DataType.SAMPLER2D);
            muDecalOffset = (RVec2) addUniform(U_DECAL_OFFSET, DataType.VEC2);
            muDecalRepeat = (RVec2) addUniform(U_DECAL_REPEAT, DataType.VEC2);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muDecalTextureHandle = getUniformLocation(programHandle, U_DECAL_TEXTURE);
            muDecalOffsetHandle = getUniformLocation(programHandle, U_DECAL_OFFSET);
            muDecalRepeatHandle = getUniformLocation(programHandle, U_DECAL_REPEAT);
        }

        @Override
        public void applyParams() {
            super.applyParams();
            if(mDecalTexture.offsetEnabled())
                GLES20.glUniform2fv(muDecalOffsetHandle, 1, mDecalTexture.getOffset(), 0);
            if(mDecalTexture.getWrapType() == ATexture.WrapType.REPEAT)
                GLES20.glUniform2fv(muDecalRepeatHandle, 1, mDecalTexture.getRepeat(), 0);
        }

        @Override
        public void bindTextures(int nextIndex) {
            if(mDecalTexture != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + nextIndex);
                GLES20.glBindTexture(mDecalTexture.getGLTextureType(), mDecalTexture.getTextureId());
                GLES20.glUniform1i(muDecalTextureHandle, nextIndex);
            }
        }

        @Override
        public void unbindTextures() {
            if(mDecalTexture != null)
                GLES20.glBindTexture(mDecalTexture.getGLTextureType(), 0);
        }

        @Override
        public void main() {
            RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
            RVec4 decalCol = new RVec4("decalCol");
            if(mDecalTexture.offsetEnabled())
                textureCoord.assignAdd(muDecalOffset);
            if(mDecalTexture.getWrapType() == ATexture.WrapType.REPEAT)
                textureCoord.assignMultiply(muDecalRepeat);
            decalCol.assign(texture2D(muDecalTexture, textureCoord));

            RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            color.assign(mix(color, decalCol, decalCol.a()));
        }
    }
}
