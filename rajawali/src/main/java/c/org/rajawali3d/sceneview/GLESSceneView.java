package c.org.rajawali3d.sceneview;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.scene.AScene;
import c.org.rajawali3d.scene.graph.NodeMember;

import android.opengl.GLES20;

import org.rajawali3d.math.Matrix4;

import java.util.List;

/**
 * @author Randy Picolet
 */

public class GLESSceneView extends ASceneView {

    /**
     * Renders the {@link AScene} into the current viewportRect;
     *
     * @throws IllegalStateException
     */
    @RequiresReadLock
    @RenderThread
    protected void renderObjects(List<RenderableObject> objects) throws IllegalStateException {

        GLES20.glViewport(viewportRect.left, viewportRect.top, viewportRect.width(), viewportRect.height());

        // still TODO: This will be an interaction point with the render pass manager. We don't want to check the
        // intersection with the camera multiple times. One possible exception would be for shadow mapping. Probably
        // a loop

        // TODO maybe render passes are simply the direct responsibility of SceneView implementations?

        // Fetch the current render type
        int type = 0;

        // Loop each node and draw
        for (RenderableObject object : objects) {
            lastUsedRenderer = object.render(type, lastUsedRenderer, viewMatrix, projectionMatrix, viewProjectionMatrix);
        }
    }


}
