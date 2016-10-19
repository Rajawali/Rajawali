package c.org.rajawali3d.object.renderers;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rajawali3d.geometry.Geometry;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;

import c.org.rajawali3d.object.RenderableObject;

/**
 * Builder for configuring and instantiating an {@link ObjectRenderer}. A {@link Material} must always be set.
 * The default configuration is for CCW Front Sided, back face culling, non-blended (source factor = source alpha,
 * destination factor = 1 - source alpha), depth enabled (less than).
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ObjectRendererBuilder {

    @Nullable
    private Material material;

    private boolean isDoubleSided = false;

    private boolean isBackSided = false;

    private boolean isBlended = false;

    private boolean isDepthEnabled = true;

    private int blendSourceFactor = GLES20.GL_SRC_ALPHA;

    private int blendDestinationFactor = GLES20.GL_ONE_MINUS_SRC_ALPHA;

    private int depthFunction = GLES20.GL_LESS;

    /**
     * Checks that the builder is in an allowable state for generating an instance of {@link ObjectRenderer}.
     *
     * @throws IllegalStateException Thrown if this {@link ObjectRendererBuilder} is not in a buildable state.
     */
    private void checkState() throws IllegalStateException {
        if (material == null) {
            throw new IllegalStateException("ObjectRenderer may not be created without a material being set.");
        }
    }

    /**
     * Builds an instance of {@link ObjectRenderer} based on the configuration of this {@link ObjectRendererBuilder}.
     *
     * @return {@link ObjectRenderer} instance which will render based on the configuration.
     * @throws IllegalStateException Thrown if this {@link ObjectRendererBuilder} is not in a buildable state.
     */
    @NonNull
    public ObjectRenderer build() throws IllegalStateException {
        // Ensure the state first
        checkState();

        // Return the anonymous instance
        return new ObjectRenderer() {

            RenderableObject object;

            @Override
            public void ensureState(@Nullable ObjectRenderer lastUsed) {
                if (equals(lastUsed)) {
                    // Fail Fast - The last renderer and this one are the same, we don't need to do anything
                    return;
                }
                if (lastUsed == null) {
                    // There was no last used renderer provided, so we must assume we need to apply the entire state
                    applyDoubleSided();
                    applyBlending();
                    applyDepth();
                } else {
                    // There was a last used renderer and we should check what we actually need to change
                    checkDoubleSided(lastUsed);
                    checkBlending(lastUsed);
                    checkDepth(lastUsed);
                }
            }

            @Override
            public void setCameraMatrices(@NonNull Matrix4 view, @NonNull Matrix4 projection,
                                          @NonNull Matrix4 viewProjection) {
                // TODO: Handle matrices for material
            }

            @Override
            public void prepareForObject(@NonNull RenderableObject object) {
                this.object = object;
            }

            @Override
            public void issueDrawCalls(@NonNull Geometry geometry) {
                geometry.issueDrawCalls();
            }

            @Override
            public boolean isDoubleSided() {
                return isDoubleSided;
            }

            @Override
            public boolean isBackSided() {
                return isBackSided;
            }

            @Override
            public boolean isBlended() {
                return isBlended;
            }

            @Override
            public boolean isDepthTestEnabled() {
                return isDepthEnabled;
            }

            @Override
            public int getBlendSourceFactor() {
                return blendSourceFactor;
            }

            @Override
            public int getBlendDestinationFactor() {
                return blendDestinationFactor;
            }

            @Override
            public int getDepthFunction() {
                return depthFunction;
            }

            private void applyDoubleSided() {
                if (isDoubleSided) {
                    GLES20.glDisable(GLES20.GL_CULL_FACE);
                } else {
                    GLES20.glEnable(GLES20.GL_CULL_FACE);
                    applyBackFace();
                }
            }

            private void applyBackFace() {
                if (isBackSided) {
                    GLES20.glCullFace(GLES20.GL_FRONT);
                } else {
                    GLES20.glCullFace(GLES20.GL_BACK);
                    GLES20.glFrontFace(GLES20.GL_CCW);
                }
            }

            private void applyBlending() {
                if (isBlended) {
                    GLES20.glEnable(GLES20.GL_BLEND);
                    GLES20.glBlendFunc(blendSourceFactor, blendDestinationFactor);
                } else {
                    GLES20.glDisable(GLES20.GL_BLEND);
                }
            }

            private void applyDepth() {
                if (!isDepthEnabled) {
                    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                } else {
                    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                    GLES20.glDepthFunc(depthFunction);
                }
            }

            private void checkDoubleSided(@NonNull ObjectRenderer lastUsed) {
                if (isDoubleSided && !lastUsed.isDoubleSided()) {
                    // We are double sided, the last renderer is not - disable culling
                    GLES20.glDisable(GLES20.GL_CULL_FACE);
                } else {
                    // We are not double sided
                    if (lastUsed.isDoubleSided()) {
                        // The last renderer is - enable culling
                        GLES20.glEnable(GLES20.GL_CULL_FACE);
                        // We dont know the backface so set it
                        applyBackFace();
                    } else {
                        // The last renderer is not - check the face
                        if (isBackSided != lastUsed.isBackSided()) {
                            // The backfaces differ, set
                            applyBackFace();
                        }
                    }
                }
            }

            private void checkBlending(@NonNull ObjectRenderer lastUsed) {
                if (isBlended && !lastUsed.isBlended()) {
                    // We are blended, the last renderer is not - enable
                    GLES20.glEnable(GLES20.GL_BLEND);
                    GLES20.glBlendFunc(blendSourceFactor, blendDestinationFactor);
                } else if (lastUsed.isBlended()) {
                    // We are not blended, the last renderer is - disable
                    GLES20.glDisable(GLES20.GL_BLEND);
                }
            }

            private void checkDepth(@NonNull ObjectRenderer lastUsed) {
                if (!isDepthEnabled) {
                    if (lastUsed.isDepthTestEnabled()) {
                        // We are not depth tested, the last renderer is - disable
                        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                    } else {
                        // We are not depth tested, neither was last renderer
                    }
                } else if (!lastUsed.isDepthTestEnabled()) {
                    // We are depth tested, the last renderer is not - enable
                    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                    GLES20.glDepthFunc(depthFunction);
                }
            }
        };
    }

    /**
     * Sets the {@link Material} to be used.
     *
     * @param material {@link Material} to use for the renderer.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder withMaterial(@NonNull Material material) {
        this.material = material;
        return this;
    }

    /**
     * Enables/disables double sided polygons.
     *
     * @param doubleSided {@code true} to enable double sided polygons.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder isDoubleSided(boolean doubleSided) {
        isDoubleSided = doubleSided;
        return this;
    }

    /**
     * Enables/disables back sided polygons.
     *
     * @param backsided {@code true} to enable backsided polygons.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder isBackSided(boolean backsided) {
        isBackSided = backsided;
        return this;
    }

    /**
     * Enables/disables blending.
     *
     * @param blended {@code true} to enable blending.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder isBlended(boolean blended) {
        isBlended = blended;
        return this;
    }

    /**
     * Enables/disables dept testing.
     *
     * @param depthTested {@code true} to enable depth testing.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder isDepthTestEnabled(boolean depthTested) {
        isDepthEnabled = depthTested;
        return this;
    }

    /**
     * Sets the blending equation source factor.
     *
     * @param factor {@code int} One of the GL constants for blend factors.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder setBlendSourceFactor(int factor) {
        // TODO: Check allowed values
        blendSourceFactor = factor;
        return this;
    }

    /**
     * Sets the blending equation destination factor.
     *
     * @param factor {@code int} One of the GL constants for blend factors.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder setBlendDestinationFactor(int factor) {
        // TODO: Check allowed values
        blendDestinationFactor = factor;
        return this;
    }

    /**
     * Sets the depth function.
     *
     * @param function {@code int} One of the GL constants for depth functions.
     *
     * @return {@code this} pointer for chaining.
     */
    @NonNull
    public ObjectRendererBuilder setDepthFunction(int function) {
        // TODO: Check allowed values
        depthFunction = function;
        return this;
    }
}
