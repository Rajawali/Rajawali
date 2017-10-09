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
import c.org.rajawali3d.control.RenderContext;
import c.org.rajawali3d.object.Object3D;
import c.org.rajawali3d.sceneview.RenderSceneView;
import c.org.rajawali3d.sceneview.render.gles.GlesFramebuffer;

/**
 * A {@link RenderPass} is a leaf node in a tree of {@link RenderComponent}s, responsible for running one or more
 * {@link Subpass}es using a set of image/render buffers used as Attachments in a GlesFramebuffer.
 *
 * render order and is responsible for mapping output RenderTargets of upstream components to
 * input sources for downstream components
 *
 * A Subpass is a leaf-node in a composite tree of {@link RenderComponent}s that controls atomic uses of the
 * current {@link RenderContext} graphics pipeline to render from one or more Object3Ds and zero or more sampling
 * Texture2Ds into one or more logical buffer attachments of the {@link RenderPass} target {@link GlesFramebuffer}.
 * <p>
 * A Subpass is responsible for:
 *  <list>
 *  <li> defining (statically) its configuration of required inputs and target GlesFramebuffer attachments/outputs </li>
 *  <li> specifying the target rectangle/viewport used by all GlesFramebuffer attachments </li>
 *  <li> providing or acquiring the required input {@link Object3D}(s) (and so the rendering primitives) </li>
 *  <li> acquiring and binding any required input sampling Textures </li>
 *  <li> acquiring and attaching the required output Renderbuffers and/or RenderTextures </li>
 *  <li> delegating the Object3D renders
 *  <li> providing access to its GlesFramebuffer attachments to downstream RenderPasses within the same FrameRender </li>
 *  <li> releasing its GlesFramebuffer attachment resources when no longer needed </li>
 *  </list>
 * </p>
 *
 * @author randy.picolet
 */
public abstract class RenderPass<A, T extends Subpass> extends CompositeRender<Subpass> {

    protected final A[] attachmentDescriptions;

    protected final int attachmentsCount;

    protected RenderPass(@NonNull RenderSceneView renderSceneView,
                         @NonNull Subpass[] children,
                         @NonNull A[] attachmentDescriptions) {
        super(renderSceneView, children);
        debugAssertNonNull(attachmentDescriptions, "attachmentDescriptions");
        this.attachmentDescriptions = attachmentDescriptions;
        attachmentsCount = attachmentDescriptions.length;
        debugAssert(attachmentsCount > 0, "No attachments!");
    }
}
