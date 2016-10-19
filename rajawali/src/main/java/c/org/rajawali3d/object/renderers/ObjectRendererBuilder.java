package c.org.rajawali3d.object.renderers;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.object.RenderableObject;
import org.rajawali3d.geometry.Geometry;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */

public class ObjectRendererBuilder {

    @Nullable private Material material;

    private boolean isDoubleSided = false;

    private boolean isTransparent = false;

    private boolean isBackSided = false;

    private boolean isBlended = false;

    private boolean isDepthEnabled = true;

    private int blendSourceFactor = GLES20.GL_SRC_ALPHA;

    private int blendDestinationFactor = GLES20.GL_ONE_MINUS_SRC_ALPHA;

    private int depthFunction = GLES20.GL_LESS;

    private void checkState() throws IllegalStateException {
        if (material == null) {
            throw new IllegalStateException("ObjectRenderer may not be created without a material being set.");
        }
    }

    @NonNull
    public ObjectRenderer build() throws IllegalStateException {
        checkState();

        return new ObjectRenderer() {
            @Override public void ensureState(@Nullable ObjectRenderer lastUsed) {

            }

            @Override
            public void setCameraMatrices(@NonNull Matrix4 view, @NonNull Matrix4 projection,
                                          @NonNull Matrix4 viewProjection) {

            }

            @Override public void prepareForObject(@NonNull RenderableObject object) {

            }

            @Override public void issueDrawCalls(@NonNull Geometry geometry) {

            }

            @Override public boolean isDoubleSided() {
                return false;
            }

            @Override public boolean isTransparent() {
                return false;
            }

            @Override public boolean isBackSided() {
                return false;
            }

            @Override public boolean isBlended() {
                return false;
            }

            @Override public boolean isDepthTestEnabled() {
                return false;
            }

            @Override public int getBlendSourceFactor() {
                return 0;
            }

            @Override public int getBlendDestinationFactor() {
                return 0;
            }

            @Override public int getDepthFunction() {
                return 0;
            }
        };
    }

    @NonNull
    public ObjectRendererBuilder withMaterial(@NonNull Material material) {

        return this;
    }

    @NonNull
    public ObjectRendererBuilder isDoubleSided(boolean doubleSided) {

        return this;
    }

    @NonNull
    public ObjectRendererBuilder isTransparent(boolean transparent) {

        return this;
    }

    @NonNull
    public ObjectRendererBuilder isBackSided(boolean backsided) {
        return this;
    }

    @NonNull
    public ObjectRendererBuilder isBlended(boolean blended) {
        return this;
    }

    @NonNull
    public ObjectRendererBuilder isDepthTestEnabled(boolean depthTested) {
        return this;
    }

    @NonNull
    public ObjectRendererBuilder setBlendSourceFactor(int factor) {
        return this;
    }

    @NonNull
    public ObjectRendererBuilder setBlendDestinationFactor(int factor) {
        return this;
    }

    @NonNull
    public ObjectRendererBuilder setDepthFunction(int function) {
        return this;
    }
}
