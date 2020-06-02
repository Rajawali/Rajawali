/**
 * Copyright 2013 Dennis Ippel
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
package org.rajawali3d.materials;

import android.graphics.Color;
import android.opengl.GLES20;
import androidx.annotation.NonNull;

import org.rajawali3d.BufferInfo;
import org.rajawali3d.Object3D;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.IDiffuseMethod;
import org.rajawali3d.materials.methods.ISpecularMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.materials.shaders.fragments.LightsFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.LightsVertexShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.AlphaMapFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.DiffuseTextureFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.EnvironmentMapFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.LightMapFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.NormalMapFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.SkyTextureFragmentShaderFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.materials.textures.CubeMapTexture;
import org.rajawali3d.materials.textures.SphereMapTexture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.util.Capabilities;
import org.rajawali3d.util.RajLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Material class is where you define the visual characteristics of your 3D model.
 * Here you can specify lighting parameters, specular highlights, ambient colors and much more.
 * This is the place where you add textures as well. For an overview of the different types
 * of materials and parameters visit the Rajawali Wiki.
 * <p/>
 * This is a basic example using lighting, a texture, Lambertian diffuse model and Phong specular highlights:
 * <pre><code>
 * Material material = new Material();
 * material.addTexture(new Texture("earth", R.drawable.earth_diffuse));
 * material.enableLighting(true);
 * material.setDiffuseMethod(new DiffuseMethod.Lambert());
 * material.setSpecularMethod(new SpecularMethod.Phong());
 * <p/>
 * myObject.setMaterial(material);
 * </code></pre>
 *
 * @author dennis.ippel
 * @see <a href="https://github.com/MasDennis/Rajawali/wiki/Materials">https://github.com/MasDennis/Rajawali/wiki/Materials</a>
 */
public class Material {
    /**
     * This tells the Material class where to insert a shader fragment into either
     * the vertex of fragment shader.
     */
    public enum PluginInsertLocation {
        PRE_LIGHTING, PRE_DIFFUSE, PRE_SPECULAR, PRE_ALPHA, PRE_TRANSFORM, POST_TRANSFORM, IGNORE
    }

    private final boolean mCapabilitiesCheckDeferred;

    /**
     * The generic vertex shader. This can be extended by using vertex shader fragments.
     * A vertex shader is typically used to modify vertex positions, vertex colors and normals.
     */
    private VertexShader mVertexShader;
    /**
     * The generic fragment shader. This can be extended by using fragment shader fragments.
     * A fragment shader is typically used to modify rasterized pixel colors.
     */
    private FragmentShader mFragmentShader;
    /**
     * The shader fragments that are plugged into both the vertex and fragment shader. This
     * is where lighting calculations are performed.
     */
    private LightsVertexShaderFragment mLightsVertexShaderFragment;
    /**
     * The diffuse method specifies the reflection of light from a surface such that an incident
     * ray is reflected at many angles rather than at just one angle as in the case of specular reflection.
     * This can be set using the setDiffuseMethod() method:
     * <pre><code>
     * material.setDiffuseMethod(new DiffuseMethod.Lambert());
     * </code></pre>
     */
    private IDiffuseMethod mDiffuseMethod;
    /**
     * The specular method specifies the mirror-like reflection of light (or of other kinds of wave)
     * from a surface, in which light from a single incoming direction (a ray) is reflected into a
     * single outgoing direction.
     * This can be set using the setSpecularMethod() method:
     * <pre><code>
     * material.setSpecularMethod(new SpecularMethod.Phong());
     * </code></pre>
     */
    private ISpecularMethod mSpecularMethod;
    /**
     * Indicates that this material should use a color value for every vertex. These colors are
     * contained in a separate color buffer.
     */
    private boolean mUseVertexColors;
    /**
     * Indicates whether lighting should be used or not. This must be set to true when using a
     * {@link DiffuseMethod} or a {@link SpecularMethod}. Lights are added to a scene {@link Scene}
     * and are automatically added to the material.
     */
    private boolean mLightingEnabled;
    /**
     * Indicates that the time shader parameter should be used. This is used when creating shaders
     * that should change during the course of time. This is used to accomplish effects like animated
     * vertices, vertex colors, plasma effects, etc. The time needs to be manually updated using the
     * {@link Material#setTime(float)} method.
     */
    private boolean mTimeEnabled;
    /**
     * Indicates that one of the material properties was changed and that the shader program should
     * be re-compiled.
     */
    private boolean mIsDirty = true;
    /**
     * Holds a reference to the shader program
     */
    private int mProgramHandle = -1;
    /**
     * Holds a reference to the vertex shader
     */
    private int mVShaderHandle;
    /**
     * Holds a reference to the fragment shader
     */
    private int mFShaderHandle;
    /**
     * The model matrix holds the object's local coordinates
     */
    private Matrix4 mModelMatrix;
    /**
     * The inverse view matrix is used to transform reflections
     */
    private float[] mInverseViewMatrix;
    /**
     * The model view matrix is used to transform vertices to eye coordinates
     */
    private float[] mModelViewMatrix;
    /**
     * The material's diffuse color. This can be overwritten by {@link Object3D#setColor(int)}.
     * This color will be applied to the whole object. For vertex colors use {@link Material#useVertexColors(boolean)}
     * and {@link Material#setVertexColors(int)}.
     */
    private float[] mColor;
    /**
     * This material's ambient color. Ambient color is the color of an object where it is in shadow.
     */
    private float[] mAmbientColor;
    /**
     * This material's ambient intensity for the r, g, b channels.
     */
    private float[] mAmbientIntensity;
    /**
     * The color influence indicates how big the influence of the color is. This should be
     * used in conjunction with {@link ATexture#setInfluence(float)}. A value of .5 indicates
     * an influence of 50%. This examples shows how to use 50% color and 50% texture:
     * <p/>
     * <pre><code>
     * material.setColorInfluence(.5f);
     * myTexture.setInfluence(.5f);
     * </code></pre>
     */
    private float mColorInfluence = 1;
    /**
     * Sets the time value that is used in the shaders to create animated effects.
     * <p/>
     * <pre><code>
     * public class MyRenderer extends Renderer
     * {
     * 		private double mStartTime;
     * 		private Material mMyMaterial;
     * <p/>
     * 		protected void initScene() {
     * 			mStartTime = SystemClock.elapsedRealtime();
     * 			...
     *        }
     * <p/>
     * 		public void onDrawFrame(GL10 glUnused) {
     * 			super.onDrawFrame(glUnused);
     * 			mMyMaterial.setTime((SystemClock.elapsedRealtime() - mLastRender) / 1000d);
     * 			...
     *        }
     * <p/>
     * </code></pre>
     */
    private float mTime;
    /**
     * The lights that affect the material. Lights shouldn't be managed by any other class
     * than {@link Scene}. To add lights to a scene call {@link Scene#addLight(ALight).
     */
    protected List<ALight> mLights;
    /**
     * A list of material plugins that are used by this material. A material plugin is basically
     * a class that contains a vertex shader fragment and a fragment shader fragment. Material
     * plugins can be used for custom shader effects.
     */
    protected List<IMaterialPlugin> mPlugins;

