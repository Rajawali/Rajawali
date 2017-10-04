package c.org.rajawali3d.sceneview.render.gles;

import c.org.rajawali3d.sceneview.render.RenderPassChain;

/**
 * @author Randy Picolet
 */

public abstract class GlesPassChain extends RenderPassChain {

    // Multi-pass attachments
    public class ChainAttachment {
        int outputPassIndex;
        int outputAttachmentIndex;
        int inputPassIndex;
        int inputAttachmentIndex;
    }



}
