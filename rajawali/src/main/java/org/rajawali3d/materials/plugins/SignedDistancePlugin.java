package org.rajawali3d.materials.plugins;

import android.graphics.Color;
import android.opengl.GLES20;

import androidx.annotation.FloatRange;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.AShaderBase;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.MathUtil;

public class SignedDistancePlugin implements IMaterialPlugin {
    private final SignedDistanceFragmentShaderFragment mFragmentShader;

    public enum SignedDistanceShaderVar implements AShaderBase.IGlobalShaderVar {
        U_DISTANCE_FIELD("uDistanceField", AShaderBase.DataType.SAMPLER2D),
        U_FEATHERING("uFeathering", AShaderBase.DataType.FLOAT),
        U_GLYPH_TRANSFORM("uGlyphTransform", AShaderBase.DataType.MAT3),
        U_GLYPH_THRESHOLD("uThreshold", AShaderBase.DataType.FLOAT),
        U_GLYPH_COLOR("uGlyphColor", AShaderBase.DataType.VEC4),
        U_OUTLINE_THICKNESS("uOutlineThickness", AShaderBase.DataType.FLOAT),
        U_OUTLINE_COLOR("uOutlineColor", AShaderBase.DataType.VEC4);

        private String mVarString;
        private AShaderBase.DataType mDataType;

