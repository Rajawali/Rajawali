/**
 * Copyright 2017 Dennis Ippel
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
package c.org.rajawali3d.sceneview.render;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.core.RenderContext;
import c.org.rajawali3d.sceneview.render.gles.GlesFramebuffer;
import c.org.rajawali3d.object.Object3D;
import c.org.rajawali3d.sceneview.RenderSceneView;
import c.org.rajawali3d.sceneview.render.ObjectPipelineTypes.ObjectPipelineFunction;

/**
 * A Subpass is a leaf-node in a 4-level tree of {@link RenderComponent}s, and controls atomic uses of the
 * current {@link RenderContext} graphics pipeline to render from one or more Object3Ds and zero or more sampling
 * Texture2Ds into one or more logical buffer attachments of a target {@link GlesFramebuffer}.
 * <p>
 * A Subpass is responsible for:
 *  <list>
 *  <li> defining (statically) its configuration of required inputs and target GlesFramebuffer attachments/outputs </li>
 *  <li> providing or acquiring the required input {@link Object3D}(s) (and so the rendering primitives) </li>
 *  <li> acquiring and binding any required input sampling Textures </li>
 *  <li> acquiring and attaching the required output Renderbuffers and/or RenderTextures </li>
 *  <li> delegating the Object3D renders
 *  </list>
 * </p>
 *
 * @author randy.picolet
 */
public abstract class Subpass extends RenderComponent {

    /**
     *
     */
    public static final int NO_ATTACHMENT = -1;

    /**
     *
     */
    public static final int[] NO_ATTACHMENTS = {};

    /**
     *
     */
    protected class AttachmentRoles {

        /**
         *
         */
        protected final int[] inputAttachments;

        /**
         *
         */
        protected final int depthStencilAttachment;

        /**
         *
         */
        protected final int[] colorAttachments;

        /**
         *
         */
        protected final int[] resolveAttachments;

        /**
         *
         */
        protected final int[] preserveAttachments;

        /**
         *
         * @param depthStencilAttachment
         * @param colorAttachments
         */
        protected AttachmentRoles(final int depthStencilAttachment, int[] colorAttachments) {
            this(NO_ATTACHMENTS, depthStencilAttachment, colorAttachments, NO_ATTACHMENTS, NO_ATTACHMENTS);
        }

        /**
         *
         * @param inputAttachments
         * @param depthStencilAttachment
         * @param colorAttachments
         * @param resolveAttachments
         * @param preserveAttachments
         */
        protected AttachmentRoles(final int[] inputAttachments, final int depthStencilAttachment,
                                  final int[] colorAttachments, final int[] resolveAttachments,
                                  final int[] preserveAttachments) {
            this.inputAttachments = inputAttachments;
            this.depthStencilAttachment = depthStencilAttachment;
            this.colorAttachments = colorAttachments;
            this.resolveAttachments = resolveAttachments;
            this.preserveAttachments = preserveAttachments;
        }
    }

    /**
     *
     */
    @NonNull
    protected final AttachmentRoles attachmentRoles;

    /**
     *
     */
    protected final @ObjectPipelineFunction
    int pipelineType;

    /**
     *
     */
    protected List<RenderObject> renderObjects;

    /**
     *
     */
    protected boolean rendersFromSceneObjects = true;

    /**
     *
     * @param renderSceneView
     * @param pipelineType
     */
    protected Subpass(RenderSceneView renderSceneView, AttachmentRoles attachmentRoles,
                      @ObjectPipelineFunction int pipelineType) {
        this(renderSceneView, null, RENDERABLE_TO_SCREEN, TARGET_SIZE_TRACKS_VIEWPORT, attachmentRoles, pipelineType);
    }

    /**
     *
     * @param renderSceneView
     * @param pipelineType
     * @param minVersionRenderContext
     * @param renderableToScreen
     * @param targetSizeTracksViewport
     */
    protected Subpass(@NonNull RenderSceneView renderSceneView, @Nullable RenderContext minVersionRenderContext,
                      boolean renderableToScreen, boolean targetSizeTracksViewport, AttachmentRoles attachmentRoles,
                      @ObjectPipelineFunction int pipelineType) {
        super(renderSceneView);
        this.minVersionRenderContext =  minVersionRenderContext == null ?
                RenderContext.getMinimumVersion() : minVersionRenderContext;
        this.renderableToScreen = renderableToScreen;
        this.targetSizeTracksViewport = targetSizeTracksViewport;
        this.attachmentRoles = attachmentRoles;
        this.pipelineType = pipelineType;
    }
}