    /**
     * This texture's unique owner identity String. This is usually the fully qualified name of the
     * {@link Renderer} instance.
     */
    protected String mOwnerIdentity;
    /**
     * The maximum number of available textures for this device. This value is returned from
     * {@link Capabilities#getMaxTextureImageUnits()}.
     */
    private int mMaxTextures;
    /**
     * The list of textures that are assigned by this materials.
     */
    protected ArrayList<ATexture> mTextureList;

    protected Map<String, Integer> mTextureHandles;
    /**
     * Contains the normal matrix. The normal matrix is used in the shaders to transform
     * the normal into eye space.
     */
    protected final float[] mNormalFloats = new float[9];
    /**
     * Scratch normal matrix. The normal matrix is used in the shaders to transform
     * the normal into eye space.
     */
    protected Matrix4 mNormalMatrix = new Matrix4();
    protected VertexShader mCustomVertexShader;
    protected FragmentShader mCustomFragmentShader;

    /**
     * The Material class is where you define the visual characteristics of your 3D model.
     * Here you can specify lighting parameters, specular highlights, ambient colors and much more.
     * This is the place where you add textures as well. For an overview of the different types
     * of materials and parameters visit the Rajawali Wiki.
     * <p/>
     * This is a basic example using lighting, a texture, Lambertian diffuse model and Phong specular highlights:
     * <pre><code>
     * Material material = new Material();
     * material.addTexture(new Texture("earth", R.drawable.earth_diffuse));
     * material.enableLighting(true);
     * material.setDiffuseMethod(new DiffuseMethod.Lambert());
     * material.setSpecularMethod(new SpecularMethod.Phong());
     * <p/>
     * myObject.setMaterial(material);
     * </code></pre>
     *
     * @see <a href="https://github.com/MasDennis/Rajawali/wiki/Materials">https://github.com/MasDennis/Rajawali/wiki/Materials</a>
     */
    public Material() {
        this(false);
    }

    public Material(boolean deferCapabilitiesCheck) {
        mCapabilitiesCheckDeferred = deferCapabilitiesCheck;
        mTextureList = new ArrayList<>();
        mTextureHandles = new HashMap<>();

        // If we have deffered the capabilities check, we have no way of knowing how many textures this material
        // is capable of having. We could choose 8, the minimum required fragment shader texture unit count, but
        // that would not allow us to finish construction of this material until the EGL context is available. Instead,
        // we are choosing the maximum integer Java can handle, and we will print a warning if the number of added textures
        // exceeds the capability once known. In this event they will be used in listed order until the max is hit.
        mMaxTextures = mCapabilitiesCheckDeferred ? Integer.MAX_VALUE : Capabilities.getInstance().getMaxTextureImageUnits();

        mColor = new float[]{1, 0, 0, 1};
        mAmbientColor = new float[]{.2f, .2f, .2f};
        mAmbientIntensity = new float[]{.3f, .3f, .3f};
    }

    public Material(VertexShader customVertexShader, FragmentShader customFragmentShader) {
        this(customVertexShader, customFragmentShader, false);
    }

    public Material(VertexShader customVertexShader, FragmentShader customFragmentShader, boolean deferCapabilitiesCheck) {
        this(deferCapabilitiesCheck);
        mCustomVertexShader = customVertexShader;
        mCustomFragmentShader = customFragmentShader;
    }

    /**
     * Indicates that this material should use a color value for every vertex. These colors are
     * contained in a separate color buffer.
     *
     * @return A boolean indicating that vertex colors will be used.
     */
    public boolean usingVertexColors() {
        return mUseVertexColors;
    }

    /**
     * Indicates that this material should use a color value for every vertex. These colors are
     * contained in a separate color buffer.
     *
     * @param value A boolean indicating whether vertex colors should be used or not
     */
    public void useVertexColors(boolean value) {
        if (value != mUseVertexColors) {
            mIsDirty = true;
            mUseVertexColors = value;
        }
    }

