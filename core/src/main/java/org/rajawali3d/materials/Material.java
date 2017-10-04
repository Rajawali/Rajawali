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

import static c.org.rajawali3d.textures.annotation.Type.ALPHA_MASK;
import static c.org.rajawali3d.textures.annotation.Type.CUBE_MAP;
import static c.org.rajawali3d.textures.annotation.Type.DIFFUSE;
import static c.org.rajawali3d.textures.annotation.Type.NORMAL;
import static c.org.rajawali3d.textures.annotation.Type.RENDER_TARGET;
import static c.org.rajawali3d.textures.annotation.Type.SPECULAR;
import static c.org.rajawali3d.textures.annotation.Type.SPHERE_MAP;
import static c.org.rajawali3d.textures.annotation.Type.VIDEO_TEXTURE;

import android.graphics.Color;
import android.opengl.GLES20;
import android.support.annotation.NonNull;

import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.textures.BaseTexture;
import c.org.rajawali3d.textures.CubeMapTexture;
import c.org.rajawali3d.textures.SphereMapTexture2D;
import c.org.rajawali3d.textures.TextureException;
import org.rajawali3d.Object3D;
import c.org.rajawali3d.gl.buffers.BufferInfo;
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
import org.rajawali3d.materials.shaders.fragments.texture.AlphaMaskFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.DiffuseTextureFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.EnvironmentMapFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.NormalMapFragmentShaderFragment;
import org.rajawali3d.materials.shaders.fragments.texture.SkyTextureFragmentShaderFragment;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.util.RajLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Material class is where you define the visual characteristics of your 3D model. Here you can specify lighting
 * parameters, specular highlights, ambient colors and much more. This is the place where you add textures as well. For
 * an overview of the different types of materials and parameters visit the Rajawali Wiki.
 *
 * This is a basic example using lighting, a texture, Lambertian diffuse model and Phong specular highlights:
 * <pre><code>
 * Material material = new Material();
 * material.addTexture(new Texture2D("earth", R.drawable.earth_diffuse));
 * material.enableLighting(true);
 * material.setDiffuseMethod(new DiffuseMethod.Lambert());
 * material.setSpecularMethod(new SpecularMethod.Phong());
 *
 * myObject.setMaterial(material);
 * </code></pre>
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://github.com/MasDennis/Rajawali/wiki/Materials">https://github.com/Rajawali/Rajawali/wiki/Materials</a>
 */
public class Material {

    /**
     * This tells the Material class where to insert a shader fragment into either the vertex of fragment shader.
     */
    public enum PluginInsertLocation {
        PRE_LIGHTING, PRE_DIFFUSE, PRE_SPECULAR, PRE_ALPHA, PRE_TRANSFORM, POST_TRANSFORM, IGNORE
    }

    private final boolean capabilitiesCheckDeferred;

    /**
     * The generic vertex shader. This can be extended by using vertex shader fragments. A vertex shader is typically
     * used to modify vertex positions, vertex colors and normals.
     */
    private VertexShader               vertexShader;

    /**
     * The generic fragment shader. This can be extended by using fragment shader fragments. A fragment shader is
     * typically used to modify rasterized pixel colors.
     */
    private FragmentShader             fragmentShader;

    /**
     * The shader fragments that are plugged into both the vertex and fragment shader. This is where lighting
     * calculations are performed.
     */
    private LightsVertexShaderFragment lightsVertexShaderFragment;

    /**
     * The diffuse method specifies the reflection of light from a surface such that an incident ray is reflected at
     * many angles rather than at just one angle as in the case of specular reflection.
     * This can be set using the setDiffuseMethod() method:
     * <pre><code>
     * material.setDiffuseMethod(new DiffuseMethod.Lambert());
     * </code></pre>
     */
    private IDiffuseMethod  diffuseMethod;

    /**
     * The specular method specifies the mirror-like reflection of light (or of other kinds of wave) from a surface, in
     * which light from a single incoming direction (a ray) is reflected into a single outgoing direction.
     * This can be set using the setSpecularMethod() method:
     * <pre><code>
     * material.setSpecularMethod(new SpecularMethod.Phong());
     * </code></pre>
     */
    private ISpecularMethod specularMethod;

    /**
     * Indicates that this material should use a color value for every vertex. These colors are contained in a separate
     * color buffer.
     */
    private boolean         useVertexColors;

    /**
     * Indicates whether lighting should be used or not. This must be set to true when using a {@link IDiffuseMethod} or
     * a {@link SpecularMethod}. Lights are added to a scene {@link Scene} and are automatically added to the material.
     */
    private boolean lightingEnabled;

