package c.org.rajawali3d.sceneview.render.gles;

import static android.R.attr.type;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.control.RenderContext;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.sceneview.SceneViewInternal;
import c.org.rajawali3d.sceneview.render.Subpass;

/**
 * @author Randy Picolet
 */

public class GlesSubpass extends Subpass {

    //
    // Construction
    //

    /**
     *
     * @param attachmentRoles
     * @param pipelineType
     */
    protected GlesSubpass(@NonNull SceneViewInternal sceneViewInternal, @PipelineType int pipelineType,
                          @NonNull AttachmentRoles attachmentRoles) {
        this(sceneViewInternal, null, RENDERABLE_TO_SCREEN, TARGET_SIZE_TRACKS_VIEWPORT, attachmentRoles,
                pipelineType);
    }


    protected GlesSubpass(@NonNull SceneViewInternal sceneViewInternal, @Nullable RenderContext minVersionRenderContext,
                          boolean renderableToScreen, boolean targetSizeTracksViewport,
                          AttachmentRoles attachmentRoles, @PipelineType int pipelineType) {
        super(sceneViewInternal, minVersionRenderContext, renderableToScreen, targetSizeTracksViewport,
                attachmentRoles, pipelineType);
    }

    //
    // Access
    //

    //
    // Intialization
    //

    public void initialize() {
        GlesPass.this.rendersFromSceneObjects = rendersFromSceneObjects(subpassIndex);
        fendersToScreen = rendersToScreen(subpassIndex);

    }

    /**
     *
     * @param subpassIndex
     * @return
     */
    protected boolean rendersFromSceneObjects(int subpassIndex) {
        return subpassIndex == 0;
    }

    /**
     *
     * @return
     */
    protected boolean rendersToScreen(int subpassIndex) {
        return (subpassIndex == lastSubpassIndex) && rendersToScreen;
    }

    //
    // Rendering
    //

    public void render() {

        // Prepare the framebuffer and its attachments
        ensureFramebuffer();
        ensureAttachments();
        ensureViewportScissor();
        clearOutputAttachments();

        //
        renderObjects();
    }

    /**
     *
     */
    protected void ensureFramebuffer() {
        // TODO
    }

    /**
     *
     */
    protected void ensureAttachments() {
        // TODO
    }

    /**
     *
     */
    protected void ensureViewportScissor() {
        if (rendersToScreen(subpassIndex)) {
            applyOnScreenViewportScissor();
        } else {
            applyFramebufferObjectViewportScissor();
        }
    }

    protected void applyOnScreenViewportScissor() {
        int left = sceneViewInternal.getViewportLeft();
        int top = sceneViewInternal.getViewportTop();
        GLES20.glViewport(left, top, renderTargetWidth, renderTargetHeight);
        GLES20.glScissor(left, top, renderTargetWidth, renderTargetHeight);
    }

    protected void applyFramebufferObjectViewportScissor() {

        // This default implementation assumes targetSizeTracksViewport...
        if (!targetSizeTracksViewport) {
            throw new IllegalStateException("GlesPass.setFramebufferObjectViewportScissor(): not viewport-sized!");
        }

        if (usesFramebufferObject()) {
            // Using upper-left area of all images/buffers to enable buffer re-use & minimize memory
            GLES20.glViewport(0, 0, renderTargetWidth, renderTargetHeight);
            GLES20.glScissor(0, 0, renderTargetWidth, renderTargetHeight);
        }
    }

    protected void clearOutputAttachments() {
        /// TODO depthStencil and color attachments
    }

    @RequiresReadLock
    //@Override
    @RenderThread
    public void renderObjects() throws IllegalStateException {

        ObjectRenderer lastUsedObjectRenderer = sceneViewInternal.getLastUsedObjectRenderer();

        //
        for (RenderableObject renderableObject : renderObjects) {
            lastUsedObjectRenderer = renderableObject.render(objectRenderType, lastUsedObjectRenderer,
                    viewMatrix, projectionMatrix, viewProjectiondMatrix);
        }
        // Loop each node and draw
        for (RenderableObject object : objects) {
            lastUsedObjectRenderer = object.render(objectRenderType, lastUsedObjectRenderer, viewMatrix, projectionMatrix, viewProjectionMatrix);
        }
    }

    /**
     *
     */
    protected void renderSkybox() {
        skybox.setPosition(sceneViewInternal.getCamera().getPosition());
        renderObject(skybox);
         /*
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);

        skybox.setPosition(mCamera.getX(), mCamera.getY(), mCamera.getZ());
        // Model matrix updates are deferred to the render method due to parent matrix needs
        // Render the skybox
        mSkybox.render(mCamera, mVPMatrix, mPMatrix, mVMatrix, null);

        if (mEnableDepthBuffer) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(true);
        }
        */
    }

    /**
     *
     * @param object3D
     */
    @RequiresReadLock
    @NonNull
    protected void renderObject(GlesObject3D object3D) {

        // Retrieve the object renderer
        ObjectPipeline currentPipeline = obect3D.getPipeline(type);

        // Ensure the state is as it needs to be
        renderer.ensureState(lastPipeline);

        // Apply camera matrices
        renderer.setCameraMatrices(view, projection, viewProjection);

        // Make any object specific preparations in the renderer
        renderer.prepareForObject(this);

        // Have the geometry issue the appropriate draw calls
        renderer.issueDrawCalls(geometry);

        return renderer;
    }

    //
    // Destruction
    //

}
