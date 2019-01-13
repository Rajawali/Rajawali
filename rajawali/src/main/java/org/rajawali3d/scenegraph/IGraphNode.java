/**
 * Copyright 2013 Dennis Ippel
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
package org.rajawali3d.scenegraph;

import java.util.Collection;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.Scene;


/**
 * Generic interface allowing for the incorporation of scene graphs
 * to the rendering pipeline of Rajawali. To be a member of scene graphs
 * which implement this interface, an object must inherit from
 * ATransformable3D.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public interface IGraphNode {

	/**
	 * This enum defines the different scene graphs which {@link Scene}
	 * can use. If a new type is created it should be added to this list.
	 */
    enum GRAPH_TYPE {
		NONE, OCTREE
	}

	/**
	 * Adds an object to the scene graph. Implementations do not
	 * need to support online adjustment of the scene graph, and
	 * should clearly document what their add behavior is.
	 *
	 * @param object BaseObject3D to be added to the graph.
	 */
    void addObject(IGraphNodeMember object);

	/**
	 * Adds a collection of objects to the scene graph. Implementations
	 * do not need to support online adustment of the scene graph, and
	 * should clearly document what their add behavior is.
	 *
	 * @param objects Collection of {@link IGraphNodeMember} objects to add.
	 */
    void addObjects(Collection<IGraphNodeMember> objects);

	/**
	 * Removes an object from the scene graph. Implementations do not
	 * need to support online adjustment of the scene graph, and should
	 * clearly document what their removal behavior is.
	 *
	 * @param object BaseObject3D to be removed from the graph.
	 */
    void removeObject(IGraphNodeMember object);

	/**
	 * Removes a collection of objects from the scene graph. Implementations do not
	 * need to support online adjustment of the scene graph, and should
	 * clearly document what thier removal behavior is.
	 *
	 * @param objects Collection of {@link IGraphNodeMember} objects to remove.
	 */
    void removeObjects(Collection<IGraphNodeMember> objects);

	/**
	 * This should be called whenever an object has moved in the scene.
	 * Implementations should determine its new position in the graph.
	 *
	 * @param object BaseObject3D to re-examine.
	 */
    void updateObject(IGraphNodeMember object);

	/**
	 * Set the child addition behavior. Implementations are expected
	 * to document their default behavior.
	 *
	 * @param recursive boolean Should the children be added recursively.
	 */
    void addChildrenRecursively(boolean recursive);

	/**
	 * Set the child removal behavior. Implementations are expected to
	 * document their default behavior.
	 *
	 * @param recursive boolean Should the children be removed recursively.
	 */
    void removeChildrenRecursively(boolean recursive);

	/**
	 * Can be called to force a reconstruction of the scene graph
	 * with all added children. This is useful if the scene graph
	 * does not support online modification.
	 */
    void rebuild();

	/**
	 * Can be called to remove all objects from the scene graph.
	 */
    void clear();

	/**
	 * Called to cause the scene graph to determine which objects are
	 * contained (even partially) by the provided volume. How this is
	 * done is left to the implementation.
	 *
	 * @param volume IBoundingVolume to test visibility against.
	 */
    void cullFromBoundingVolume(IBoundingVolume volume);

	/**
	 * Call this in the renderer to cause the scene graph to be
	 * displayed. It is up to the implementation to determine
	 * the best way to accomplish this (draw volumes, write text,
	 * log statements, etc.)
	 *
	 * @param display boolean indicating if the graph is to be displayed.
	 */
    void displayGraph(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix);

	/**
	 * Retrieve the minimum bounds of this scene.
	 *
	 * @return {@link Vector3} The components represent the minimum value in each axis.
	 */
    Vector3 getSceneMinBound();

	/**
	 * Retrieve the maximum bounds of this scene.
	 *
	 * @return {@link Vector3} The components represent the maximum value in each axis.
	 */
    Vector3 getSceneMaxBound();

	/**
	 * Retrieve the number of objects this node is aware of. This count should
	 * be recursive, meaning each node should ask its children for a count and
	 * return the sum of that count.
	 *
	 * @return int containing the object count.
	 */
    int getObjectCount();

	/**
	 * Does this volume fully contain the input volume.
	 *
	 * @param boundingVolume Volume to check containment of.
	 * @return boolean result of containment test.
	 */
    boolean contains(IBoundingVolume boundingVolume);

	/**
	 * Is this volume fully contained by the input volume.
	 *
	 * @param boundingVolume Volume to check containment by.
	 * @return boolean result of containment test.
	 */
    boolean isContainedBy(IBoundingVolume boundingVolume);
}