    /**
     * Indicates that the time shader parameter should be used. This is used when creating shaders that should change
     * during the course of time. This is used to accomplish effects like animated vertices, vertex colors, plasma
     * effects, etc. The time needs to be manually updated using the {@link Material#setTime(float)} method.
     */
    private boolean timeEnabled;

    /**
     * Indicates that one of the material properties was changed and that the shader program should be re-compiled.
     */
    private boolean isDirty       = true;

    /**
     * Holds a reference to the shader program
     */
    private int     programHandle = -1;

    /**
     * Holds a reference to the vertex shader
     */
    private int     vertexShaderHandle;

    /**
     * Holds a reference to the fragment shader
     */
    private int     fragmentShaderHandle;

    /**
     * The model matrix holds the object's local coordinates
     */
    private Matrix4 modelMatrix;

    /**
     * The inverse view matrix is used to transform reflections
     */
    private float[] inverseViewMatrix;
    /**
     * The model view matrix is used to transform vertices to eye coordinates
     */
    private float[] modelViewMatrix;

    /**
     * The material's diffuse color. This can be overwritten by {@link Object3D#setColor(int)}. This color will be
     * applied to the whole object. For vertex colors use {@link Material#useVertexColors(boolean)} and
     * {@link Material#setVertexColors(int)}.
     */
    private float[] color;

    /**
     * This material's ambient color. Ambient color is the color of an object where it is in shadow.
     */
    private float[] ambientColor;

    /**
     * This material's ambient intensity for the r, g, b channels.
     */
    private float[] ambientIntensity;

    /**
     * The color influence indicates how big the influence of the color is. This should be used in conjunction with
     * {@link BaseTexture#setInfluence(float)}. A value of .5 indicates an influence of 50%. This examples shows how to
     * use 50% color and 50% texture:
     *
     * <pre><code>
     * material.setColorInfluence(.5f);
     * myTexture.setInfluence(.5f);
     * </code></pre>
     */
    private float colorInfluence = 1;

    /**
     * Sets the time value that is used in the shaders to create animated effects.
     *
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
    private float                   time;

    /**
     * The lights that affect the material. Lights shouldn't be managed by any other class than {@link Scene}. To add
     * lights to a scene call {@link Scene#addLight(ALight).
     */
    protected List<ALight>          lights;

    /**
     * A list of material plugins that are used by this material. A material plugin is basically a class that contains a
     * vertex shader fragment and a fragment shader fragment. Material plugins can be used for custom shader effects.
     */
    protected List<IMaterialPlugin> plugins;

    /**
     * This texture's unique owner identity String. This is usually the fully qualified name of the {@link Renderer}
     * instance.
     */
    protected String              ownerIdentity;

    /**
     * The maximum number of available textures for this device. This value is returned from
     * {@link Capabilities#getMaxTextureImageUnits()}.
     */
    private int                   maxTextures;

    /**
     * The list of textures that are assigned by this materials.
     */
    protected ArrayList<BaseTexture> textures;

    protected Map<String, Integer> textureHandles;

    /**
     * Contains the normal matrix. The normal matrix is used in the shaders to transform the normal into eye space.
     */
    protected final float[] normalFloats = new float[9];

    /**
     * Scratch normal matrix. The normal matrix is used in the shaders to transform the normal into eye space.
     */
    protected Matrix4       normalMatrix = new Matrix4();

    protected VertexShader   customVertexShader;
    protected FragmentShader customFragmentShader;

    /**
     * The Material class is where you define the visual characteristics of your 3D model. Here you can specify lighting
     * parameters, specular highlights, ambient colors and much more. This is the place where you add textures as well.
     * For an overview of the different types of materials and parameters visit the Rajawali Wiki.
     *
     * This is a basic example using lighting, a texture, Lambertian diffuse model and Phong specular highlights:
     * <pre><code>
     * Material material = new Material();
     * material.addTexture(new Texture2D("earth", R.drawable.earth_diffuse));
     * material.enableLighting(true);
     * material.setDiffuseMethod(new DiffuseMethod.Lambert());
     * material.setSpecularMethod(new SpecularMethod.Phong());
     * <p/>
     * myObject.setMaterial(material);
     * </code></pre>
     *
     * @see <a href="https://github.com/MasDennis/Rajawali/wiki/Materials">
     *     https://github.com/Rajawali/Rajawali/wiki/Materials</a>
     */
    public Material() {
        this(false);
    }