    /**
     * The material's diffuse color. This can be overwritten by {@link Object3D#setColor(int)}.
     * This color will be applied to the whole object. For vertex colors use {@link Material#useVertexColors(boolean)}
     * and {@link Material#setVertexColors(int)}.
     *
     * @param color {@code int} color The color to be used. Color.RED for instance. Or 0xffff0000.
     */
    public void setColor(int color) {
        mColor[0] = (float) Color.red(color) / 255.f;
        mColor[1] = (float) Color.green(color) / 255.f;
        mColor[2] = (float) Color.blue(color) / 255.f;
        mColor[3] = (float) Color.alpha(color) / 255.f;
        if (mVertexShader != null)
            mVertexShader.setColor(mColor);
    }

    /**
     * The material's diffuse color. This can be overwritten by {@link Object3D#setColor(int)}.
     * This color will be applied to the whole object. For vertex colors use {@link Material#useVertexColors(boolean)}
     * and {@link Material#setVertexColors(int)}.
     *
     * @param color A float array containing the colors to be used. These are normalized values containing values for
     *              the red, green, blue and alpha channels.
     */
    public void setColor(float[] color) {
        mColor[0] = color[0];
        mColor[1] = color[1];
        mColor[2] = color[2];
        mColor[3] = color[3];
        if (mVertexShader != null)
            mVertexShader.setColor(mColor);
    }

    /**
     * Returns this material's diffuse color.
     *
     * @return
     */
    public int getColor() {
        return Color.argb((int) (mColor[3] * 255), (int) (mColor[0] * 255), (int) (mColor[1] * 255), (int) (mColor[2] * 255));
    }

    /**
     * The color influence indicates how big the influence of the color is. This should be
     * used in conjunction with {@link ATexture#setInfluence(float)}. A value of .5 indicates
     * an influence of 50%. This examples shows how to use 50% color and 50% texture:
     * <p/>
     * <pre><code>
     * material.setColorInfluence(.5f);
     * myTexture.setInfluence(.5f);
     * </code></pre>
     *
     * @param influence A value in the range of [0..1] indicating the color influence. Use .5 for
     *                  50% color influence, .75 for 75% color influence, etc.
     */
    public void setColorInfluence(float influence) {
        mColorInfluence = influence;
    }

    /**
     * Indicates the color influence. Use .5 for 50% color influence, .75 for 75% color influence, etc.
     *
     * @return A value in the range of [0..1]
     */
    public float getColorInfluence() {
        return mColorInfluence;
    }

    /**
     * This material's ambient color. Ambient color is the color of an object where it is in shadow.
     *
     * @param color The color to be used. Color.RED for instance. Or 0xffff0000.
     */
    public void setAmbientColor(int color) {
        mAmbientColor[0] = (float) Color.red(color) / 255.f;
        mAmbientColor[1] = (float) Color.green(color) / 255.f;
        mAmbientColor[2] = (float) Color.blue(color) / 255.f;
        if (mLightsVertexShaderFragment != null)
            mLightsVertexShaderFragment.setAmbientColor(mAmbientColor);
    }

    /**
     * This material's ambient color. Ambient color is the color of an object where it is in shadow.
     *
     * @param color A float array containing the colors to be used. These are normalized values containing values for
     *              the red, green, blue and alpha channels.
     */
    public void setAmbientColor(float[] color) {
        mAmbientColor[0] = color[0];
        mAmbientColor[1] = color[1];
        mAmbientColor[2] = color[2];
        if (mLightsVertexShaderFragment != null)
            mLightsVertexShaderFragment.setAmbientColor(mAmbientColor);
    }

    /**
     * Returns this material's ambient color. Ambient color is the color of an object where it is in shadow.
     *
     * @return
     */
    public int getAmbientColor() {
        return Color.argb(1, (int) (mAmbientColor[0] * 255), (int) (mAmbientColor[1] * 255), (int) (mAmbientColor[2] * 255));
    }

    /**
     * This material's ambient intensity for the r, g, b channels.
     *
     * @param r The value [0..1] for the red channel
     * @param g The value [0..1] for the green channel
     * @param b The value [0..1] for the blue channel
     */
    public void setAmbientIntensity(double r, double g, double b) {
        setAmbientIntensity((float) r, (float) g, (float) b);
    }

    /**
     * This material's ambient intensity for the r, g, b channels.
     *
     * @param r The value [0..1] for the red channel
     * @param g The value [0..1] for the green channel
     * @param b The value [0..1] for the blue channel
     */
    public void setAmbientIntensity(float r, float g, float b) {
        mAmbientIntensity[0] = r;
        mAmbientIntensity[1] = g;
        mAmbientIntensity[2] = b;
        if (mLightsVertexShaderFragment != null)
            mLightsVertexShaderFragment.setAmbientIntensity(mAmbientIntensity);
    }

    /**
     * {@inheritDoc}
     */
    void add() {
        RajLog.d("Material is being added.");
        // We are being added to the scene, check the capabilities now if needed since they are available.
        checkCapabilitiesIfNeeded();

        if (mLightingEnabled && mLights == null)
            return;

        createShaders();
    }

    /**
     * {@inheritDoc}
     */
    void remove() {
        mModelMatrix = null;
        mInverseViewMatrix = null;
        mModelViewMatrix = null;

        if (mLights != null)
            mLights.clear();
        if (mTextureList != null)
            mTextureList.clear();

        if (Renderer.hasGLContext()) {
            GLES20.glDeleteShader(mVShaderHandle);
            GLES20.glDeleteShader(mFShaderHandle);
            GLES20.glDeleteProgram(mProgramHandle);
        }
    }

    /**
     * {@inheritDoc}
     */
    void reload() {
        mIsDirty = true;
        createShaders();
    }

    /**
     * Called prior to {@link VertexShader#initialize()} being called when creating auto-generated materials.
     *
     * @param vertexShader The {@link VertexShader}.
     */
    protected void onPreVertexShaderInitialize(@NonNull VertexShader vertexShader) {

    }

