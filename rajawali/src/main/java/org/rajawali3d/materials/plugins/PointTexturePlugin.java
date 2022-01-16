package org.rajawali3d.materials.plugins;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;

public class PointTexturePlugin implements IMaterialPlugin {
    private final PointVertexShaderFragment mVertexShader;
    private final PointFragmentShaderFragment mFragmentShader;

    public enum PointTextureShaderVar implements AShaderBase.IGlobalShaderVar {
        U_POINT_TEXTURE("uPointTexture", AShaderBase.DataType.SAMPLER2D),
        U_POINT_TRANSFORM("uPointTransform", AShaderBase.DataType.MAT3);

        private final String mVarString;
        private final AShaderBase.DataType mDataType;

        PointTextureShaderVar(String varString, AShaderBase.DataType dataType) {
            mVarString = varString;
            mDataType = dataType;
        }

        public String getVarString() {
            return mVarString;
        }

        public AShaderBase.DataType getDataType() {
            return mDataType;
        }
    }

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

        private RMat3 muPointTransform;
        private int muPointTransformHandle;

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
            muPointTexture = (RSampler2D) addUniform(PointTextureShaderVar.U_POINT_TEXTURE);
            muPointTransform = (RMat3) addUniform(PointTextureShaderVar.U_POINT_TRANSFORM);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muPointTextureHandle = getUniformLocation(programHandle, PointTextureShaderVar.U_POINT_TEXTURE);
            muPointTransformHandle = getUniformLocation(programHandle, PointTextureShaderVar.U_POINT_TRANSFORM);
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
        public void applyParams() {
            super.applyParams();
            if(mPointTexture.transformEnabled())
                GLES20.glUniformMatrix3fv(muPointTransformHandle, 1, false, mPointTexture.getTransform(), 0);
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
            RVec2 pointCoord = new RVec2("pointCoord", GL_POINT_COORD);
            if(mPointTexture.transformEnabled()) {
                RVec3 pointResult = new RVec3("pointResult");
                pointResult.assign(muPointTransform.multiply(castVec3(GL_POINT_COORD, 1)));
                pointCoord.assign(pointResult.xy());
            }
            color.assign(texture2D(muPointTexture, pointCoord));
        }
    }
}