    public Material(boolean deferCapabilitiesCheck) {
        capabilitiesCheckDeferred = deferCapabilitiesCheck;
        textures = new ArrayList<>();
        textureHandles = new HashMap<>();

        // If we have deffered the capabilities check, we have no way of knowing how many textures this material
        // is capable of having. We could choose 8, the minimum required fragment shader texture unit count, but
        // that would not allow us to finish construction of this material until the EGL context is available. Instead,
        // we are choosing the maximum integer Java can handle, and we will print a warning if the number of added textures
        // exceeds the capability once known. In this event they will be used in listed order until the max is hit.
        maxTextures = capabilitiesCheckDeferred ? Integer.MAX_VALUE : Capabilities.getInstance().getMaxTextureImageUnits();

        color = new float[]{ 1, 0, 0, 1};
        ambientColor = new float[]{ .2f, .2f, .2f};
        ambientIntensity = new float[]{ .3f, .3f, .3f};
    }

    public Material(VertexShader customVertexShader, FragmentShader customFragmentShader) {
        this(customVertexShader, customFragmentShader, false);
    }

    public Material(VertexShader customVertexShader, FragmentShader customFragmentShader, boolean deferCapabilitiesCheck) {
        this(deferCapabilitiesCheck);
        this.customVertexShader = customVertexShader;
        this.customFragmentShader = customFragmentShader;
    }

    /**
     * Indicates that this material should use a color value for every vertex. These colors are contained in a separate
     * color buffer.
     *
     * @return A boolean indicating that vertex colors will be used.
     */
    public boolean usingVertexColors() {
        return useVertexColors;
    }

    /**
     * Indicates that this material should use a color value for every vertex. These colors are contained in a separate
     * color buffer.
     *
     * @param value A boolean indicating whether vertex colors should be used or not
     */
    public void useVertexColors(boolean value) {
        if (value != useVertexColors) {
            isDirty = true;
            useVertexColors = value;
        }
    }

    /**
     * The material's diffuse color. This can be overwritten by {@link Object3D#setColor(int)}. This color will be
     * applied to the whole object. For vertex colors use {@link Material#useVertexColors(boolean)} and
     * {@link Material#setVertexColors(int)}.
     *
     * @param color {@code int} color The color to be used. Color.RED for instance. Or 0xffff0000.
     */
    public void setColor(int color) {
        this.color[0] = (float) Color.red(color) / 255.f;
        this.color[1] = (float) Color.green(color) / 255.f;
        this.color[2] = (float) Color.blue(color) / 255.f;
        this.color[3] = (float) Color.alpha(color) / 255.f;
        if (vertexShader != null)
            vertexShader.setColor(this.color);
    }

    /**
     * The material's diffuse color. This can be overwritten by {@link Object3D#setColor(int)}. This color will be
     * applied to the whole object. For vertex colors use {@link Material#useVertexColors(boolean)} and
     * {@link Material#setVertexColors(int)}.
     *
     * @param color A float array containing the colors to be used. These are normalized values containing values for
     *              the red, green, blue and alpha channels.
     */
    public void setColor(float[] color) {
        this.color[0] = color[0];
        this.color[1] = color[1];
        this.color[2] = color[2];
        this.color[3] = color[3];
        if (vertexShader != null)
            vertexShader.setColor(this.color);
    }

    /**
     * Returns this material's diffuse color.
     *
     * @return
     */
    public int getColor() {
        return Color.argb((int) (color[3] * 255), (int) (color[0] * 255), (int) (color[1] * 255), (int) (color[2] * 255));
    }

    /**
     * The color influence indicates how big the influence of the color is. This should be used in conjunction with
     * {@link BaseTexture#setInfluence(float)}. A value of .5 indicates an influence of 50%. This examples shows how to
     * use 50% color and 50% texture:
     *
     * <pre><code>
     * material.setColorInfluence(.5f);
     * myTexture.setInfluence(.5f);
     * </code></pre>
     *
     * @param influence A value in the range of [0..1] indicating the color influence. Use .5 for 50% color influence,
     *                  .75 for 75% color influence, etc.
     */
    public void setColorInfluence(float influence) {
        colorInfluence = influence;
    }

    /**
     * Indicates the color influence. Use .5 for 50% color influence, .75 for 75% color influence, etc.
     *
     * @return A value in the range of [0..1]
     */
    public float getColorInfluence() {
        return colorInfluence;
    }

    /**
     * This material's ambient color. Ambient color is the color of an object where it is in shadow.
     *
     * @param color The color to be used. Color.RED for instance. Or 0xffff0000.
     */
    public void setAmbientColor(int color) {
        ambientColor[0] = (float) Color.red(color) / 255.f;
        ambientColor[1] = (float) Color.green(color) / 255.f;
        ambientColor[2] = (float) Color.blue(color) / 255.f;
        if (lightsVertexShaderFragment != null)
            lightsVertexShaderFragment.setAmbientColor(ambientColor);
    }

