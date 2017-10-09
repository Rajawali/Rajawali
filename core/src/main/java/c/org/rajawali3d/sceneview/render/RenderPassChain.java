package c.org.rajawali3d.sceneview.render;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.sceneview.RenderSceneView;

/**
 * @author Randy Picolet
 */

public abstract class RenderPassChain extends CompositeRender<RenderPass> {

    public class ChainAttachment {

        final int passIndex;

        final int attachmentIndex;

        public ChainAttachment(@IntRange(from = 0) int passIndex, @IntRange(from = 0) int attachmentIndex) {
            this.passIndex = passIndex;
            this.attachmentIndex = attachmentIndex;
        }
    }

    // Attachment shared between RenderPasses

    public class ChainAttachmentLink {

        final ChainAttachment sourceAttachment;
        final ChainAttachment sinkAttachment;

        public ChainAttachmentLink(@NonNull ChainAttachment sourceAttachment,
                                   @NonNull ChainAttachment sinkAttachment) {
            this.sourceAttachment = sourceAttachment;
            this.sinkAttachment = sinkAttachment;
        }
    }

    @Nullable
    private final ChainAttachmentLink[] links;


    protected RenderPassChain(@NonNull RenderSceneView renderSceneView, @NonNull RenderPass[] children) {
        this(renderSceneView, children, null);
    }

    protected RenderPassChain(@NonNull RenderSceneView renderSceneView, @NonNull RenderPass[] children,
                              @Nullable ChainAttachmentLink[] links) {
        super(renderSceneView, children);
        // TODO links validation
        this.links = links;
    }

}
