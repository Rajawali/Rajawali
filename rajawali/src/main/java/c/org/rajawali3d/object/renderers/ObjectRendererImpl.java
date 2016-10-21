package c.org.rajawali3d.object.renderers;

import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import c.org.rajawali3d.object.RenderableObject;
import org.rajawali3d.geometry.Geometry;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
class ObjectRendererImpl implements ObjectRenderer {

    @NonNull final Material material;
    final          boolean  isDoubleSided;
    final          boolean  isBackSided;
    final          boolean  isBlended;
    final          boolean  isDepthEnabled;
    final          int      blendSourceFactor;
    final          int      blendDestinationFactor;
    final          int      depthFunction;

    RenderableObject object;

    ObjectRendererImpl(@NonNull Material material, boolean isDoubleSided, boolean isBackSided, boolean isBlended,
                       boolean isDepthEnabled, int blendSourceFactor, int blendDestinationFactor,
                       int depthFunction) {
        this.material = material;
        this.isDoubleSided = isDoubleSided;
        this.isBackSided = isBackSided;
        this.isBlended = isBlended;
        this.isDepthEnabled = isDepthEnabled;
        this.blendSourceFactor = blendSourceFactor;
        this.blendDestinationFactor = blendDestinationFactor;
        this.depthFunction = depthFunction;
    }

    @Override
    public void ensureState(@Nullable ObjectRenderer lastUsed) {
        if (equals(lastUsed)) {
            // Fail Fast - The last renderer and this one are the same, we don't need to do anything
            return;
        }
        if (lastUsed == null) {
            // There was no last used renderer provided, so we must assume we need to apply the entire state
            applyMaterial();
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
        // Bind VBOS

        // Issue Draw Call
        geometry.issueDrawCalls();
    }

    @NonNull
    @Override
    public Material getMaterial() {
        return material;
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

    private void applyMaterial() {
        // Bind the program
        material.useProgram();

        // Bind the textures
        material.bindTextures();

        // Apply shader parameters
        material.applyParams();
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
            Log.v("BLENDING", "Enabling blending");
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
}
