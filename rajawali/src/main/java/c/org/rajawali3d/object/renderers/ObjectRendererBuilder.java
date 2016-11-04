package c.org.rajawali3d.object.renderers;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rajawali3d.materials.Material;

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
     *
     * @throws IllegalStateException Thrown if this {@link ObjectRendererBuilder} is not in a buildable state.
     */
    @NonNull
    public ObjectRenderer build() throws IllegalStateException {
        // Ensure the state first
        checkState();

        // Return the anonymous instance
        assert material != null;
        return new ObjectRendererImpl(this);
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
     * @see <a href="https://www.khronos.org/opengles/sdk/docs/man/xhtml/glDepthFunc.xml">glDepthFunc</a>
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
     * @see <a href="https://www.khronos.org/opengles/sdk/docs/man/xhtml/glBlendFunc.xml">glBlendFunc</a>
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
     * @see <a href="https://www.khronos.org/opengles/sdk/docs/man/xhtml/glBlendFunc.xml">glBlendFunc</a>
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

    /**
     * Retrieves the {@link Material} that has been set for this builder. This method should only be called after
     * {@link #checkState()} has passsed.
     *
     * @return The {@link Material}.
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    Material getMaterial() {
        return material;
    }

    /**
     * Retrieves whether or not the builder has double sided polygons enabled.
     *
     * @return {@code true} if double sided polygons are enabled.
     */
    boolean isDoubleSided() {
        return isDoubleSided;
    }

    /**
     * Retrieves whether or not the builder has back sided polygons enabled.
     *
     * @return {@code true} if back sided polygons are used (vs. front sided).
     */
    boolean isBackSided() {
        return isBackSided;
    }

    /**
     * Retrieves whether or not the builder has blending enabled.
     *
     * @return {@code true} if blending is enabled.
     */
    boolean isBlended() {
        return isBlended;
    }

    /**
     * Retrieves whether or not the builder has depth testing enabled.
     *
     * @return {@code true} if depth testing is enabled.
     */
    boolean isDepthEnabled() {
        return isDepthEnabled;
    }

    /**
     * Retrieves the currently set blend source factor.
     *
     * @return {@code int} The blend source factor.
     */
    public int getBlendSourceFactor() {
        return blendSourceFactor;
    }

    /**
     * Retrieves the currently set blend destination factor.
     *
     * @return {@code int} The blend destination factor.
     */
    int getBlendDestinationFactor() {
        return blendDestinationFactor;
    }

    /**
     * Retrieves the currently set depth test function.
     *
     * @return {@code int} The depth test function.
     */
    int getDepthFunction() {
        return depthFunction;
    }
}
