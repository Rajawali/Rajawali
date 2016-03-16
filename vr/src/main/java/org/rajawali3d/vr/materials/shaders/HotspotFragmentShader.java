/**
 * Copyright 2015 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.rajawali3d.vr.materials.shaders;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.math.vector.Vector2;

/**
 * @author dennis.ippel
 */
public class HotspotFragmentShader extends FragmentShader {
    private RFloat mcPI;
    private RFloat mcTwoPI;

    private RVec2 muCircleCenter;
    private RVec2 mvTextureCoord;

    private RVec4 muTrackColor;
    private RVec4 muProgressColor;

    private RFloat muCircleRadius;
    private RFloat muBorderThickness;
    private RFloat muTextureRotationSpeed;
    private RFloat muProgress;
    private RFloat muTime;

    private RSampler2D muTexture;

    private float[] mCircleCenter = new float[] { 0.5f, 0.5f };
    private float[] mTrackColor = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
    private float[] mProgressColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

    private float mCircleRadius = 0.4f;
    private float mBorderThickness = 0.05f;
    private float mTextureRotationSpeed = 2.0f;
    private float mProgress;

    private int muCircleCenterHandle;
    private int muTrackColorHandle;
    private int muProgressColorHandle;
    private int muCircleRadiusHandle;
    private int muBorderThicknessHandle;
    private int muTextureRotationSpeedHandle;
    private int muProgressHandle;
    private int muTextureHandle;

    private boolean mUseTexture;
    private boolean mDiscardAlpha;

    public HotspotFragmentShader(boolean useTexture) {
        this(useTexture, false);
    }

    public HotspotFragmentShader(boolean useTexture, boolean discardalpha) {
        super();

        mUseTexture = useTexture;
        mDiscardAlpha = discardalpha;

        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();

        addPrecisionQualifier(DataType.FLOAT, Precision.MEDIUMP);

        mcPI = (RFloat) addConst("PI", Math.PI);
        mcTwoPI = (RFloat) addConst("TWO_PI", Math.PI * 2.0);

        mvTextureCoord = (RVec2) addVarying(DefaultShaderVar.V_TEXTURE_COORD);

        muCircleCenter = (RVec2) addUniform("uCircleCenter", DataType.VEC2);

        muTrackColor = (RVec4) addUniform("uTrackColor", DataType.VEC4);
        muProgressColor = (RVec4) addUniform("uProgressColor", DataType.VEC4);

        muCircleRadius = (RFloat) addUniform("uCircleRadius", DataType.FLOAT);
        muBorderThickness = (RFloat) addUniform("uBorderThickness", DataType.FLOAT);
        muProgress = (RFloat) addUniform("uProgress", DataType.FLOAT);

        muTime = (RFloat) addUniform(DefaultShaderVar.U_TIME);

        muTexture = (RSampler2D) addUniform("uProgressTexture", DataType.SAMPLER2D);

        if(mUseTexture)
            muTextureRotationSpeed = (RFloat) addUniform("uTextureRotationSpeed", DataType.FLOAT);
    }

