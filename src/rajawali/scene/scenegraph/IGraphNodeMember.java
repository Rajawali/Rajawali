package rajawali.scene.scenegraph;

import java.util.List;

import rajawali.Camera;
import rajawali.bounds.volumes.IBoundingVolume;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;

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
	 * @return True if the member is inside the graph.
	 */
	public boolean isInGraph();
	
	/**
	 * Renders the object to the scene if it is renderable (Object3D)
	 * 
	 * @param camera The camera
	 * @param vpMatrix The view-projection matrix
	 * @param projMatrix The projection matrix
	 * @param vMatrix The view matrix
	 * @param parentMatrix This object's parent matrix
	 * @param pickerInfo The current color picker info. This is only used when an object is touched.
	 */
	public void renderToFrame(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, 
			final Matrix4 vMatrix, ColorPickerInfo pickerInfo);
	
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
	
	/**
	 * Check if the node member has child members.
	 * 
	 * @return True if the member has child members.
	 */
	public boolean hasChildMembers();
	
	/**
	 * Fetches a {@link List} of all the child members of this member.
	 * If the member has no children it will return null.
	 * 
	 * @return {@link List} containing the children, or null if no children exist.
	 */
	public List<IGraphNodeMember> getChildMembers();
}