    /**
     * This material's ambient color. Ambient color is the color of an object where it is in shadow.
     *
     * @param color A float array containing the colors to be used. These are normalized values containing values for
     *              the red, green, blue and alpha channels.
     */
    public void setAmbientColor(float[] color) {
        ambientColor[0] = color[0];
        ambientColor[1] = color[1];
        ambientColor[2] = color[2];
        if (lightsVertexShaderFragment != null)
            lightsVertexShaderFragment.setAmbientColor(ambientColor);
    }

    /**
     * Returns this material's ambient color. Ambient color is the color of an object where it is in shadow.
     *
     * @return
     */
    public int getAmbientColor() {
        return Color.argb(1, (int) (ambientColor[0] * 255), (int) (ambientColor[1] * 255), (int) (ambientColor[2] * 255));
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
        ambientIntensity[0] = r;
        ambientIntensity[1] = g;
        ambientIntensity[2] = b;
        if (lightsVertexShaderFragment != null)
            lightsVertexShaderFragment.setAmbientIntensity(ambientIntensity);
    }

    //TODO: Remove visibility!
    public void add() {
        RajLog.d("Material is being added.");
        // We are being added to the scene, check the capabilities now if needed since they are available.
        checkCapabilitiesIfNeeded();

        if (lightingEnabled && lights == null)
            return;

        createShaders();
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        modelMatrix = null;
        inverseViewMatrix = null;
        modelViewMatrix = null;

        if (lights != null)
            lights.clear();
        if (textures != null)
            textures.clear();

        if (Renderer.hasGLContext()) {
            GLES20.glDeleteShader(vertexShaderHandle);
            GLES20.glDeleteShader(fragmentShaderHandle);
            GLES20.glDeleteProgram(programHandle);
        }
    }