    @Override
    public void main() {
        //        vec2 uv = fragCoord.xy / iResolution.xy;
        RVec2 uv = new RVec2("uv", mvTextureCoord);
        //        float circleBorderHalf = circleBorder * 0.5;
        RFloat circleBorderHalf = new RFloat("circleBorderHalf");
        circleBorderHalf.assign(muBorderThickness.multiply(0.5f));

        //        float dist = distance(uv, circleCenter);
        ShaderVar dist = distance(uv, muCircleCenter);

        RVec4 outColor = new RVec4("outColor", new RVec4("vec4(0.0)"));
        //outColor.a().assign(0.0f);

        if(mUseTexture) {
            //        float yScale = sin(iGlobalTime * textureRotationSpeed) * 0.5 + 0.5;
            ShaderVar yScale = sin(muTime.multiply(muTextureRotationSpeed)).multiply(0.5f).add(0.5f);
            RFloat one = new RFloat("one", new RFloat(1.0f));
            //one.assign(1.0f);
            //        vec2 uvAnim = vec2(uv.x, uv.y - 0.5);
            RVec2 uvAnim = new RVec2("uvAnim", new RVec2("vec2(0.0)"));
            uvAnim.x().assign(uv.x());
            uvAnim.y().assign(uv.y().subtract(0.5f));
            //        uvAnim.y *= 1.0 / yScale;
            uvAnim.y().assignMultiply(one.divide(yScale));
            //        uvAnim.y += 0.5;
            uvAnim.y().assignAdd(0.5f);
            //        outColor = texture2D(iChannel0, uvAnim);
            RVec4 color = (RVec4)getGlobal(DefaultShaderVar.G_COLOR);
            outColor.assign(texture2D(muTexture, uvAnim));
        }

        //        if(dist < circleRadius + circleBorderHalf && dist > circleRadius - circleBorderHalf) {
        startif(
                new Condition(dist, Operator.LESS_THAN, muCircleRadius.add(circleBorderHalf)),
                new Condition(Operator.AND, dist, Operator.GREATER_THAN, muCircleRadius.subtract(circleBorderHalf))
        );
        {
            //            outColor = trackColor;
            outColor.assign(muTrackColor);
            //            float startAngle = 0.0;
            RFloat startAngle = new RFloat("startAngle", new RFloat(0));
            //            float endAngle = startAngle + mod(iGlobalTime * 2.0, PI_2);
            RFloat endAngle = new RFloat("endAngle", startAngle.add(muProgress.multiply(mcTwoPI)));
            //            uv -= circleCenter;
            uv.assignSubtract(muCircleCenter);
            //            float angle = -1.0 * atan(uv.y, uv.x) + PI;
            RFloat angle = new RFloat("angle", atan(uv.y(), uv.x()).multiply(-1));
            angle.assignAdd(mcPI);

            //            if(angle >= startAngle && angle <= endAngle) {
            startif(new Condition(angle, Operator.GREATER_THAN_EQUALS, startAngle),
                    new Condition(Operator.AND, angle, Operator.LESS_THAN_EQUALS, endAngle));
            {
                //                outColor = progressColor;
                outColor.assign(muProgressColor);
            }
            endif();
        }
        endif();

        if(mDiscardAlpha) {
            startif(new Condition(outColor.a(), Operator.EQUALS, 0));
            {
                discard();
            }
            endif();
        }

        //        fragColor = outColor;
        GL_FRAG_COLOR.assign(outColor.getName());
    }

    @Override
    public void setLocations(final int programHandle) {
        super.setLocations(programHandle);

        muCircleCenterHandle = getUniformLocation(programHandle, "uCircleCenter");
        muTrackColorHandle = getUniformLocation(programHandle, "uTrackColor");
        muProgressColorHandle = getUniformLocation(programHandle, "uProgressColor");
        muCircleRadiusHandle = getUniformLocation(programHandle, "uCircleRadius");
        muBorderThicknessHandle = getUniformLocation(programHandle, "uBorderThickness");
        muTextureRotationSpeedHandle = getUniformLocation(programHandle, "uTextureRotationSpeed");
        muProgressHandle = getUniformLocation(programHandle, "uProgress");
        muTextureHandle = getUniformLocation(programHandle, "uProgressTexture");
    }

    @Override
    public void applyParams() {
        super.applyParams();

        GLES20.glUniform2fv(muCircleCenterHandle, 1, mCircleCenter, 0);
        GLES20.glUniform4fv(muTrackColorHandle, 1, mTrackColor, 0);
        GLES20.glUniform4fv(muProgressColorHandle, 1, mProgressColor, 0);
        GLES20.glUniform1f(muCircleRadiusHandle, mCircleRadius);
        GLES20.glUniform1f(muBorderThicknessHandle, mBorderThickness);
        GLES20.glUniform1f(muTextureRotationSpeedHandle, mTextureRotationSpeed);
        GLES20.glUniform1f(muProgressHandle, mProgress);
    }

    public void setCircleCenter(Vector2 center) {
        mCircleCenter[0] = (float)center.getX();
        mCircleCenter[1] = (float)center.getY();
    }

    public void setTrackColor(int color) {
        mTrackColor[0] = (float) Color.red(color) / 255.f;
        mTrackColor[1] = (float) Color.green(color) / 255.f;
        mTrackColor[2] = (float) Color.blue(color) / 255.f;
        mTrackColor[3] = (float) Color.alpha(color) / 255.f;
    }

    public void setProgressColor(int color) {
        mProgressColor[0] = (float) Color.red(color) / 255.f;
        mProgressColor[1] = (float) Color.green(color) / 255.f;
        mProgressColor[2] = (float) Color.blue(color) / 255.f;
        mProgressColor[3] = (float) Color.alpha(color) / 255.f;
    }

    public void setCircleRadius(float circleRadius) {
        mCircleRadius = circleRadius;
    }

    public void setBorderThickness(float borderThickness) {
        mBorderThickness = borderThickness;
    }

    public void setTextureRotationSpeed(float textureRotationSpeed) {
        mTextureRotationSpeed = textureRotationSpeed;
    }

    public void setProgress(float progress) {
        mProgress = progress;
    }

    public float getProgress() {
        return mProgress;
    }
}