    /**
     * Called prior to {@link FragmentShader#initialize()} being called when creating auto-generated materials.
     *
     * @param fragmentShader The {@link FragmentShader}.
     */
    protected void onPreFragmentShaderInitialize(@NonNull FragmentShader fragmentShader) {

    }

    /**
     * Takes all material parameters and creates the vertex shader and fragment shader and then compiles the program.
     * This method should only be called on initialization or when parameters have changed.
     */
    protected void createShaders() {
        if (!mIsDirty)
            return;

        mVertexShader = mCustomVertexShader;
        mFragmentShader = mCustomFragmentShader;

        //
        // -- Check textures
        //

        List<ATexture> diffuseTextures = null;
        List<ATexture> lightMapTextures = null;
        List<ATexture> normalMapTextures = null;
        List<ATexture> envMapTextures = null;
        List<ATexture> skyTextures = null;
        List<ATexture> specMapTextures = null;
        List<ATexture> alphaMapTextures = null;

        boolean hasCubeMaps = false;
        boolean hasVideoTexture = false;

        for (int i = 0; i < mTextureList.size(); i++) {
            ATexture texture  = mTextureList.get(i);

            switch (texture.getTextureType()) {
                case VIDEO_TEXTURE:
                    hasVideoTexture = true;
                    // no break statement, add the video texture to the diffuse textures
                case DIFFUSE:
                case RENDER_TARGET:
                    if (diffuseTextures == null) diffuseTextures = new ArrayList<>();
                    diffuseTextures.add(texture);
                    break;
                case LIGHT:
                    if (lightMapTextures == null) lightMapTextures = new ArrayList<>();
                    lightMapTextures.add(texture);
                    break;
                case NORMAL:
                    if (normalMapTextures == null) normalMapTextures = new ArrayList<>();
                    normalMapTextures.add(texture);
                    break;
                case CUBE_MAP:
                    hasCubeMaps = true;
                case SPHERE_MAP:
                    boolean isSkyTexture = false;
                    boolean isEnvironmentTexture = false;

                    if (texture.getClass() == SphereMapTexture.class) {
                        isSkyTexture = ((SphereMapTexture) texture).isSkyTexture();
                        isEnvironmentTexture = ((SphereMapTexture) texture).isEnvironmentTexture();
                    } else if (texture.getClass() == CubeMapTexture.class) {
                        isSkyTexture = ((CubeMapTexture) texture).isSkyTexture();
                        isEnvironmentTexture = ((CubeMapTexture) texture).isEnvironmentTexture();
                    }

                    if (isSkyTexture) {
                        if (skyTextures == null)
                            skyTextures = new ArrayList<>();
                        skyTextures.add(texture);
                    } else if (isEnvironmentTexture) {
                        if (envMapTextures == null)
                            envMapTextures = new ArrayList<>();
                        envMapTextures.add(texture);
                    } else if(hasCubeMaps) {
                        if (diffuseTextures == null) diffuseTextures = new ArrayList<>();
                        diffuseTextures.add(texture);
                    }
                    break;
                case SPECULAR:
                    if (specMapTextures == null) specMapTextures = new ArrayList<>();
                    specMapTextures.add(texture);
                    break;
                case ALPHA:
                    if (alphaMapTextures == null) alphaMapTextures = new ArrayList<>();
                    alphaMapTextures.add(texture);
                    break;
                default:
                    break;
            }
        }

        if (mVertexShader == null && mFragmentShader == null) {
            mVertexShader = new VertexShader();
            mVertexShader.enableTime(mTimeEnabled);
            mVertexShader.hasCubeMaps(hasCubeMaps);
            mVertexShader.hasSkyTexture(skyTextures != null && skyTextures.size() > 0);
            mVertexShader.useVertexColors(mUseVertexColors);
            onPreVertexShaderInitialize(mVertexShader);
            mVertexShader.initialize();
            mFragmentShader = new FragmentShader();
            mFragmentShader.enableTime(mTimeEnabled);
            mFragmentShader.hasCubeMaps(hasCubeMaps);
            onPreFragmentShaderInitialize(mFragmentShader);
            mFragmentShader.initialize();
        } else {
            if (mVertexShader.needsBuild()) mVertexShader.initialize();
            if (mFragmentShader.needsBuild()) mFragmentShader.initialize();
        }

        if (diffuseTextures != null && diffuseTextures.size() > 0) {
            DiffuseTextureFragmentShaderFragment fragment = new DiffuseTextureFragmentShaderFragment(diffuseTextures);
            mFragmentShader.addShaderFragment(fragment);
        }

        if (normalMapTextures != null && normalMapTextures.size() > 0) {
            NormalMapFragmentShaderFragment fragment = new NormalMapFragmentShaderFragment(normalMapTextures);
            mFragmentShader.addShaderFragment(fragment);
        }

        if (envMapTextures != null && envMapTextures.size() > 0) {
            EnvironmentMapFragmentShaderFragment fragment = new EnvironmentMapFragmentShaderFragment(envMapTextures);
            mFragmentShader.addShaderFragment(fragment);
        }

        if (skyTextures != null && skyTextures.size() > 0) {
            SkyTextureFragmentShaderFragment fragment = new SkyTextureFragmentShaderFragment(skyTextures);
            mFragmentShader.addShaderFragment(fragment);
        }

        if (hasVideoTexture)
            mFragmentShader.addPreprocessorDirective("#extension GL_OES_EGL_image_external : require");

        checkForPlugins(PluginInsertLocation.PRE_LIGHTING);

        //
        // -- Lighting
        //

        if (mLightingEnabled && mLights != null && mLights.size() > 0) {
            mVertexShader.setLights(mLights);
            mFragmentShader.setLights(mLights);

            mLightsVertexShaderFragment = new LightsVertexShaderFragment(mLights);
            mLightsVertexShaderFragment.setAmbientColor(mAmbientColor);
            mLightsVertexShaderFragment.setAmbientIntensity(mAmbientIntensity);
            mVertexShader.addShaderFragment(mLightsVertexShaderFragment);
            mFragmentShader.addShaderFragment(new LightsFragmentShaderFragment(mLights));

            checkForPlugins(PluginInsertLocation.PRE_DIFFUSE);

            //
            // -- Diffuse method
            //

            if (mDiffuseMethod != null) {
                mDiffuseMethod.setLights(mLights);
                IShaderFragment fragment = mDiffuseMethod.getVertexShaderFragment();
                if (fragment != null)
                    mVertexShader.addShaderFragment(fragment);
                fragment = mDiffuseMethod.getFragmentShaderFragment();
                mFragmentShader.addShaderFragment(fragment);
            }

            checkForPlugins(PluginInsertLocation.PRE_SPECULAR);

            //
            // -- Specular method
            //

            if (mSpecularMethod != null) {
                mSpecularMethod.setLights(mLights);
                mSpecularMethod.setTextures(specMapTextures);
                IShaderFragment fragment = mSpecularMethod.getVertexShaderFragment();
                if (fragment != null)
                    mVertexShader.addShaderFragment(fragment);

                fragment = mSpecularMethod.getFragmentShaderFragment();
                if (fragment != null)
                    mFragmentShader.addShaderFragment(fragment);
            }
        }

        checkForPlugins(PluginInsertLocation.PRE_ALPHA);

        if (alphaMapTextures != null && alphaMapTextures.size() > 0) {
            AlphaMapFragmentShaderFragment fragment = new AlphaMapFragmentShaderFragment(alphaMapTextures);
            mFragmentShader.addShaderFragment(fragment);
        }

        if (lightMapTextures != null && lightMapTextures.size() > 0) {
            LightMapFragmentShaderFragment fragment = new LightMapFragmentShaderFragment(lightMapTextures);
            mFragmentShader.addShaderFragment(fragment);
        }

        checkForPlugins(PluginInsertLocation.PRE_TRANSFORM);
        checkForPlugins(PluginInsertLocation.POST_TRANSFORM);

        if (mVertexShader.needsBuild()) mVertexShader.buildShader();
        if (mFragmentShader.needsBuild()) mFragmentShader.buildShader();

        if (RajLog.isDebugEnabled()) {
            RajLog.d("-=-=-=- VERTEX SHADER -=-=-=-");
            RajLog.d(mVertexShader.getShaderString());
            RajLog.d("-=-=-=- FRAGMENT SHADER -=-=-=-");
            RajLog.d(mFragmentShader.getShaderString());
        }

        mProgramHandle = createProgram(mVertexShader.getShaderString(), mFragmentShader.getShaderString());
        if (mProgramHandle == 0) {
            mIsDirty = false;
            return;
        }

        mVertexShader.setLocations(mProgramHandle);
        mFragmentShader.setLocations(mProgramHandle);

        for (String name : mTextureHandles.keySet()) {
            setTextureHandleForName(name);
        }

        for (int i = 0; i < mTextureList.size(); i++) {
            setTextureParameters(mTextureList.get(i));
        }

        mIsDirty = false;
    }

