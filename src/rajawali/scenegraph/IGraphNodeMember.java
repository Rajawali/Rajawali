package rajawali.scenegraph;

import rajawali.bounds.IBoundingVolume;
import rajawali.math.Vector3;

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
	public void setGraphNode(IGraphNode node, boolean inside);
	
	/**
	 * Gets the node that this member is contained in.
	 * 
	 * @return IGraphNode this member was placed inside.
	 */
	public IGraphNode getGraphNode();
	
	/**
	 * Gets the objects state in the graph.
	 * 
	 * @return True if the object is inside the graph.
	 */
	public boolean isInGraph();
	
	/**
	 * Retrieve the bounding volume of this member.
	 * 
	 * @return IBoundingVolume which encloses this members "geometry."
	 */
	public IBoundingVolume getTransformedBoundingVolume();
	
	/**
	 * Retrieve the position in the scene of this member.
	 * 
	 * @return Number3D containing the position.
	 */
	public Vector3 getScenePosition();
}
