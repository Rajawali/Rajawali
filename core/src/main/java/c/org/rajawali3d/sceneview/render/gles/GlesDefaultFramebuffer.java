package c.org.rajawali3d.sceneview.render.gles;

/**
 * @author Randy Picolet
 */

public class GlesDefaultFramebuffer extends GlesFramebuffer {

    // TODO not static, can be zero or one, tie lifecycle/move to RenderControl/context!
    public static final GlesDefaultFramebuffer DEFAULT_FRAMEBUFFER = new GlesDefaultFramebuffer();

    GlesDefaultFramebuffer() {
        super(true);
    }

    @Override
    void clearBuffers() {
        // TODO
    }


}