    /**
     * Checks if the device capabilities need to be checked to update the count of available texture units.
     */
    private void checkCapabilitiesIfNeeded() {
        if (!mCapabilitiesCheckDeferred) return;
        mMaxTextures = Capabilities.getInstance().getMaxTextureImageUnits();
    }

    /**
     * Checks if any {@link IMaterialPlugin}s have been added. If so they will be added
     * to the vertex and/or fragment shader.
     *
     * @param location Where to insert the vertex and/or fragment shader
     */
    private void checkForPlugins(PluginInsertLocation location) {
        if (mPlugins == null) return;
        for (IMaterialPlugin plugin : mPlugins) {
            if (plugin.getInsertLocation() == location) {
                mVertexShader.addShaderFragment(plugin.getVertexShaderFragment());
                mFragmentShader.addShaderFragment(plugin.getFragmentShaderFragment());
            }
        }
    }

    /**
     * Loads the shader from a text string and then compiles it.
     *
     * @param shaderType
     * @param source
     *
     * @return
     */
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                RajLog.e("[" + getClass().getName() + "] Could not compile "
                    + (shaderType == GLES20.GL_FRAGMENT_SHADER ? "fragment" : "vertex") + " shader:");
                RajLog.e("Shader log: " + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * Creates a shader program by compiling the vertex and fragment shaders
     * from a string.
     *
     * @param vertexSource
     * @param fragmentSource
     *
     * @return
     */
    private int createProgram(String vertexSource, String fragmentSource) {
        mVShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (mVShaderHandle == 0) {
            return 0;
        }

        mFShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (mFShaderHandle == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, mVShaderHandle);
            GLES20.glAttachShader(program, mFShaderHandle);
            GLES20.glLinkProgram(program);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                RajLog.e("Could not link program in " + getClass().getCanonicalName() + ": ");
                RajLog.e(GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    /**
     * Tells the OpenGL context to use this program. This should be called every frame.
     */
    public void useProgram() {
        if (mIsDirty) {
            createShaders();
        }
        GLES20.glUseProgram(mProgramHandle);
    }

    /**
     * Applies parameters that should be set on the shaders. These are parameters
     * like time, color, buffer handles, etc.
     */
    public void applyParams() {
        mVertexShader.setColor(mColor);
        mVertexShader.setTime(mTime);
        mVertexShader.applyParams();

        mFragmentShader.setColorInfluence(mColorInfluence);
        mFragmentShader.applyParams();
    }

    /**
     * Sets the OpenGL texture handles for a newly added texture.
     *
     * @param texture
     */
    private void setTextureParameters(ATexture texture) {
        if (mTextureHandles.containsKey(texture.getTextureName())) return;

        int textureHandle = GLES20.glGetUniformLocation(mProgramHandle, texture.getTextureName());
        if (textureHandle == -1 && RajLog.isDebugEnabled()) {
            RajLog.e("Could not get uniform location for " + texture.getTextureName() + ", "
                     + texture.getTextureType());
            return;
        }
        mTextureHandles.put(texture.getTextureName(), textureHandle);
    }

    public void setTextureHandleForName(@NonNull String name) {
        if (mProgramHandle < 0 || mTextureHandles.containsKey(name) && mTextureHandles.get(name) > -1) {
            return;
        }
        int textureHandle = GLES20.glGetUniformLocation(mProgramHandle, name);
        if (textureHandle == -1 && RajLog.isDebugEnabled()) {
            RajLog.e("Could not get uniform location for " + name + " Program Handle: " + mProgramHandle);
            return;
        }
        mTextureHandles.put(name, textureHandle);
    }

    /**
     * Binds the textures to an OpenGL texturing target. Called every frame by
     * {@link Scene#render(long, double, org.rajawali3d.renderer.RenderTarget)}. Shouldn't
     * be called manually.
     */
    public void bindTextures() {
        // Assume its the number of textures
        int num = mTextureList.size();
        // Check if the number of applied textures is larger than the max texture count
        // - this would be due to deferred capabilities checking. If so, choose max texture count.
        if (num > mMaxTextures) {
            RajLog.e(num + " textures have been added to this material but this device supports a max of "
                + mMaxTextures + " textures in the fragment shader. Only the first " + mMaxTextures + " will be used.");
            num = mMaxTextures;
        }

        for (int i = 0; i < num; i++) {
            bindTextureByName(i, mTextureList.get(i));
        }

        if (mPlugins != null)
            for (IMaterialPlugin plugin : mPlugins)
                plugin.bindTextures(num);
    }

    public void bindTextureByName(int index, ATexture texture) {
        if (!mTextureHandles.containsKey(texture.getTextureName())) {
            setTextureParameters(texture);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
        GLES20.glBindTexture(texture.getGLTextureType(), texture.getTextureId());
        GLES20.glUniform1i(mTextureHandles.get(texture.getTextureName()), index);
    }

    public void bindTextureByName(String name, int index, ATexture texture) {
        if (!mTextureHandles.containsKey(texture.getTextureName())) {
            setTextureHandleForName(name);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
        GLES20.glBindTexture(texture.getGLTextureType(), texture.getTextureId());
        GLES20.glUniform1i(mTextureHandles.get(name), index);
    }

    /**
     * Unbinds the texture from an OpenGL texturing target.
     */
    public void unbindTextures() {
        int num = mTextureList.size();

        if (mPlugins != null)
            for (IMaterialPlugin plugin : mPlugins)
                plugin.unbindTextures();

        for (int i = 0; i < num; i++) {
            ATexture texture = mTextureList.get(i);
            GLES20.glBindTexture(texture.getGLTextureType(), 0);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Adds a texture to this material. Throws and error if the maximum number of textures was reached.
     *
     * @param texture
     *
     * @throws TextureException
     */
    public void addTexture(ATexture texture) throws TextureException {
        if (mTextureList.indexOf(texture) > -1) return;
        if (mTextureList.size() + 1 > mMaxTextures) {
            throw new TextureException("Maximum number of textures for this material has been reached. Maximum number of textures is " + mMaxTextures + ".");
        }
        mTextureList.add(texture);

        TextureManager.getInstance().addTexture(texture);
        texture.registerMaterial(this);

        mIsDirty = true;
    }

    /**
     * Removes a texture from this material.
     *
     * @param texture
     */
    public void removeTexture(ATexture texture) {
        mTextureList.remove(texture);
        texture.unregisterMaterial(this);
    }

    /**
     * Gets a list of textures bound to this material.
     *
     * @return
     */
    public ArrayList<ATexture> getTextureList() {
        return mTextureList;
    }

    /**
     * Copies this material's textures to another material.
     *
     * @param material
     *
     * @throws TextureException
     */
    public void copyTexturesTo(Material material) throws TextureException {
        int num = mTextureList.size();

        for (int i = 0; i < num; ++i)
            material.addTexture(mTextureList.get(i));
    }

    /**
     * Set the vertex buffer handle. This is passed to {@link VertexShader#setVertices(int)}
     *
     * @param vertexBufferHandle
     */
    public void setVertices(final int vertexBufferHandle) {
        mVertexShader.setVertices(vertexBufferHandle);
    }

    /**
     * Set the vertex buffer handle. This is passed to {@link VertexShader#setVertices(int)}
     *
     * @param bufferInfo
     */
    public void setVertices(BufferInfo bufferInfo) {
        mVertexShader.setVertices(bufferInfo.bufferHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Set the texture coordinates buffer handle. This is passed to {@link VertexShader#setTextureCoords(int)}
     *
     * @param textureCoordBufferHandle
     */
    public void setTextureCoords(final int textureCoordBufferHandle) {
        mVertexShader.setTextureCoords(textureCoordBufferHandle);
    }

    /**
     * Set the texture coordinates buffer handle. This is passed to {@link VertexShader#setTextureCoords(int)}
     *
     * @param bufferInfo
     */
    public void setTextureCoords(BufferInfo bufferInfo) {
        mVertexShader.setTextureCoords(bufferInfo.bufferHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Set the normal buffer handle. This is passed to {@link VertexShader#setNormals(int)}
     *
     * @param normalBufferHandle
     */
    public void setNormals(final int normalBufferHandle) {
        mVertexShader.setNormals(normalBufferHandle);
    }

    /**
     * Set the normal buffer handle. This is passed to {@link VertexShader#setNormals(int)}
     *
     * @param bufferInfo
     */
    public void setNormals(BufferInfo bufferInfo) {
        mVertexShader.setNormals(bufferInfo.bufferHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Set the vertex color buffer handle. This is passed to {@link VertexShader#setVertexColors(int)}
     *
     * @param vertexColorBufferHandle
     */
    public void setVertexColors(final int vertexColorBufferHandle) {
        mVertexShader.setVertexColors(vertexColorBufferHandle);
    }

    /**
     * Set the vertex color buffer handle. This is passed to {@link VertexShader#setVertexColors(int)}
     *
     * @param bufferInfo
     */
    public void setVertexColors(BufferInfo bufferInfo) {
        mVertexShader.setVertexColors(bufferInfo.bufferHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Returns the inverse view matrix. The inverse view matrix is used to transform reflections.
     *
     * @return
     */
    public float[] getInverseViewMatrix() {
        return mInverseViewMatrix;
    }

    /**
     * Returns the model view matrix. The model view matrix is used to transform vertices to eye coordinates.
     *
     * @return
     */
    public float[] getModelViewMatrix() {
        return mModelViewMatrix;
    }

    /**
     * Sets the model view projection matrix. The model view projection matrix is used to transform vertices
     * to screen coordinates.
     *
     * @param mvpMatrix
     */
    public void setMVPMatrix(Matrix4 mvpMatrix) {
        mVertexShader.setMVPMatrix(mvpMatrix.getFloatValues());
    }

    /**
     * Sets the model matrix. The model matrix holds the object's local coordinates.
     *
     * @param modelMatrix
     */
    public void setModelMatrix(Matrix4 modelMatrix) {
        mModelMatrix = modelMatrix;//.getFloatValues();
        mVertexShader.setModelMatrix(mModelMatrix);

        mNormalMatrix.setAll(modelMatrix);
        try {
            mNormalMatrix.setToNormalMatrix();
        } catch (IllegalStateException exception) {
            RajLog.d("modelMatrix is degenerate (zero scale)...");
        }
        float[] matrix = mNormalMatrix.getFloatValues();

        mNormalFloats[0] = matrix[0];
        mNormalFloats[1] = matrix[1];
        mNormalFloats[2] = matrix[2];
        mNormalFloats[3] = matrix[4];
        mNormalFloats[4] = matrix[5];
        mNormalFloats[5] = matrix[6];
        mNormalFloats[6] = matrix[8];
        mNormalFloats[7] = matrix[9];
        mNormalFloats[8] = matrix[10];

        mVertexShader.setNormalMatrix(mNormalFloats);
    }

    /**
     * Sets the inverse view matrix. The inverse view matrix is used to transform reflections
     *
     * @param inverseViewMatrix
     */
    public void setInverseViewMatrix(Matrix4 inverseViewMatrix) {
        mInverseViewMatrix = inverseViewMatrix.getFloatValues();
        mVertexShader.setInverseViewMatrix(mInverseViewMatrix);
    }

    /**
     * Sets the model view matrix. The model view matrix is used to transform vertices to eye coordinates
     *
     * @param modelViewMatrix
     */
    public void setModelViewMatrix(Matrix4 modelViewMatrix) {
        mModelViewMatrix = modelViewMatrix.getFloatValues();
        mVertexShader.setModelViewMatrix(mModelViewMatrix);
    }

    /**
     * Indicates whether lighting should be used or not. This must be set to true when using a
     * {@link DiffuseMethod} or a {@link SpecularMethod}. Lights are added to a scene {@link Scene}
     * and are automatically added to the material.
     *
     * @param value
     */
    public void enableLighting(boolean value) {
        mLightingEnabled = value;
    }

    /**
     * Indicates whether lighting should be used or not. This must be set to true when using a
     * {@link DiffuseMethod} or a {@link SpecularMethod}. Lights are added to a scene {@link Scene}
     * and are automatically added to the material.
     *
     * @return
     */
    public boolean lightingEnabled() {
        return mLightingEnabled;
    }

    /**
     * Indicates that the time shader parameter should be used. This is used when creating shaders
     * that should change during the course of time. This is used to accomplish effects like animated
     * vertices, vertex colors, plasma effects, etc. The time needs to be manually updated using the
     * {@link Material#setTime(float)} method.
     *
     * @param value
     */
    public void enableTime(boolean value) {
        mTimeEnabled = value;
    }

    /**
     * Indicates that the time shader parameter should be used. This is used when creating shaders
     * that should change during the course of time. This is used to accomplish effects like animated
     * vertices, vertex colors, plasma effects, etc. The time needs to be manually updated using the
     * {@link Material#setTime(float)} method.
     *
     * @return
     */
    public boolean timeEnabled() {
        return mTimeEnabled;
    }

    /**
     * Sets the time value that is used in the shaders to create animated effects.
     * <p/>
     * <pre><code>
     * public class MyRenderer extends Renderer
     * {
     * 		private double mStartTime;
     * 		private Material mMyMaterial;
     * <p/>
     * 		protected void initScene() {
     * 			mStartTime = SystemClock.elapsedRealtime();
     * 			...
     *        }
     * <p/>
     * 		public void onDrawFrame(GL10 glUnused) {
     * 			super.onDrawFrame(glUnused);
     * 			mMyMaterial.setTime((SystemClock.elapsedRealtime() - mLastRender) / 1000d);
     * 			...
     *        }
     * <p/>
     * </code></pre>
     *
     * @param time
     */
    public void setTime(float time) {
        mTime = time;
    }

    /**
     * Sets the time value that is used in the shaders to create animated effects.
     * <p/>
     * <pre><code>
     * public class MyRenderer extends Renderer
     * {
     * 		private double mStartTime;
     * 		private Material mMyMaterial;
     * <p/>
     * 		protected void initScene() {
     * 			mStartTime = SystemClock.elapsedRealtime();
     * 			...
     *        }
     * <p/>
     * 		public void onDrawFrame(GL10 glUnused) {
     * 			super.onDrawFrame(glUnused);
     * 			mMyMaterial.setTime((SystemClock.elapsedRealtime() - mLastRender) / 1000d);
     * 			...
     *        }
     * <p/>
     * </code></pre>
     *
     * @return
     */
    public float getTime() {
        return mTime;
    }

    /**
     * The lights that affect the material. Lights shouldn't be managed by any other class
     * than {@link Scene}. To add lights to a scene call {@link Scene#addLight(ALight).
     *
     * @param lights The lights collection
     */
    public void setLights(List<ALight> lights) {
        if (mLights != null) {
            for (ALight light : lights) {
                if (!mLights.contains(light)) {
                    break;
                }
            }
        } else {
            mIsDirty = true;
            mLights = lights;
        }
    }

    /**
     * The diffuse method specifies the reflection of light from a surface such that an incident
     * ray is reflected at many angles rather than at just one angle as in the case of specular reflection.
     * This can be set using the setDiffuseMethod() method:
     * <pre><code>
     * material.setDiffuseMethod(new DiffuseMethod.Lambert());
     * </code></pre>
     *
     * @param diffuseMethod The diffuse method
     */
    public void setDiffuseMethod(IDiffuseMethod diffuseMethod) {
        if (mDiffuseMethod == diffuseMethod) return;
        mDiffuseMethod = diffuseMethod;
        mIsDirty = true;
    }

    /**
     * The diffuse method specifies the reflection of light from a surface such that an incident
     * ray is reflected at many angles rather than at just one angle as in the case of specular reflection.
     * This can be set using the setDiffuseMethod() method:
     * <pre><code>
     * material.setDiffuseMethod(new DiffuseMethod.Lambert());
     * </code></pre>
     *
     * @return the currently used diffuse method
     */
    public IDiffuseMethod getDiffuseMethod() {
        return mDiffuseMethod;
    }

    /**
     * The specular method specifies the mirror-like reflection of light (or of other kinds of wave)
     * from a surface, in which light from a single incoming direction (a ray) is reflected into a
     * single outgoing direction.
     * This can be set using the setSpecularMethod() method:
     * <pre><code>
     * material.setSpecularMethod(new SpecularMethod.Phong());
     * </code></pre>
     *
     * @param specularMethod The specular method to use
     */
    public void setSpecularMethod(ISpecularMethod specularMethod) {
        if (mSpecularMethod == specularMethod) return;
        mSpecularMethod = specularMethod;
        mIsDirty = true;
    }

    /**
     * The specular method specifies the mirror-like reflection of light (or of other kinds of wave)
     * from a surface, in which light from a single incoming direction (a ray) is reflected into a
     * single outgoing direction.
     * This can be set using the setSpecularMethod() method:
     * <pre><code>
     * material.setSpecularMethod(new SpecularMethod.Phong());
     * </code></pre>
     *
     * @return The currently used specular method
     */
    public ISpecularMethod getSpecularMethod() {
        return mSpecularMethod;
    }

    /**
     * Add a material plugin. A material plugin is basically
     * a class that contains a vertex shader fragment and a fragment shader fragment. Material
     * plugins can be used for custom shader effects.
     *
     * @param plugin
     */
    public void addPlugin(IMaterialPlugin plugin) {
        if (mPlugins == null) {
            mPlugins = new ArrayList<IMaterialPlugin>();
        } else {
            for (IMaterialPlugin p : mPlugins) {
                if (plugin.getClass().getSimpleName().equals(p.getClass().getSimpleName()))
                    return;
            }
        }

        mPlugins.add(plugin);
        mIsDirty = true;
    }

    /**
     * Get a material plugin by using its class type. A material plugin is basically
     * a class that contains a vertex shader fragment and a fragment shader fragment. Material
     * plugins can be used for custom shader effects.
     *
     * @param pluginClass
     *
     * @return
     */
    public IMaterialPlugin getPlugin(Class<?> pluginClass) {
        if (mPlugins == null) return null;

        for (IMaterialPlugin plugin : mPlugins) {
            if (plugin.getClass() == pluginClass)
                return plugin;
        }

        return null;
    }

    public void setCurrentObject(Object3D currentObject) {
    }

    public void unsetCurrentObject(Object3D currentObject) {
    }

    /**
     * Remove a material plugin. A material plugin is basically
     * a class that contains a vertex shader fragment and a fragment shader fragment. Material
     * plugins can be used for custom shader effects.
     *
     * @param plugin
     */
    public void removePlugin(IMaterialPlugin plugin) {
        if (mPlugins != null && mPlugins.contains(plugin)) {
            mPlugins.remove(plugin);
            mIsDirty = true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param identity
     */
    public void setOwnerIdentity(String identity) {
        mOwnerIdentity = identity;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public String getOwnerIdentity() {
        return mOwnerIdentity;
    }
}