    void reload() {
        isDirty = true;
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
        if (!isDirty)
            return;
        if (customVertexShader == null && customFragmentShader == null) {
            //
            // -- Check textures
            //

            List<BaseTexture> diffuseTextures = null;
            List<BaseTexture> normalMapTextures = null;
            List<BaseTexture> envMapTextures = null;
            List<BaseTexture> skyTextures = null;
            List<BaseTexture> specMapTextures = null;
            List<BaseTexture> alphaMapTextures = null;

            boolean hasCubeMaps = false;
            boolean hasVideoTexture = false;

            for (int i = 0; i < textures.size(); i++) {
                BaseTexture texture  = textures.get(i);

                switch (texture.getTextureType()) {
                    case VIDEO_TEXTURE:
                        hasVideoTexture = true;
                        // no break statement, add the video texture to the diffuse textures
                    case DIFFUSE:
                    case RENDER_TARGET:
                        if (diffuseTextures == null) diffuseTextures = new ArrayList<>();
                        diffuseTextures.add(texture);
                        break;
                    case NORMAL:
                        if (normalMapTextures == null) normalMapTextures = new ArrayList<>();
                        normalMapTextures.add(texture);
                        break;
                    case CUBE_MAP:
                        hasCubeMaps = true;
                    case SPHERE_MAP:
                        boolean isSkyTexture = false;

                        if (texture.getClass() == SphereMapTexture2D.class) {
                            isSkyTexture = ((SphereMapTexture2D) texture).isSkyTexture();
                        } else if (texture.getClass() == CubeMapTexture.class) {
                            isSkyTexture = ((CubeMapTexture) texture).isSkyTexture();
                        }

                        if (isSkyTexture) {
                            if (skyTextures == null)
                                skyTextures = new ArrayList<>();
                            skyTextures.add(texture);
                        } else {
                            if (envMapTextures == null)
                                envMapTextures = new ArrayList<>();
                            envMapTextures.add(texture);
                        }
                        break;
                    case SPECULAR:
                        if (specMapTextures == null) specMapTextures = new ArrayList<>();
                        specMapTextures.add(texture);
                        break;
                    case ALPHA_MASK:
                        if (alphaMapTextures == null) alphaMapTextures = new ArrayList<>();
                        alphaMapTextures.add(texture);
                        break;
                    default:
                        break;
                }
            }

            vertexShader = new VertexShader();
            vertexShader.enableTime(timeEnabled);
            vertexShader.hasCubeMaps(hasCubeMaps);
            vertexShader.hasSkyTexture(skyTextures != null && skyTextures.size() > 0);
            vertexShader.useVertexColors(useVertexColors);
            onPreVertexShaderInitialize(vertexShader);
            vertexShader.initialize();
            fragmentShader = new FragmentShader();
            fragmentShader.enableTime(timeEnabled);
            fragmentShader.hasCubeMaps(hasCubeMaps);
            onPreFragmentShaderInitialize(fragmentShader);
            fragmentShader.initialize();

            if (diffuseTextures != null && diffuseTextures.size() > 0) {
                DiffuseTextureFragmentShaderFragment fragment = new DiffuseTextureFragmentShaderFragment(diffuseTextures);
                fragmentShader.addShaderFragment(fragment);
            }

            if (normalMapTextures != null && normalMapTextures.size() > 0) {
                NormalMapFragmentShaderFragment fragment = new NormalMapFragmentShaderFragment(normalMapTextures);
                fragmentShader.addShaderFragment(fragment);
            }

            if (envMapTextures != null && envMapTextures.size() > 0) {
                EnvironmentMapFragmentShaderFragment fragment = new EnvironmentMapFragmentShaderFragment(envMapTextures);
                fragmentShader.addShaderFragment(fragment);
            }

            if (skyTextures != null && skyTextures.size() > 0) {
                SkyTextureFragmentShaderFragment fragment = new SkyTextureFragmentShaderFragment(skyTextures);
                fragmentShader.addShaderFragment(fragment);
            }

            if (hasVideoTexture)
                fragmentShader.addPreprocessorDirective("#extension GL_OES_EGL_image_external : require");

            checkForPlugins(PluginInsertLocation.PRE_LIGHTING);

            //
            // -- Lighting
            //

            if (lightingEnabled && lights != null && lights.size() > 0) {
                vertexShader.setLights(lights);
                fragmentShader.setLights(lights);

                lightsVertexShaderFragment = new LightsVertexShaderFragment(lights);
                lightsVertexShaderFragment.setAmbientColor(ambientColor);
                lightsVertexShaderFragment.setAmbientIntensity(ambientIntensity);
                vertexShader.addShaderFragment(lightsVertexShaderFragment);
                fragmentShader.addShaderFragment(new LightsFragmentShaderFragment(lights));

                checkForPlugins(PluginInsertLocation.PRE_DIFFUSE);

                //
                // -- Diffuse method
                //

                if (diffuseMethod != null) {
                    diffuseMethod.setLights(lights);
                    IShaderFragment fragment = diffuseMethod.getVertexShaderFragment();
                    if (fragment != null)
                        vertexShader.addShaderFragment(fragment);
                    fragment = diffuseMethod.getFragmentShaderFragment();
                    fragmentShader.addShaderFragment(fragment);
                }

                checkForPlugins(PluginInsertLocation.PRE_SPECULAR);

                //
                // -- Specular method
                //

                if (specularMethod != null) {
                    specularMethod.setLights(lights);
                    specularMethod.setTextures(specMapTextures);
                    IShaderFragment fragment = specularMethod.getVertexShaderFragment();
                    if (fragment != null)
                        vertexShader.addShaderFragment(fragment);

                    fragment = specularMethod.getFragmentShaderFragment();
                    if (fragment != null)
                        fragmentShader.addShaderFragment(fragment);
                }
            }

            checkForPlugins(PluginInsertLocation.PRE_ALPHA);

            if (alphaMapTextures != null && alphaMapTextures.size() > 0) {
                AlphaMaskFragmentShaderFragment fragment = new AlphaMaskFragmentShaderFragment(alphaMapTextures);
                fragmentShader.addShaderFragment(fragment);
            }

            checkForPlugins(PluginInsertLocation.PRE_TRANSFORM);
            checkForPlugins(PluginInsertLocation.POST_TRANSFORM);

            vertexShader.buildShader();
            fragmentShader.buildShader();
        } else {
            vertexShader = customVertexShader;
            fragmentShader = customFragmentShader;

            if (vertexShader.needsBuild()) vertexShader.initialize();
            if (fragmentShader.needsBuild()) fragmentShader.initialize();

            if (vertexShader.needsBuild()) vertexShader.buildShader();
            if (fragmentShader.needsBuild()) fragmentShader.buildShader();
        }

        if (RajLog.isDebugEnabled()) {
            RajLog.d("-=-=-=- VERTEX SHADER -=-=-=-");
            RajLog.d(vertexShader.getShaderString());
            RajLog.d("-=-=-=- FRAGMENT SHADER -=-=-=-");
            RajLog.d(fragmentShader.getShaderString());
        }

        programHandle = createProgram(vertexShader.getShaderString(), fragmentShader.getShaderString());
        if (programHandle == 0) {
            isDirty = false;
            return;
        }

        vertexShader.setLocations(programHandle);
        fragmentShader.setLocations(programHandle);

        for (String name : textureHandles.keySet()) {
            setTextureHandleForName(name);
        }

        for (int i = 0; i < textures.size(); i++) {
            setTextureParameters(textures.get(i));
        }

        isDirty = false;
    }

