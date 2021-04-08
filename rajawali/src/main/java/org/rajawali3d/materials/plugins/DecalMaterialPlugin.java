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
    private final static String U_DECAL_TRANSFORM = "uDecalTransform";

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
        private RMat3 muDecalTransform;

        private int muDecalTextureHandle;
        private int muDecalTransformHandle;
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
            muDecalTransform = (RMat3) addUniform(U_DECAL_TRANSFORM, DataType.MAT3);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muDecalTextureHandle = getUniformLocation(programHandle, U_DECAL_TEXTURE);
            muDecalTransformHandle = getUniformLocation(programHandle, U_DECAL_TRANSFORM);
        }

        @Override
        public void applyParams() {
            super.applyParams();
            if(mDecalTexture.transformEnabled())
                GLES20.glUniformMatrix3fv(muDecalTransformHandle, 1, false, mDecalTexture.getTransform(), 0);
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
            RVec2 vTextureCoord = (RVec2) getGlobal(DefaultShaderVar.V_TEXTURE_COORD);
            RVec2 decalCoord = new RVec2("decalCoord");
            RVec4 decalCol = new RVec4("decalCol");
            RVec3 decalResult = new RVec3("decalResult");
            decalCoord.assign(vTextureCoord);
            if(mDecalTexture.transformEnabled()) {
                RMat3 transform = (RMat3) getGlobal(DefaultShaderVar.U_TRANSFORM, 0);
                decalResult.assign(transform.multiply(castVec3(vTextureCoord, 1)));
                decalCoord.assign(decalResult.xy());
            }
            decalCol.assign(texture2D(muDecalTexture, decalCoord));

            RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            color.assign(mix(color, decalCol, decalCol.a()));
        }
    }
}
