package c.org.rajawali3d.sceneview.render.gles;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.sceneview.SceneViewInternal;
import c.org.rajawali3d.sceneview.sky.Skybox;
import c.org.rajawali3d.sceneview.Viewport;
import c.org.rajawali3d.sceneview.render.RenderPass;
import c.org.rajawali3d.sceneview.render.gles.GlesPass.GlesAttachmentDescriptor;
import c.org.rajawali3d.sceneview.render.Subpass;
import c.org.rajawali3d.textures.annotation.TexelFormat;
import org.rajawali3d.math.Matrix4;

/**
 * @author Randy Picolet
 */

public abstract class GlesPass extends RenderPass<GlesAttachmentDescriptor, GlesSubpass> {

    //
    // Attachment descriptors
    //

    /**
     *
     */
    protected static final boolean CLEAR_BEFORE_FIRST_USE = true;

    /**
     *
     */
    protected static final boolean PRESERVE_AFTER_LAST_USE = true;

    /**
     * Gles-specific Attachment descriptor
     */
    protected final class GlesAttachmentDescriptor {

        /**
         *
         */
        @NonNull
        protected final TexelFormat internalFormat;

        /**
         *
         */
        protected final int sampleCount;

        /**
         *
         */
        protected final boolean clearBeforeFirstUse;

        /**
         *
         */
        protected final boolean preserveAfterLastUse;

        /**
         *
         * @param internalFormat
         */
        protected GlesAttachmentDescriptor(TexelFormat internalFormat) {
            this(internalFormat, CLEAR_BEFORE_FIRST_USE);
        }

        /**
         *
         * @param internalFormat
         * @param clearBeforeFirstUse
         */
        protected GlesAttachmentDescriptor(TexelFormat internalFormat, boolean clearBeforeFirstUse) {
            this(internalFormat, 1, clearBeforeFirstUse);
        }

        /**
         *
         * @param internalFormat
         * @param sampleCount
         * @param clearBeforeFirstUse
         */
        protected GlesAttachmentDescriptor(TexelFormat internalFormat, int sampleCount,
                                           boolean clearBeforeFirstUse) {
            this(internalFormat, sampleCount, clearBeforeFirstUse, !PRESERVE_AFTER_LAST_USE);
        }

        /**
         *
         * @param internalFormat
         * @param sampleCount
         * @param clearBeforeFirstUse
         * @param preserveAfterLastUse
         */
        protected GlesAttachmentDescriptor(TexelFormat internalFormat, int sampleCount,
                                           boolean clearBeforeFirstUse, boolean preserveAfterLastUse) {
            this.internalFormat = internalFormat;
            this.sampleCount = sampleCount;
            this.clearBeforeFirstUse = clearBeforeFirstUse;
            this.preserveAfterLastUse = preserveAfterLastUse;
        }

        /**
         *
         * @return
         */
        public TexelFormat getInternalFormat() {
            return internalFormat;
        }

        /**
         *
         * @return
         */
        public int getSampleCount() {
            return sampleCount;
        }

        /**
         *
         * @return
         */
        public boolean clearBeforeFirstUse() {
            return clearBeforeFirstUse;
        }

        /**
         *
         * @return
         */
        public boolean preserveAfterLasttUse() {
            return preserveAfterLastUse;
        }
    }

    //
    // Subpasses
    //

    //

    //
    // Current framebuffer
    //

    /**
     *  GlesFramebufferObject (if needed)
     */
    @Nullable
    protected GlesFramebufferObject framebufferObject;

    /**
     *
     */
    protected int framebufferName;

    //
    // Current Skybox
    //

    /**
     *
     */
    @Nullable
    protected Skybox skybox;

    //
    // Current Camera-dependent matrices
    //

    /**
     *
     */
    @NonNull
    Matrix4 viewMatrix;

    /**
     *
     */
    @NonNull
    Matrix4 projectionMatrix;

    /**
     *
     */
    @NonNull
    Matrix4 viewProjectiondMatrix;

    //
    // Current Viewport
    //

    /**
     *
     */
    protected Viewport viewport;

    /**
     *
     */
    protected int renderTargetWidth = 0;

    /**
     *
     */
    protected int renderTargetHeight = 0;

    //
    // Current Subpass references and properties
    //

    /**
     *
     */
    protected int subpassIndex;

    /**
     *
     */
    protected Subpass subpassDescriptor;

    /**
     *
     */
    protected boolean rendersFromSceneObjects;

    //
    // Construction
    //

    /**
     *
     * @param sceneViewInternal
     */
    protected GlesPass(final @NonNull SceneViewInternal sceneViewInternal, @NonNull GlesSubpass[] children,
                       final @NonNull GlesAttachmentDescriptor[] attachments) {
        super(sceneViewInternal, children, attachments);
    }

    @Override
    public void setRendersToScreen(boolean rendersToScreen) {
        super.setRendersToScreen(rendersToScreen);
        // Allocate a GlesFramebufferObject if needed
        if (usesFramebufferObject()) {
            framebufferObject = new GlesFramebufferObject();
        }
    }

    //
    // Access
    //

    /**
     *
     * @return
     */
    protected boolean usesFramebufferObject() {
        return !rendersToScreen || hasMultipleSubpasses();
    }

    //
    // Initialization
    //

    @Override
    public void initialize() {
        super.initialize();

    }

    /**
     *
     * @return
     */
    protected final boolean hasMultipleSubpasses() {
        return getChildCount() > 1;
    }

    //
    // Render methods
    //

    @Override
    public void render() {

        updateSkybox();
        updateCameraMatrices();
        updateViewport();

        acquireAttachmentBuffers();

        // Do Subpass renders
        renderChildren();

        releaseAttachmentBuffers();
    }

    protected void updateSkybox() {
        skybox =  sceneViewInternal.getSkybox();
    }

    protected void updateCameraMatrices() {
        viewMatrix = sceneViewInternal.getViewMatrix();
        projectionMatrix = sceneViewInternal.getProjectionMatrix();
        viewProjectiondMatrix = sceneViewInternal.getViewProjectionMatrix();
    }

    protected void updateViewport() {
        viewport = sceneViewInternal.getViewport();
        renderTargetWidth = viewport.getWidth();
        renderTargetHeight = viewport.getHeight();
    }

    private boolean acquireAttachmentBuffers() {
        if (usesFramebufferObject()) {
            // TODO acquire any input attachment buffers from prior passes
            // TODO acquire output attachment buffers
            return false;
        } else {
            // Only using the system-default GlesFramebuffer, all buffers defined/owned by the system
            return true;
        }
    }

    private void releaseAttachmentBuffers() {
        if(usesFramebufferObject()) {
            // TODO
        }
    }
}
