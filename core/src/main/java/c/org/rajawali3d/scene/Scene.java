package c.org.rajawali3d.scene;

import c.org.rajawali3d.control.SceneDelegate;
import c.org.rajawali3d.scene.graph.SceneGraph;
import c.org.rajawali3d.sceneview.SceneView;

import java.util.concurrent.locks.Lock;

/**
 * Client interface for models that are to be presented by the RenderControl.
 *
 * Author: Randy Picolet
 */

public interface Scene extends SceneDelegate {

    SceneGraph getSceneGraph();
}
