package c.org.rajawali3d.sceneview.render.gles;

import static c.org.rajawali3d.sceneview.render.Subpass.NO_ATTACHMENTS;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.sceneview.RenderSceneView;
import c.org.rajawali3d.sceneview.Skybox;
import c.org.rajawali3d.sceneview.Viewport;
import c.org.rajawali3d.sceneview.render.RenderPass;
import c.org.rajawali3d.sceneview.render.gles.GlesPass.GlesAttachmentDescriptor;
import c.org.rajawali3d.sceneview.render.ObjectPipelineTypes.ObjectPipelineFunction;
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
    protected static final boolean CLEAR_BEFORE_USE = true;

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
        protected final boolean preserveAfterLasttUse;

        /**
         *
         * @param internalFormat
         */
        protected GlesAttachmentDescriptor(TexelFormat internalFormat) {
            this(internalFormat, CLEAR_BEFORE_USE);
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
         * @param preserveAfterLasttUse
         */
        protected GlesAttachmentDescriptor(TexelFormat internalFormat, int sampleCount,
                                           boolean clearBeforeFirstUse, boolean preserveAfterLasttUse) {
            this.internalFormat = internalFormat;
            this.sampleCount = sampleCount;
            this.clearBeforeFirstUse = clearBeforeFirstUse;
            this.preserveAfterLasttUse = preserveAfterLasttUse;
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
            return preserveAfterLasttUse;
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
     * @param renderSceneView
     */
    protected GlesPass(final @NonNull RenderSceneView renderSceneView,
                       final @NonNull GlesAttachmentDescriptor[] attachments) {
        super(renderSceneView,attachments);
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
        addSubpasses();
        commitSubpasses();
    }

    /**
     *
     * @param depthStencilAttachment
     * @param colorAttachments
     */
    protected void addSubpass(final @ObjectPipelineFunction int pipelineType, final int depthStencilAttachment,
                              final int[] colorAttachments) {
        this.addSubpass(pipelineType, NO_ATTACHMENTS, depthStencilAttachment, colorAttachments, NO_ATTACHMENTS);
    }

    /**
     *
     */
    protected void addSubpass(final @ObjectPipelineFunction int pipelineType, final int[] inputAttachments,
                              final int depthStencilAttachment, final int[] colorAttachments,
                              final int[] preserveAttachments) {
        if (lastSubpassIndex == NO_SUBPASS) {
            subpasses.add(new GlesSubpass(pipelineType, inputAttachments, depthStencilAttachment,
                    colorAttachments, preserveAttachments));
        }
    }

    /**
     *
     */
    protected abstract void addSubpasses();

    /**
     *
     */
    protected void commitSubpasses() {
        subpassesCount = subpasses.size();
        if (subpassesCount == 0) {
            throw new IllegalStateException("At least one Subpass must be added!");
        }
        lastSubpassIndex = subpassesCount - 1;
    }

    /**
     *
     * @return
     */
    protected final boolean hasMultipleSubpasses() {
        return subpassesCount > 1;
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
        skybox =  renderSceneView.getSkybox();
    }

    protected void updateCameraMatrices() {
        viewMatrix = renderSceneView.getViewMatrix();
        projectionMatrix = renderSceneView.getProjectionMatrix();
        viewProjectiondMatrix = renderSceneView.getViewProjectionMatrix();
    }

    protected void updateViewport() {
        viewport = renderSceneView.getViewport();
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
