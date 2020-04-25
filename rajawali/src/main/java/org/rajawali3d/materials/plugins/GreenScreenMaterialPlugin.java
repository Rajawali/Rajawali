package org.rajawali3d.materials.plugins;

import android.graphics.Color;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;

public class GreenScreenMaterialPlugin implements IMaterialPlugin {
        private ChromaKeyFragmentShaderFragment mFragmentShader;

        public GreenScreenMaterialPlugin(ATexture texture) {
            mFragmentShader = new ChromaKeyFragmentShaderFragment(texture, Color.GREEN, 0.03f, 0.3f);
        }

        public GreenScreenMaterialPlugin(ATexture texture, int keyColor) {
            mFragmentShader = new ChromaKeyFragmentShaderFragment(texture, keyColor, 0.03f, 0.3f);
        }

        public GreenScreenMaterialPlugin(ATexture texture, int keyColor, float matchDistance, float closeDistance) {
            mFragmentShader = new ChromaKeyFragmentShaderFragment(texture, keyColor, matchDistance, closeDistance);
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

        }

        @Override
        public void unbindTextures() {

        }


    /* applies a 2D CbCr distance based chromakey to a specified texture

       the current fragment color is a blend of material and texture colors.

       divides CrCb color plane into three regions clustered around the keyColor:
       - matched texture colors are subtracted from the blend, alpha is set equal to the color influence
       - unmatched colors are not changed
       - close colors are partially subtracted from the blend, alpha is the key value multiplied by the color influence.

     */
    final class ChromaKeyFragmentShaderFragment extends AShader implements IShaderFragment {
        final static String SHADER_ID = "CHROMAKEY_FRAGMENT_SHADER_FRAGMENT";
        private ATexture mTexure;
        int keyColor;
        float matchDistance;
        float closeDistance;
        float CrKey;
        float CbKey;

        public ChromaKeyFragmentShaderFragment(ATexture texture, int keyColor, float matchDistance, float closeDistance) {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            mTexure = texture;

            this.matchDistance = matchDistance;
            this.closeDistance = closeDistance;

            this.keyColor = keyColor;
            CrKey = (0.439f*Color.red(keyColor) - 0.368f*Color.green(keyColor) - 0.071f*Color.blue(keyColor))/255f;
            CbKey = (-0.148f*Color.red(keyColor) - 0.291f*Color.green(keyColor) + 0.439f*Color.blue(keyColor))/255f;

            initialize();
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.PRE_TRANSFORM;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int nextIndex) {

        }

        @Override
        public void unbindTextures() {

        }

        @Override
        public void main() {
            RFloat u_color_influence = (RFloat) getGlobal(DefaultShaderVar.U_COLOR_INFLUENCE);

            RVec4 textureColor = new RVec4("textureColor");
            textureColor.assign("texture2D(" + mTexure.getTextureName() + ", gTextureCoord)");
            RVec4 g_color = (RVec4) getGlobal(AShaderBase.DefaultShaderVar.G_COLOR);

            RFloat red = new RFloat("red");
            red.assign(textureColor.r().subtract(Color.red(keyColor)/255f));
            RFloat green = new RFloat("green");
            green.assign(textureColor.g().subtract(Color.green(keyColor)/255f));
            RFloat blue = new RFloat("blue");
            blue.assign(textureColor.b().subtract(Color.blue(keyColor)/255f));

            RFloat matchThreshold = new RFloat("matchThreshold");
            matchThreshold.assign(matchDistance);
            RFloat closeThreshold = new RFloat("closeThreshold");
            closeThreshold.assign(closeDistance);

            RFloat texCr = new RFloat("texCr");
            texCr.assign(0);
            texCr.assignAdd(textureColor.r().multiply(0.439f));
            texCr.assignAdd(textureColor.g().multiply(-0.368f));
            texCr.assignAdd(textureColor.b().multiply(-0.071f));

            RFloat deltaCr = new RFloat("deltaCr");
            deltaCr.assign(CrKey);
            deltaCr.assignSubtract(texCr);

            RFloat texCb = new RFloat("texCb");
            texCb.assign(0);
            texCb.assignAdd(textureColor.r().multiply(-0.148f));
            texCb.assignAdd(textureColor.g().multiply(-0.291f));
            texCb.assignAdd(textureColor.b().multiply(0.439f));

            RFloat deltaCb= new RFloat("deltaCb");
            deltaCb.assign(CbKey);
            deltaCb.assignSubtract(texCb);

            RFloat deviation = new RFloat("deviation");
            deviation.assign(sqrt(deltaCr.multiply(deltaCr).add(deltaCb.multiply(deltaCb))));

            startif(new Condition(deviation, Operator.LESS_THAN, matchThreshold));
            {
                g_color.rgb().assignSubtract(textureColor.rgb().multiply(mTexure.getInfluence()));
                g_color.a().assign(u_color_influence);
            }
            ifelseif(new Condition(deviation, Operator.LESS_THAN, closeThreshold));
            {
                RFloat keyValue = new RFloat("keyValue");
                keyValue.assign(1);
                keyValue.assignSubtract(deviation.subtract(matchThreshold).divide(closeThreshold.subtract(matchThreshold)));
                g_color.rgb().assignSubtract(textureColor.rgb().multiply(mTexure.getInfluence()).multiply(keyValue));
                g_color.a().assign(keyValue.multiply(u_color_influence));
            }
            endif();
        }
    }

}
