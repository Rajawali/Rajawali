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

import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.math.vector.Vector3;

/**
 * Generic interface which any member of IGraphNode must
 * implement in order to be a part of the graph.
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public interface IGraphNodeMember {

	/**
	 * Sets the node that this member is contained in.
	 * 
	 * @param node IGraphNode this member was placed inside.
	 * @param inside Boolean indicating if this object is inside the graph.
	 */
    void setGraphNode(IGraphNode node, boolean inside);
	
	/**
	 * Gets the node that this member is contained in.
	 * 
	 * @return IGraphNode this member was placed inside.
	 */
    IGraphNode getGraphNode();
	
	/**
	 * Gets the objects state in the graph.
	 * 
	 * @return True if the object is inside the graph.
	 */
    boolean isInGraph();
	
	/**
	 * Retrieve the bounding volume of this member.
	 * 
	 * @return IBoundingVolume which encloses this members "geometry."
	 */
    IBoundingVolume getTransformedBoundingVolume();
	
	/**
	 * Retrieve the position in the scene of this member.
	 * 
	 * @return Number3D containing the position.
	 */
    Vector3 getScenePosition();
}