        SignedDistanceShaderVar(String varString, AShaderBase.DataType dataType) {
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

    public SignedDistancePlugin(Texture texture) {
        this(texture, 1/32f, 1/2f, Color.BLACK);
    }

    public SignedDistancePlugin(Texture texture,
                                @FloatRange(from = 0.0f, to = 1.0f) float feathering,
                                @FloatRange(from = 0.0f, to = 1.0f) float threshold,
                                int glyphColor) {
        TextureManager.getInstance().addTexture(texture);
        float[] rgbaGlyph = {
                Color.red(glyphColor)/255f,
                Color.green(glyphColor)/255f,
                Color.blue(glyphColor)/255f,
                Color.alpha(glyphColor)/255f
        };
        float[] rgbaOutline = {
                1-rgbaGlyph[0],
                1-rgbaGlyph[1],
                1-rgbaGlyph[2],
                rgbaGlyph[3]
        };
        float thickness = 1/2f;
        mFragmentShader = new SignedDistanceFragmentShaderFragment(feathering, threshold, rgbaGlyph, thickness, rgbaOutline);
        mFragmentShader.setDistanceTexture(texture);
    }

    public void setThreshold(@FloatRange(from = 0.0d, to = 1.0d) double threshold) {
        mFragmentShader.setThreshold(MathUtil.clamp((float)threshold, 0, 1));
    }

    public void setGlyphColor(int color) {
        float[] rgba = {
                Color.red(color)/255f,
                Color.green(color)/255f,
                Color.blue(color)/255f,
                Color.alpha(color)/255f
        };
        mFragmentShader.setGlyphColor(rgba);
    }

    public void setThickness(@FloatRange(from = 0.0d, to = 1.0d) double thickness) {
        mFragmentShader.setThickness(MathUtil.clamp((float)thickness, 0, 1));
    }

    public void setOulineColor(int color) {
        float[] rgba = {
                Color.red(color)/255f,
                Color.green(color)/255f,
                Color.blue(color)/255f,
                Color.alpha(color)/255f
        };
        mFragmentShader.setOutlineColor(rgba);
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

    private static final class SignedDistanceFragmentShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "SIGNED_DISTANCE_FRAGMENT_SHADER_FRAGMENT";

        private RSampler2D muDistanceTexture;
        private int muDistanceTextureHandle;
        private ATexture mDistanceTexture;

        private float mFeathering;
        private RFloat muFeathering;
        private int muFeatheringHandle;

        private RMat3 muGlyphTransform;
        private int muGlyphTransformHandle;

        private float mThreshold;
        private RFloat muThreshold;
        private int muThresholdHandle;

        private float[] mGlyphRGBA;
        private RVec4 muGlyphColor;
        private int muGlyphColorHandle;

        private float mThickness;
        private RFloat muOutlineThickness;
        private int muThicknessHandle;

        private float[] mOutlineRGBA;
        private RVec4 muOutlineColor;
        private int muOutlineColorHandle;

        public SignedDistanceFragmentShaderFragment(float feathering, float threshold, float[] rgbaGlyph, float thickness, float[] rgbaOutline) {
            super();
            mFeathering = MathUtil.clamp(feathering, 0, 1);
            mThreshold = MathUtil.clamp(threshold, 0, 1);
            mGlyphRGBA = rgbaGlyph;
            mThickness = MathUtil.clamp(1-thickness, 0, 1);
            mOutlineRGBA = rgbaOutline;
            initialize();
        }

        public void setDistanceTexture(ATexture distanceTexture) {
            mDistanceTexture = distanceTexture;
            int[] genTextureNames = new int[1];
            GLES20.glGenTextures(1, genTextureNames, 0);
            mDistanceTexture.setTextureId(genTextureNames[0]);
        }

        void setThreshold(@FloatRange(from = 0.0f, to = 1.0f) float threshold) {
            mThreshold = threshold;
        }

        void setGlyphColor(float[] rgba) {
            mGlyphRGBA = rgba;
        }

        void setThickness(@FloatRange(from = 0.0f, to = 1.0f) float thickness) {
            mThickness = 1-thickness;
        }

        void setOutlineColor(float[] rgba) {
            mOutlineRGBA = rgba;
        }

        @Override
        public void initialize() {
            super.initialize();
            muDistanceTexture = (RSampler2D) addUniform(SignedDistanceShaderVar.U_DISTANCE_FIELD.mVarString, DataType.SAMPLER2D);
            muFeathering = (RFloat) addUniform(SignedDistanceShaderVar.U_FEATHERING.mVarString, DataType.FLOAT);
            muGlyphTransform = (RMat3) addUniform(SignedDistanceShaderVar.U_GLYPH_TRANSFORM.mVarString, DataType.MAT3);
            muThreshold = (RFloat) addUniform(SignedDistanceShaderVar.U_GLYPH_THRESHOLD.mVarString, DataType.FLOAT);
            muGlyphColor = (RVec4) addUniform(SignedDistanceShaderVar.U_GLYPH_COLOR.mVarString, DataType.VEC4);
            muOutlineThickness = (RFloat) addUniform(SignedDistanceShaderVar.U_OUTLINE_THICKNESS.mVarString, DataType.FLOAT);
            muOutlineColor = (RVec4) addUniform(SignedDistanceShaderVar.U_OUTLINE_COLOR.mVarString, DataType.VEC4);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            muDistanceTextureHandle = getUniformLocation(programHandle, SignedDistanceShaderVar.U_DISTANCE_FIELD);
            muFeatheringHandle = getUniformLocation(programHandle, SignedDistanceShaderVar.U_FEATHERING);
            muGlyphTransformHandle = getUniformLocation(programHandle, SignedDistanceShaderVar.U_GLYPH_TRANSFORM);
            muThresholdHandle = getUniformLocation(programHandle, SignedDistanceShaderVar.U_GLYPH_THRESHOLD);
            muGlyphColorHandle = getUniformLocation(programHandle, SignedDistanceShaderVar.U_GLYPH_COLOR);
            muThicknessHandle = getUniformLocation(programHandle, SignedDistanceShaderVar.U_OUTLINE_THICKNESS);
            muOutlineColorHandle = getUniformLocation(programHandle, SignedDistanceShaderVar.U_OUTLINE_COLOR);
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(muFeatheringHandle, mFeathering);
            if(mDistanceTexture.transformEnabled())
                GLES20.glUniform2fv(muGlyphTransformHandle, 1, mDistanceTexture.getTransform(), 0);
            GLES20.glUniform1f(muThresholdHandle, mThreshold);
            GLES20.glUniform4fv(muGlyphColorHandle, 1, mGlyphRGBA, 0);
            GLES20.glUniform1f(muThicknessHandle, mThickness);
            GLES20.glUniform4fv(muOutlineColorHandle, 1, mOutlineRGBA, 0);
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.PRE_LIGHTING;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void bindTextures(int nextIndex) {
            if (mDistanceTexture != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + nextIndex);
                GLES20.glBindTexture(mDistanceTexture.getGLTextureType(), mDistanceTexture.getTextureId());
                GLES20.glUniform1i(muDistanceTextureHandle, nextIndex);
            }
        }

        @Override
        public void unbindTextures() {
            if (mDistanceTexture != null)
                GLES20.glBindTexture(mDistanceTexture.getGLTextureType(), 0);
        }

        @Override
        public void main() {
            RVec4 gColor = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            RVec2 vTextureCoord = (RVec2) getGlobal(DefaultShaderVar.V_TEXTURE_COORD);

            RVec2 glyphCoord = new RVec2("glyphCoord");
            RVec3 glyphResult = new RVec3("glyphResult");
            RVec4 overlay = new RVec4("overlay");
            RVec4 distance = new RVec4("distance");
            RFloat alpha = new RFloat("alpha");

            glyphCoord.assign(vTextureCoord);
            if(mDistanceTexture.transformEnabled()) {
                RMat3 transform = (RMat3) getGlobal(DefaultShaderVar.U_TRANSFORM, 0);
                glyphResult.assign(transform.multiply(castVec3(vTextureCoord, 1)));
                glyphCoord.assign(glyphResult.xy());
            }

            distance.assign(texture2D(muDistanceTexture, glyphCoord));
            alpha.assign(smoothstep(
                    muThreshold.subtract(muFeathering),
                    muThreshold.add(muFeathering),
                    distance.r()
            ));
            overlay.assign(mix(muOutlineColor, muGlyphColor, alpha));
            alpha.assign(smoothstep(
                    muThreshold.multiply(muOutlineThickness).subtract(muFeathering),
                    muThreshold.multiply(muOutlineThickness).add(muFeathering),
                    distance.r()
            ));
            gColor.assign(mix(gColor, overlay, alpha.multiply(overlay.a())));
        }
    }
}