    /**
     * Checks if the device capabilities need to be checked to update the count of available texture units.
     */
    private void checkCapabilitiesIfNeeded() {
        if (!capabilitiesCheckDeferred) return;
        maxTextures = Capabilities.getInstance().getMaxTextureImageUnits();
    }

    /**
     * Checks if any {@link IMaterialPlugin}s have been added. If so they will be added to the vertex and/or fragment
     * shader.
     *
     * @param location Where to insert the vertex and/or fragment shader
     */
    private void checkForPlugins(PluginInsertLocation location) {
        if (plugins == null) return;
        for (IMaterialPlugin plugin : plugins) {
            if (plugin.getInsertLocation() == location) {
                vertexShader.addShaderFragment(plugin.getVertexShaderFragment());
                fragmentShader.addShaderFragment(plugin.getFragmentShaderFragment());
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
     * Creates a shader program by compiling the vertex and fragment shaders from a string.
     *
     * @param vertexSource
     * @param fragmentSource
     *
     * @return
     */
    private int createProgram(String vertexSource, String fragmentSource) {
        vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShaderHandle == 0) {
            return 0;
        }

        fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShaderHandle == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShaderHandle);
            GLES20.glAttachShader(program, fragmentShaderHandle);
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
        if (isDirty) {
            createShaders();
        }
        GLES20.glUseProgram(programHandle);
    }

    /**
     * Applies parameters that should be set on the shaders. These are parameters like time, color, buffer handles, etc.
     */
    public void applyParams() {
        vertexShader.setColor(color);
        vertexShader.setTime(time);
        vertexShader.applyParams();

        fragmentShader.setColorInfluence(colorInfluence);
        fragmentShader.applyParams();
    }

    /**
     * Sets the OpenGL texture handles for a newly added texture.
     *
     * @param texture
     */
    private void setTextureParameters(BaseTexture texture) {
        if (textureHandles.containsKey(texture.getTextureName())) return;

        int textureHandle = GLES20.glGetUniformLocation(programHandle, texture.getTextureName());
        if (textureHandle == -1 && RajLog.isDebugEnabled()) {
            RajLog.e("Could not get uniform location for " + texture.getTextureName() + ", "
                     + texture.getTextureType());
            return;
        }
        textureHandles.put(texture.getTextureName(), textureHandle);
    }

    public void setTextureHandleForName(@NonNull String name) {
        if (programHandle < 0 || textureHandles.containsKey(name) && textureHandles.get(name) > -1) {
            return;
        }
        int textureHandle = GLES20.glGetUniformLocation(programHandle, name);
        if (textureHandle == -1 && RajLog.isDebugEnabled()) {
            RajLog.e("Could not get uniform location for " + name + " Program Handle: " + programHandle);
            return;
        }
        textureHandles.put(name, textureHandle);
    }

    /**
     * Binds the textures to an OpenGL texturing target. Called every frame by
     * {@link Scene#render(long, double, org.rajawali3d.renderer.RenderTarget)}. Shouldn't be called manually.
     */
    public void bindTextures() {
        // Assume its the number of textures
        int num = textures.size();
        // Check if the number of applied textures is larger than the max texture count
        // - this would be due to deferred capabilities checking. If so, choose max texture count.
        if (num > maxTextures) {
            RajLog.e(num + " textures have been added to this material but this device supports a max of "
                     + maxTextures + " textures in the fragment shader. Only the first " + maxTextures
                     + " will be used.");
            num = maxTextures;
        }

        for (int i = 0; i < num; i++) {
            bindTextureByName(i, textures.get(i));
        }

        if (plugins != null)
            for (IMaterialPlugin plugin : plugins)
                plugin.bindTextures(num);
    }

    public void bindTextureByName(int index, BaseTexture texture) {
        if (!textureHandles.containsKey(texture.getTextureName())) {
            setTextureParameters(texture);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
        GLES20.glBindTexture(texture.getTextureTarget(), texture.getTextureId());
        GLES20.glUniform1i(textureHandles.get(texture.getTextureName()), index);
    }

    public void bindTextureByName(String name, int index, BaseTexture texture) {
        if (!textureHandles.containsKey(texture.getTextureName())) {
            setTextureHandleForName(name);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
        GLES20.glBindTexture(texture.getTextureTarget(), texture.getTextureId());
        GLES20.glUniform1i(textureHandles.get(name), index);
    }

    /**
     * Unbinds the texture from an OpenGL texturing target.
     */
    public void unbindTextures() {
        int num = textures.size();

        if (plugins != null)
            for (IMaterialPlugin plugin : plugins)
                plugin.unbindTextures();

        for (int i = 0; i < num; i++) {
            BaseTexture texture = textures.get(i);
            GLES20.glBindTexture(texture.getTextureTarget(), 0);
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
    public void addTexture(BaseTexture texture) throws TextureException {
        if (textures.indexOf(texture) > -1) return;
        if (textures.size() + 1 > maxTextures) {
            throw new TextureException("Maximum number of textures for this material has been reached. Maximum number"
                                       + " of textures is " + maxTextures + ".");
        }
        textures.add(texture);

        texture.registerMaterial(this);

        isDirty = true;
    }

    /**
     * Removes a texture from this material.
     *
     * @param texture
     */
    public void removeTexture(BaseTexture texture) {
        textures.remove(texture);
        texture.unregisterMaterial(this);
    }

    /**
     * Gets a list of textures bound to this material.
     *
     * @return
     */
    public ArrayList<BaseTexture> getTextureList() {
        return textures;
    }

    /**
     * Copies this material's textures to another material.
     *
     * @param material
     *
     * @throws TextureException
     */
    public void copyTexturesTo(Material material) throws TextureException {
        int num = textures.size();

        for (int i = 0; i < num; ++i)
            material.addTexture(textures.get(i));
    }

    /**
     * Set the vertex buffer handle. This is passed to {@link VertexShader#setVertices(int)}
     *
     * @param vertexBufferHandle
     */
    public void setVertices(final int vertexBufferHandle) {
        vertexShader.setVertices(vertexBufferHandle);
    }

    /**
     * Set the vertex buffer handle. This is passed to {@link VertexShader#setVertices(int)}
     *
     * @param bufferInfo
     */
    public void setVertices(BufferInfo bufferInfo) {
        vertexShader.setVertices(bufferInfo.glHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Set the texture coordinates buffer handle. This is passed to {@link VertexShader#setTextureCoords(int)}
     *
     * @param textureCoordBufferHandle
     */
    public void setTextureCoords(final int textureCoordBufferHandle) {
        vertexShader.setTextureCoords(textureCoordBufferHandle);
    }

    /**
     * Set the texture coordinates buffer handle. This is passed to {@link VertexShader#setTextureCoords(int)}
     *
     * @param bufferInfo
     */
    public void setTextureCoords(BufferInfo bufferInfo) {
        vertexShader.setTextureCoords(bufferInfo.glHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Set the normal buffer handle. This is passed to {@link VertexShader#setNormals(int)}
     *
     * @param normalBufferHandle
     */
    public void setNormals(final int normalBufferHandle) {
        vertexShader.setNormals(normalBufferHandle);
    }

    /**
     * Set the normal buffer handle. This is passed to {@link VertexShader#setNormals(int)}
     *
     * @param bufferInfo
     */
    public void setNormals(BufferInfo bufferInfo) {
        vertexShader.setNormals(bufferInfo.glHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Set the vertex color buffer handle. This is passed to {@link VertexShader#setVertexColors(int)}
     *
     * @param vertexColorBufferHandle
     */
    public void setVertexColors(final int vertexColorBufferHandle) {
        vertexShader.setVertexColors(vertexColorBufferHandle);
    }

    /**
     * Set the vertex color buffer handle. This is passed to {@link VertexShader#setVertexColors(int)}
     *
     * @param bufferInfo
     */
    public void setVertexColors(BufferInfo bufferInfo) {
        vertexShader.setVertexColors(bufferInfo.glHandle, bufferInfo.type, bufferInfo.stride, bufferInfo.offset);
    }

    /**
     * Returns the inverse view matrix. The inverse view matrix is used to transform reflections.
     *
     * @return
     */
    public float[] getInverseViewMatrix() {
        return inverseViewMatrix;
    }

    /**
     * Returns the model view matrix. The model view matrix is used to transform vertices to eye coordinates.
     *
     * @return
     */
    public Matrix4 getModelViewMatrix() {
        return modelMatrix;
    }

    /**
     * Sets the model view projection matrix. The model view projection matrix is used to transform vertices
     * to screen coordinates.
     *
     * @param mvpMatrix
     */
    public void setMVPMatrix(Matrix4 mvpMatrix) {
        vertexShader.setMVPMatrix(mvpMatrix.getFloatValues());
    }

    /**
     * Sets the model matrix. The model matrix holds the object's local coordinates.
     *
     * @param modelMatrix
     */
    public void setModelMatrix(Matrix4 modelMatrix) {
        this.modelMatrix = modelMatrix;//.getFloatValues();
        vertexShader.setModelMatrix(this.modelMatrix);

        normalMatrix.setAll(modelMatrix);
        try {
            normalMatrix.setToNormalMatrix();
        } catch (IllegalStateException exception) {
            RajLog.d("modelMatrix is degenerate (zero scale)...");
        }
        float[] matrix = normalMatrix.getFloatValues();

        normalFloats[0] = matrix[0];
        normalFloats[1] = matrix[1];
        normalFloats[2] = matrix[2];
        normalFloats[3] = matrix[4];
        normalFloats[4] = matrix[5];
        normalFloats[5] = matrix[6];
        normalFloats[6] = matrix[8];
        normalFloats[7] = matrix[9];
        normalFloats[8] = matrix[10];

        vertexShader.setNormalMatrix(normalFloats);
    }

    /**
     * Sets the inverse view matrix. The inverse view matrix is used to transform reflections
     *
     * @param inverseViewMatrix
     */
    public void setInverseViewMatrix(Matrix4 inverseViewMatrix) {
        this.inverseViewMatrix = inverseViewMatrix.getFloatValues();
        vertexShader.setInverseViewMatrix(this.inverseViewMatrix);
    }

    /**
     * Sets the model view matrix. The model view matrix is used to transform vertices to eye coordinates
     *
     * @param modelViewMatrix
     */
    public void setModelViewMatrix(Matrix4 modelViewMatrix) {
        this.modelViewMatrix = modelViewMatrix.getFloatValues();
        vertexShader.setModelViewMatrix(this.modelViewMatrix);
    }

    /**
     * Indicates whether lighting should be used or not. This must be set to true when using a {@link DiffuseMethod}
     * or a {@link SpecularMethod}. Lights are added to a scene {@link Scene} and are automatically added to the
     * material.
     *
     * @param value
     */
    public void enableLighting(boolean value) {
        lightingEnabled = value;
    }

    /**
     * Indicates whether lighting should be used or not. This must be set to true when using a {@link DiffuseMethod} or
     * a {@link SpecularMethod}. Lights are added to a scene {@link Scene} and are automatically added to the material.
     *
     * @return
     */
    public boolean lightingEnabled() {
        return lightingEnabled;
    }

    /**
     * Indicates that the time shader parameter should be used. This is used when creating shaders that should change
     * during the course of time. This is used to accomplish effects like animated vertices, vertex colors, plasma
     * effects, etc. The time needs to be manually updated using the {@link Material#setTime(float)} method.
     *
     * @param value
     */
    public void enableTime(boolean value) {
        timeEnabled = value;
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
        return timeEnabled;
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
        this.time = time;
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
        return time;
    }

    /**
     * The lights that affect the material. Lights shouldn't be managed by any other class
     * than {@link Scene}. To add lights to a scene call {@link Scene#addLight(ALight).
     *
     * @param lights The lights collection
     */
    public void setLights(List<ALight> lights) {
        if (this.lights != null) {
            for (ALight light : lights) {
                if (!this.lights.contains(light)) {
                    break;
                }
            }
        } else {
            isDirty = true;
            this.lights = lights;
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
        if (this.diffuseMethod == diffuseMethod) return;
        this.diffuseMethod = diffuseMethod;
        isDirty = true;
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
        return diffuseMethod;
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
        if (this.specularMethod == specularMethod) return;
        this.specularMethod = specularMethod;
        isDirty = true;
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
        return specularMethod;
    }

    /**
     * Add a material plugin. A material plugin is basically
     * a class that contains a vertex shader fragment and a fragment shader fragment. Material
     * plugins can be used for custom shader effects.
     *
     * @param plugin
     */
    public void addPlugin(IMaterialPlugin plugin) {
        if (plugins == null) {
            plugins = new ArrayList<IMaterialPlugin>();
        } else {
            for (IMaterialPlugin p : plugins) {
                if (plugin.getClass().getSimpleName().equals(p.getClass().getSimpleName()))
                    return;
            }
        }

        plugins.add(plugin);
        isDirty = true;
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
        if (plugins == null) return null;

        for (IMaterialPlugin plugin : plugins) {
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
        if (plugins != null && plugins.contains(plugin)) {
            plugins.remove(plugin);
            isDirty = true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param identity
     */
    public void setOwnerIdentity(String identity) {
        ownerIdentity = identity;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public String getOwnerIdentity() {
        return ownerIdentity;
    }
}
