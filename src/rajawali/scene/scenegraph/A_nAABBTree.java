package rajawali.scene.scenegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rajawali.ATransformable3D;
import rajawali.Camera;
import rajawali.bounds.BoundingVolumeTester;
import rajawali.bounds.volumes.BoundingBox;
import rajawali.bounds.volumes.BoundingCone;
import rajawali.bounds.volumes.BoundingSphere;
import rajawali.bounds.volumes.CameraFrustum;
import rajawali.bounds.volumes.IBoundingVolume;
import rajawali.math.vector.Vector3;
import rajawali.util.RajLog;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Generic Axis Aligned Bounding Box based tree sorting hierarchy. Subclasses
 * are left to determine child count and any count specific behavior. This implementation
 * in general uses the methodology described in the tutorial listed below by Paramike, with
 * a few modifications to the behavior. 
 * 
 * Implementations of this could be Ternary trees (3), Octree (8), Icoseptree (27), etc.
 * The tree will try to nest objects as deeply as possible while trying to maintain a minimal
 * tree structure based on the thresholds set. It is up to the user to determine what thresholds
 * make sense and are optimal for your specific needs as there are tradeoffs associated with
 * them all. The default implementation attempts to strike a reasonable balance.
 * 
 * This tree design also utilizes an option for overlap between child partitions. This is useful
 * for mimicking some of the behavior of a more complex tree without incurring the complexity. If
 * you specify an overlap percentage, it is more likely that an object near a boundary of the 
 * partitions will fit in one or the other and be able to be nested deeper rather than staying in
 * the parent partition. Note however that in cases where the object is small enough to still be
 * fully contained by both (or more) children, it is added to the parent. This is where a more
 * complex tree would excel, but only in the case over very large object counts.
 * 
 * By default, this tree will NOT recursively add the children of added objects and NOT
 * recursively remove the children of removed objects.
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 * @see {@link http://www.piko3d.com/tutorials/space-partitioning-tutorial-piko3ds-dynamic-octree}
 */
public abstract class A_nAABBTree extends BoundingBox implements IGraphNode {

	protected int CHILD_COUNT = 0; //The number of child nodes used

	protected A_nAABBTree mParent; //Parent partition;
	protected int mLevel = 0; //Level in the tree - 0 = root
	protected A_nAABBTree[] mChildren; //Child partitions
	protected Vector3 mChildLengths; //Lengths of each side of the child nodes

	protected boolean mSplit = false; //Have we split to child partitions
	protected List<IGraphNodeMember> mMembers; //A list of all the member objects
	protected List<IGraphNodeMember> mOutside; //A list of all the objects outside the root

	protected int mOverlap = 0; //Partition overlap
	protected int mGrowThreshold = 5; //Threshold at which to grow the graph
	protected int mShrinkThreshold = 4; //Threshold at which to shrink the graph
	protected int mSplitThreshold = 5; //Threshold at which to split the node
	protected int mMergeThreshold = 2; //Threshold at which to merge the node

	protected boolean mRecursiveAdd = false; //Default to NOT recursive add
	protected boolean mRecursiveRemove = false; //Default to NOT recursive remove.

	protected float[] mMMatrix = new float[16]; //A model matrix to use for drawing the bounds of this node.
	protected Vector3 mPosition; //This node's center point in 3D space.

	/**
	 * The region (e.g. octant) this node occupies in its parent. If this node
	 * has no parent this is a meaningless number. A negative
	 * number is used to represent that there is no region assigned.
	 */
	protected int mChildRegion = -1;

	/**
	 * Default constructor
	 */
	protected A_nAABBTree() {
		super();
		mBoundingColor.set(0xFFFF0000);
	}

	/**
	 * Constructor to setup root node with specified merge/split and
	 * grow/shrink behavior.
	 * 
	 * @param maxMembers int containing the divide threshold count. When more 
	 * members than this are added, a partition will divide into 8 children.
	 * @param minMembers int containing the merge threshold count. When fewer
	 * members than this exist, a partition will recursively merge to its ancestors.
	 * @param overlap int containing the percentage overlap between two adjacent
	 * partitions. This allows objects to be nested deeper in the tree when they
	 * would ordinarily span a boundary.
	 */
	public A_nAABBTree(int mergeThreshold, int splitThreshold, int shrinkThreshold, int growThreshold, int overlap) {
		this(null, mergeThreshold, splitThreshold, shrinkThreshold, growThreshold, overlap);
	}

	/**
	 * Constructor to setup a child node with specified merge/split and 
	 * grow/shrink behavior.
	 * 
	 * @param parent A_nAABBTree which is the parent of this partition.
	 * @param maxMembers int containing the divide threshold count. When more 
	 * members than this are added, a partition will divide into 8 children.
	 * @param minMembers int containing the merge threshold count. When fewer
	 * members than this exist, a partition will recursively merge to its ancestors.
	 * @param overlap int containing the percentage overlap between two adjacent
	 * partitions. This allows objects to be nested deeper in the tree when they
	 * would ordinarily span a boundary.
	 */
	public A_nAABBTree(A_nAABBTree parent, int mergeThreshold, int splitThreshold, int shrinkThreshold, int growThreshold, int overlap) {
		mParent = parent;
		if (mParent == null) {
			mBoundingColor.set(0xFFFF0000);
		} else {
			mLevel = mParent.mLevel + 1;
		}
		mMergeThreshold = mergeThreshold;
		mSplitThreshold = splitThreshold;
		mShrinkThreshold = shrinkThreshold;
		mGrowThreshold = growThreshold;
		mOverlap = overlap;
		init();
	}

	/**
	 * Performs the necessary process to destroy this node
	 */
	protected abstract void destroy();

	/**
	 * Calculates the side lengths that child nodes
	 * of this node should have.
	 */
	protected void calculateChildSideLengths() {
		//Determine the distance on each axis
		Vector3 temp = Vector3.subtractAndCreate(mTransformedMax, mTransformedMin);
		temp.multiply(0.5f); //Divide it in half
		float overlap = 1.0f + mOverlap/100.0f;
		temp.multiply(overlap);
		temp.absoluteValue();
		mChildLengths.setAll(temp);
	}

	/**
	 * Sets the bounding volume of this node. This should only be called
	 * for a root node with no children. This sets the initial root node
	 * to have a volume ~8x the member, centered on the member.
	 * 
	 * @param object IGraphNodeMember the member we will be basing
	 * our bounds on. 
	 */
	protected void setBounds(IGraphNodeMember member) {
		//RajLog.d("[" + this.getClass().getName() + "] Setting bounds based on member: " + member);
		if (mMembers.size() != 0 && mParent != null) {return;}
		IBoundingVolume volume = member.getTransformedBoundingVolume();
		Vector3 position = member.getScenePosition();
		double span_y = 0;
		double span_x = 0;
		double span_z = 0;
		if (volume == null) {
			span_x = 5.0;
			span_y = 5.0;
			span_z = 5.0;
		} else {
			//The order here is chosen to such that events which are more likely are
			//higher in the chain to avoid unnecessary checks.
			if (volume instanceof BoundingBox) {
				BoundingBox bcube = (BoundingBox) volume;
				Vector3 min = bcube.getTransformedMin();
				Vector3 max = bcube.getTransformedMax();
				span_x = (max.x - min.x);
				span_y = (max.y - min.y);
				span_z = (max.z - min.z);
			} else if (volume instanceof BoundingSphere) {
				BoundingSphere bsphere = (BoundingSphere) volume;
				span_x = 2.0*bsphere.getScaledRadius();
				span_y = span_x;
				span_z = span_x;
			} else if (volume instanceof CameraFrustum) {
				CameraFrustum frustum = (CameraFrustum) volume;
				Vector3 far = frustum.getPlanePoint(6);
				Vector3 near = frustum.getPlanePoint(2);
				span_x = far.x;
				span_y = far.y;
				span_z = far.z - near.z;
			} else if (volume instanceof BoundingCone) {
				//TODO: Implement
			} else {
				//This is more to notify developers of a spot they need to expand. It should never
				//occur in production code.
				RajLog.e("[" + this.getClass().getName() + "] Received a bounding box of unknown type: " 
						+ volume.getClass().getCanonicalName());

				throw new IllegalArgumentException("Received a bounding box of unknown type."); 
			}
		}
		mMin.x = (float) (position.x - span_x);
		mMin.y = (float) (position.y - span_y);
		mMin.z = (float) (position.z - span_z);
		mMax.x = (float) (position.x + span_x);
		mMax.y = (float) (position.y + span_y);
		mMax.z = (float) (position.z + span_z);
		mTransformedMin.setAll(mMin);
		mTransformedMax.setAll(mMax);
		calculatePoints();
		calculateChildSideLengths();
	}

	/**
	 * Sets the bounding volume of this node to that of the specified
	 * child. This should only be called for a root node during a shrink
	 * operation. 
	 * 
	 * @param child int Which octant to match.
	 */
	protected void setBounds(int child) {
		A_nAABBTree new_bounds = mChildren[child];
		mMin.setAll(new_bounds.mMin);
		mMax.setAll(new_bounds.mMax);
		mTransformedMin.setAll(mMin);
		mTransformedMax.setAll(mMax);
		calculatePoints();
		calculateChildSideLengths();
	}

	/**
	 * Sets the region this node occupies in its parent.
	 * Subclasses should be sure to call the super implementation
	 * to avoid unexpected behavior.
	 * 
	 * @param region Integer region this child occupies.
	 * @param size Number3D containing the length for each
	 * side this node should be. 
	 */
	protected void setChildRegion(int region, Vector3 side_lengths) {
		mTransformedMin.setAll(mMin);
		mTransformedMax.setAll(mMax);
		calculatePoints();
		calculateChildSideLengths();
		if (mSplit) {
			for (int i = 0; i < CHILD_COUNT; ++i) {
				mChildren[i].setChildRegion(i, mChildLengths);
			}
		}
	}

	/**
	 * Retrieve the octant this node resides in.
	 * 
	 * @return integer The octant.
	 */
	protected int getChildRegion() {
		return mChildRegion;
	}

	/**
	 * Sets the threshold for growing the tree.
	 * 
	 * @param threshold int containing the new threshold.
	 */
	public void setGrowThreshold(int threshold) {
		mGrowThreshold = threshold;
	}

	/**
	 * Sets the threshold for shrinking the tree.
	 * 
	 * @param threshold int containing the new threshold.
	 */
	public void setShrinkThreshold(int threshold) {
		mShrinkThreshold = threshold;
	}

	/**
	 * Sets the threshold for merging this node.
	 * 
	 * @param threshold int containing the new threshold.
	 */
	public void setMergeThreshold(int threshold) {
		mMergeThreshold = threshold;
	}

	/**
	 * Sets the threshold for splitting this node.
	 * 
	 * @param threshold int containing the new threshold.
	 */
	public void setSplitThreshold(int threshold) {
		mSplitThreshold = threshold;
	}

	/**
	 * Adds the specified object to this node's internal member
	 * list and sets the node attribute on the member to this
	 * node.
	 * 
	 * @param object IGraphNodeMember to be added.
	 */
	protected void addToMembers(IGraphNodeMember object) {
		RajLog.d("[" + this.getClass().getName() + "] Adding object: " + object + " to members list in: " + this); 
		object.getTransformedBoundingVolume().setBoundingColor(mBoundingColor.get());
		object.setGraphNode(this, true);
		mMembers.add(object);
	}

	/**
	 * Removes the specified object from this node's internal member
	 * list and sets the node attribute on the member to null.
	 * 
	 * @param object IGraphNodeMember to be removed.
	 */
	protected void removeFromMembers(IGraphNodeMember object) {
		RajLog.d("[" + this.getClass().getName() + "] Removing object: " + object + " from members list in: " + this);
		object.getTransformedBoundingVolume().setBoundingColor(IBoundingVolume.DEFAULT_COLOR);
		object.setGraphNode(null, false);
		mMembers.remove(object);
	}

	/**
	 * Adds the specified object to the scenegraph's outside member
	 * list and sets the node attribute on the member to the root node.
	 * 
	 * @param object IGraphNodeMember to be added.
	 */
	protected void addToOutside(IGraphNodeMember object) {
		RajLog.d("[" + this.getClass().getName() + "] Adding object: " + object + " to outside list in: " + this);
		if (mOutside.contains(object)) return;
		mOutside.add(object);
		object.setGraphNode(this, false);
		object.getTransformedBoundingVolume().setBoundingColor(IBoundingVolume.DEFAULT_COLOR);
	}

	/**
	 * Returns a list of all members of this node and any decendent nodes.
	 * 
	 * @param shouldClear boolean indicating if the search should clear the lists.
	 * @return ArrayList of IGraphNodeMembers.
	 */
	protected ArrayList<IGraphNodeMember> getAllMembersRecursively(boolean shouldClear) {
		ArrayList<IGraphNodeMember> members = new ArrayList<IGraphNodeMember>();
		members.addAll(mMembers);
		if (mParent == null) {
			members.addAll(mOutside);
		}
		if (shouldClear) clear();
		if (mSplit) {
			for (int i = 0; i < CHILD_COUNT; ++i) {
				members.addAll(mChildren[i].mMembers);
				if (shouldClear) mChildren[i].clear();
			}
		}
		return members;
	}

	/**
	 * Internal method for adding an object to the graph. This method will determine if
	 * it gets added to this node or moved to a child node.
	 * 
	 * @param object IGraphNodeMember to be added.
	 */ 
	protected void internalAddObject(IGraphNodeMember object) {
		//TODO: Implement a batch process for this to save excessive splitting/merging
		if (mSplit) {
			//Check if the object fits in our children
			int fits_in_child = -1;
			for (int i = 0; i < CHILD_COUNT; ++i) {
				if (mChildren[i].contains(object.getTransformedBoundingVolume())) {
					//If the member fits in this child, mark that child
					if (fits_in_child < 0) {
						fits_in_child = i;
					} else {
						//It fits in multiple children, leave it in parent
						fits_in_child = -1;
						break;
					}
				}
			}
			if (fits_in_child >= 0) { //If a single child was marked, add the member to it
				mChildren[fits_in_child].addObject(object);
			} else {
				//It didn't fit in any of the children, so store it here
				addToMembers(object);
			}
		} else {
			//We just add it to this node, then check if we should split
			addToMembers(object);
			if (mMembers.size() >= mSplitThreshold) {
				split();
			}
		}
	}

	/**
	 * Adds an object back into the graph when shrinking.
	 * 
	 * @param object The object to be handled.
	 */
	protected void shrinkAddObject(IGraphNodeMember object) {
		if (contains(object.getTransformedBoundingVolume())) {
			internalAddObject(object);
		} else {
			addToOutside(object);
		}
	}

	/**
	 * Splits this node into child nodes. Subclasses
	 * should be sure to call the super implementation
	 * to avoid unexpected behavior.
	 */
	protected void split() {
		//Keep a list of members we have removed
		ArrayList<IGraphNodeMember> removed = new ArrayList<IGraphNodeMember>();
		for (int i = 0; i < mMembers.size(); ++i) {
			int fits_in_child = -1;
			IGraphNodeMember member = mMembers.get(i);
			for (int j = 0; j < CHILD_COUNT; ++j) {
				if (mChildren[j].contains(member.getTransformedBoundingVolume())) {
					//If the member fits in this child, mark that child
					if (fits_in_child < 0) {
						fits_in_child = j;
					} else {
						//It fits in multiple children, leave it in parent
						fits_in_child = -1;
						break;
					}
				}
			}
			if (fits_in_child >= 0) { //If a single child was marked, add the member to it
				mChildren[fits_in_child].addObject(member);
				removed.add(member); //Mark the member for removal from parent
			}
		}
		//Now remove all of the members marked for removal
		mMembers.removeAll(removed);
		mSplit = true; //Flag that we have split
	}

	/**
	 * Merges this child nodes into their parent node. 
	 */
	protected void merge() {
		RajLog.d("[" + this.getClass().getName() + "] Merge nodes called on node: " + this);
		if (mParent != null && mParent.canMerge()) {
			RajLog.d("[" + this.getClass().getName() + "] Parent can merge...passing call up.");
			mParent.merge();
		} else {
			if (mSplit) {
				for (int i = 0; i < CHILD_COUNT; ++i) {
					//Add all the members of all the children
					ArrayList<IGraphNodeMember> members = mChildren[i].getAllMembersRecursively(false);
					int members_count = members.size();
					for (int j = 0; j < members_count; ++j) {
						addToMembers(members.get(j));
					}
					mChildren[i].destroy();
					mChildren[i] = null;
				}
				mSplit = false;
			}
		}
	}

	/**
	 * Grows the tree.
	 */
	protected void grow() {
		RajLog.d("[" + this.getClass().getName() + "] Growing tree: " + this);
		Vector3 min = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Vector3 max = new Vector3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		//Get a full list of all the members, including members in the children
		ArrayList<IGraphNodeMember> members = getAllMembersRecursively(true);
		int members_count = members.size();
		for (int i = 0; i < members_count; ++i) {
			IBoundingVolume volume = members.get(i).getTransformedBoundingVolume();
			Vector3 test_against_min = null;
			Vector3 test_against_max = null;
			if (volume == null) {
				ATransformable3D object = (ATransformable3D) members.get(i);
				test_against_min = object.getPosition();
				test_against_max = test_against_min;
			} else {
				//The order here is chosen to such that events which are more likely are
				//higher in the chain to avoid unnecessary checks.
				if (volume instanceof BoundingBox) {
					BoundingBox bb = (BoundingBox) volume;
					test_against_min = bb.getTransformedMin();
					test_against_max = bb.getTransformedMax();
				} else if (volume instanceof BoundingSphere) {
					BoundingSphere bs = (BoundingSphere) volume;
					Vector3 bs_position = bs.getPosition();
					float radius = bs.getScaledRadius();
					Vector3 rad = new Vector3();
					rad.setAll(radius, radius, radius);
					test_against_min = Vector3.subtractAndCreate(bs_position, rad);
					test_against_max = Vector3.addAndCreate(bs_position, rad);
				} else if (volume instanceof CameraFrustum) {
					CameraFrustum frustum = (CameraFrustum) volume;
					Vector3 far = frustum.getPlanePoint(6);
					Vector3 near = frustum.getPlanePoint(2);
					test_against_min = new Vector3(far.x, far.y, far.z);
					test_against_max = new Vector3(far.x, far.y, near.z);
				} else if (volume instanceof BoundingCone) {
					//TODO: Implement
				} else {
					//This is more to notify developers of a spot they need to expand. It should never
					//occur in production code.
					RajLog.e("[" + this.getClass().getName() + "] Received a bounding box of unknown type: " 
							+ volume.getClass().getCanonicalName());
					throw new IllegalArgumentException("Received a bounding box of unknown type."); 
				}
			}
			if (test_against_min != null && test_against_max != null) {
				if(test_against_min.x < min.x) min.x = test_against_min.x;
				if(test_against_min.y < min.y) min.y = test_against_min.y;
				if(test_against_min.z < min.z) min.z = test_against_min.z;
				if(test_against_max.x > max.x) max.x = test_against_max.x;
				if(test_against_max.y > max.y) max.y = test_against_max.y;
				if(test_against_max.z > max.z) max.z = test_against_max.z;
			}
		}
		mMin.setAll(min);
		mMax.setAll(max);
		mTransformedMin.setAll(min);
		mTransformedMax.setAll(max);
		calculatePoints();
		calculateChildSideLengths();
		if (mSplit) {
			for (int i = 0; i < CHILD_COUNT; ++i) {
				((Octree) mChildren[i]).setChildRegion(i, mChildLengths);
			}
		}
		for (int i = 0; i < members_count; ++i) {
			internalAddObject(members.get(i));
		}
	}

	/**
	 * Initializes the storage elements of the tree.
	 */
	protected abstract void init();

	/**
	 * Shrinks the tree. Should only be called by root node.
	 */
	protected void shrink() {
		if (mParent != null) {
			throw new IllegalStateException("Shrink can only be called by the root node.");
		}
		RajLog.d("[" + this.getClass().getName() + "] Checking if tree should be shrunk.");
		int maxCount = 0;
		int index_max = -1;
		for (int i = 0; i < CHILD_COUNT; ++i) { //For each child, get the object count and find the max
			if (mChildren[i].getObjectCount() > maxCount) {
				maxCount = mChildren[i].getObjectCount();
				index_max = i;
			}
		}
		if (index_max >= 0) {
			for (int i = 0; i < CHILD_COUNT; ++i) { //Validate shrink
				if (i == index_max) {
					continue;
				} else if (mChildren[i].getObjectCount() == maxCount) { 
					//If there are two+ children with the max count, shrinking doesnt make sense
					return;
				}
			}
			if ((getObjectCount() - maxCount) <= mShrinkThreshold) {
				RajLog.d("[" + this.getClass().getName() + "] Shrinking tree.");
				ArrayList<IGraphNodeMember> members = getAllMembersRecursively(true);
				int members_count = members.size();
				setBounds(index_max);
				if (mSplit) {
					for (int i = 0; i < CHILD_COUNT; ++i) { 
						//TODO: This is not always necessary depending on the object count, a GC improvement can be made here
						mChildren[i].destroy();
						mChildren[i] = null;
					}
					mSplit = false;
				}
				for (int i = 0; i < members_count; ++i) {
					shrinkAddObject(members.get(i));
				}
			}
		}
	}

	/**
	 * Determines if this node can be merged.
	 * 
	 * @return boolean indicating merge status.
	 */
	public boolean canMerge() {
		//Determine recursive member count
		int count = mMembers.size();
		if (mSplit) {
			for (int i = 0; i < CHILD_COUNT; ++i) {
				count += mChildren[i].mMembers.size();
			}
		}
		return (count <= mMergeThreshold);
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#clear()
	 */
	public void clear() {
		mMembers.clear();
		if (mParent == null) {
			mOutside.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#addObject(rajawali.scenegraph.IGraphNodeMember)
	 */
	public synchronized void addObject(IGraphNodeMember object) {
		RajLog.d("[" + this.getClass().getName() + "] Adding object: " + object + " to octree."); 
		List<IGraphNodeMember> members = null;
		if (mRecursiveAdd && object.hasChildMembers()) {
			members = object.getChildMembers();
			members.add(0, object);
		} else {
			members = new ArrayList<IGraphNodeMember>();
			members.add(object);
		}
		for (int i = 0, j = members.size(); i < j; ++i) {
			if (mParent == null) {
				//We are the root node
				if (getObjectCount() == 0) {
					//Set bounds based the incoming objects bounding box
					setBounds(members.get(i)); 
					addToMembers(members.get(i));
				} else {
					//Check if object is in bounds
					if (contains(object.getTransformedBoundingVolume())) {
						//The object is fully in bounds
						internalAddObject(members.get(i));
					} else {
						//The object is not in bounds or only partially in bounds
						//Add it to the outside container
						addToOutside(object);
						if (mOutside.size() >= mGrowThreshold) {
							grow();
						}
					}
				}
			} else {
				//We are a branch or leaf node
				internalAddObject(members.get(i));
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#addObjects(java.util.Collection)
	 */
	public void addObjects(Collection<IGraphNodeMember> objects) {
		//TODO: This could potentially be made more efficient
		if (mParent != null) {
			//Pass the call up to the root if we are not the root node
			mParent.addObjects(objects);
		} else {
			IGraphNodeMember[] object_array = new IGraphNodeMember[objects.size()];
			object_array = objects.toArray(object_array);
			for (int i = 0, j = object_array.length; i < j; ++i) {
				addObject(object_array[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#removeObject(rajawali.ATransformable3D)
	 */
	public synchronized void removeObject(IGraphNodeMember object) {
		RajLog.d("[" + this.getClass().getName() + "] Removing object: " + object + " from octree.");
		List<IGraphNodeMember> members = null;
		if (mRecursiveRemove && object.hasChildMembers()) {
			members = object.getChildMembers();
			members.add(0, object);
		} else {
			members = new ArrayList<IGraphNodeMember>();
			members.add(object);
		}
		for (int i = 0, j = members.size(); i < j; ++i) {
			//Retrieve the container object
			IGraphNode container = members.get(i).getGraphNode();
			if (container == null) {
				mOutside.remove(members.get(i));
			} else {
				if (container == this) {
					//If this is the container, process the removal
					//Remove the object from the members
					removeFromMembers(members.get(i));
					if (canMerge() && mParent != null) {
						//If we can merge, do it (if we are the root node, we can't)
						merge();
					}
				} else {
					//Defer the removal to the container
					container.removeObject(members.get(i));
				}
			}
			if (mParent == null && mSplit) shrink(); //Try to shrink the tree
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#removeObjects(java.util.Collection)
	 */
	public void removeObjects(Collection<IGraphNodeMember> objects) {
		//TODO: This could potentially be made more efficient
		if (mParent != null) {
			//Pass the call up to the root if we are not the root node
			mParent.removeObjects(objects);
		} else {
			IGraphNodeMember[] object_array = new IGraphNodeMember[objects.size()];
			object_array = objects.toArray(object_array);
			for (int i = 0, j = object_array.length; i < j; ++i) {
				removeObject(object_array[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#updateObject(rajawali.ATransformable3D)
	 */
	public synchronized void updateObject(IGraphNodeMember object) {
		/*RajLog.d("[" + this.getClass().getName() + "] Updating object: " + object + 
				"[" + object.getClass().getName() + "] in octree.");*/
		if (mParent == null && getObjectCount() == 1) { //If there is only one object, we should just follow it
			setBounds(object);			
			return;
		}
		IGraphNode container = object.getGraphNode(); //Get the container node
		Log.e("Rajawali", "Update called on node: " + this + " Object Container: " + container + " In graph? " + object.isInGraph());
		handleRecursiveUpdate((A_nAABBTree) container, object);
		Log.e("Rajawali", "After: " + this + " Object Container: " + object.getGraphNode() + " In graph? " + object.isInGraph());
		Log.e("Rajawali", "-------------------------------------------------------------------");
	}

	/**
	 * Handles the potentially recursive process of the update. Will determine which node
	 * the object is now within.
	 * 
	 * @param container A_nAABBTree instance which is the prior container.
	 * @param object IGraphNodeMember which is being updated.
	 */
	protected void handleRecursiveUpdate(final A_nAABBTree container, IGraphNodeMember object) {
		A_nAABBTree local_container = container;
		boolean updated = false;
		while (!updated) {
			if (local_container.contains(object.getTransformedBoundingVolume())) {
				int fits_in_child = -1;
				if (mSplit) {
					for (int j = 0; j < CHILD_COUNT; ++j) {
						if (mChildren[j].contains(object.getTransformedBoundingVolume())) {
							//If the member fits in this child, mark that child
							if (fits_in_child < 0) {
								fits_in_child = j;
							} else {
								//It fits in multiple children, leave it in parent
								fits_in_child = -1;
								break;
							}
						}
					}
					if (fits_in_child >= 0) { //If a single child was marked
						if (object.isInGraph()) {
							container.removeFromMembers(object); //First remove from the original container
						} else {
							container.mOutside.remove(object);
						}
						mChildren[fits_in_child].internalAddObject(object); //We want the child to check its children
						updated = true;
					} else {
						if (!object.isInGraph()) { //If we werent inside before, mark that we are now
							container.mOutside.remove(object);
							local_container.internalAddObject(object);
						}
						updated = true;
					}
				} else {
					if (local_container.equals(container)) {
						//We are dealing with the initial update
						if (!object.isInGraph()) {
							container.mOutside.remove(object);
							local_container.internalAddObject(object);
						}
					} else {
						//We are dealing with a recursive update
						container.removeFromMembers(object); //First remove from the original container
						local_container.internalAddObject(object); //Now add to the local container, which could be the root
					}
					updated = true;
				}
			} else { //If we are outside the container currently of interest
				if (local_container.mParent == null) { //If root node
					if (object.isInGraph()) { //If its in the graph, remove it to outside
						container.removeFromMembers(object); //First remove from the original container
						local_container.addToOutside(object);
					}//else nothing needs to be done
					updated = true;
				} else { //If container is not root node, pass the call up
					local_container = local_container.mParent;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#rebuild()
	 */
	public void rebuild() {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#addChildrenRecursively(boolean)
	 */
	public void addChildrenRecursively(boolean recursive) {
		mRecursiveAdd = recursive;
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#removeChildrenRecursively(boolean)
	 */
	public void removeChildrenRecursively(boolean recursive) {
		mRecursiveRemove = recursive;
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#cullFromBoundingVolume(rajawali.bounds.IBoundingVolume)
	 */
	public List<IGraphNodeMember> cullFromBoundingVolume(final IBoundingVolume volume, IGraphNode container) {
		Log.d("Culling", "Culling Volume: " + volume);
		ArrayList<IGraphNodeMember> survivors = new ArrayList<IGraphNodeMember>();
		ArrayList<A_nAABBTree> survivorNodes = new ArrayList<A_nAABBTree>();
		int start = 0;
		A_nAABBTree local_container = null;
		if (container == null) {
			Log.i("Culling", "Culling with null container");
			//Should only be called on the root node
			
			//The below if statement is choosing to pass this node in first since
			//we know it is of type BoundingBox...this is potentially marginally faster.
			if (BoundingVolumeTester.testIntersection(this, volume)) {
				//If the volume doesnt intersect with the root node there is nothing to do
				survivorNodes.add(this);
				local_container = this;
			}
		} else {
			Log.i("Culling", "Culling with specific container");
			survivorNodes.add((A_nAABBTree) container);
			local_container = (A_nAABBTree) container;
		}
		Log.i("Culling", "Survivor Nodes: " + survivorNodes);
		Log.i("Culling", "Local Container: " + local_container);
		start = local_container.getObjectCount();
		if (local_container.mSplit) {
			recursiveIntersectChildNodes(volume, local_container, survivorNodes);
		}
		for (int i = 0, j = survivorNodes.size(); i < j; ++i) {
			Log.v("Culling", "Culling for Node: " + survivorNodes.get(i));
			List<IGraphNodeMember> list = survivorNodes.get(i).mMembers;
			Log.v("Culling", "Node members: " + list);
			for (int n = 0, k = list.size(); n < k; ++n) {
				IBoundingVolume current_volume = list.get(n).getTransformedBoundingVolume();
				//The below if statement is choosing to pass the current volume in first since
				//we know odds are high it is of type BoundingBox...this is potentially marginally
				//faster.
				if (BoundingVolumeTester.testIntersection(current_volume, volume)) {
					survivors.add(list.get(n));
				}
			}
			list = survivorNodes.get(i).mOutside;
			Log.v("Culling", "Node outside: " + list);
			if (list != null) {
				for (int n = 0, k = list.size(); n < k; ++n) {
					IBoundingVolume current_volume = list.get(n).getTransformedBoundingVolume();
					Log.v("Culling", "Volume: " + current_volume);
					//The below if statement is choosing to pass the current volume in first since
					//we know odds are high it is of type BoundingBox...this is potentially marginally
					//faster.
					if (BoundingVolumeTester.testIntersection(current_volume, volume)) {
						survivors.add(list.get(n));
					}
				}
			}
		}
		int end = survivors.size();
		Log.v("Culling", "Survivors: " + end + "/" + start);
		return survivors;
	}
	
	/**
	 * Recursively checks child nodes and adds any survivors to the list.
	 * 
	 * @param volume {@link IBoundingVolume} to check for intersection with.
	 * @param container {@link A_nAABBTree} who's children should be checked.
	 * @param survivors {@link List} of {@link A_nAABBTree} objects which have survived the intersection test.
	 */
	private void recursiveIntersectChildNodes(IBoundingVolume volume, A_nAABBTree container, List<A_nAABBTree> survivors) {
		Log.i("Culling", "Recursively checking child nodes.");
		for (int i = 0, j = container.CHILD_COUNT; i < j; ++i) {
			//The below if statement is choosing to pass the container in first since
			//we know the container is of type BoundingBox...this is potentially marginally
			//faster.
			if (BoundingVolumeTester.testIntersection(container.mChildren[i], volume)) {
				survivors.add(container.mChildren[i]);
				recursiveIntersectChildNodes(volume, container, survivors);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#displayGraph(boolean)
	 */
	public void displayGraph(Camera camera, float[] vpMatrix, float[] projMatrix, float[] vMatrix) {
		Matrix.setIdentityM(mMMatrix, 0);
		drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, null);
		for (int i = 0, j = mMembers.size(); i < j; ++i) {
			IBoundingVolume volume = mMembers.get(i).getTransformedBoundingVolume();
			volume.drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, null);
		}
		if (mParent == null) {
			for (int i = 0, j = mOutside.size(); i < j; ++i) {
				IBoundingVolume volume = mOutside.get(i).getTransformedBoundingVolume();
				volume.drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, null);
			}
		}
		if (mSplit) {
			for (int i = 0; i < CHILD_COUNT; ++i) {
				mChildren[i].displayGraph(camera, vpMatrix, projMatrix, vMatrix);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#getSceneMinBound()
	 */
	public Vector3 getSceneMinBound() {
		return getTransformedMin();
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#getSceneMaxBound()
	 */
	public Vector3 getSceneMaxBound() {
		return getTransformedMax();
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#getObjectCount()
	 */
	public int getObjectCount() {
		int count = mMembers.size();
		if (mParent == null) {
			count += mOutside.size();
		}
		if (mSplit) {
			for (int i = 0; i < CHILD_COUNT; ++i) {
				count += mChildren[i].getObjectCount();
			}
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#contains(rajawali.bounds.IBoundingVolume)
	 */
	public boolean contains(IBoundingVolume boundingVolume) {
		//The order here is chosen to such that events which are more likely are
		//higher in the chain to avoid unnecessary checks.
		if (boundingVolume instanceof BoundingBox) {
			BoundingBox boundingBox = (BoundingBox)boundingVolume;
			Vector3 otherMin = boundingBox.getTransformedMin();
			Vector3 otherMax = boundingBox.getTransformedMax();
			Vector3 min = mTransformedMin;
			Vector3 max = mTransformedMax;		
			return (max.x >= otherMax.x) && (min.x <= otherMin.x) &&
					(max.y >= otherMax.y) && (min.y <= otherMin.y) &&
					(max.z >= otherMax.z) && (min.z <= otherMin.z);
		} else if (boundingVolume instanceof BoundingSphere) {
			//TODO: Implement
			return false;
		} else if (boundingVolume instanceof CameraFrustum) {
			//TODO: Implement
			return false;
		} else if (boundingVolume instanceof BoundingCone) {
			//TODO: Implement
			return false;
		} else {
			//This is more to notify developers of a spot they need to expand. It should never
			//occur in production code.
			RajLog.e("[" + this.getClass().getName() + "] Received a bounding box of unknown type: " 
					+ boundingVolume.getClass().getCanonicalName());
			throw new IllegalArgumentException("Received a bounding box of unknown type."); 
		}
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#isContainedBy(rajawali.bounds.IBoundingVolume)
	 */
	public boolean isContainedBy(IBoundingVolume boundingVolume) {
		//The order here is chosen to such that events which are more likely are
		//higher in the chain to avoid unnecessary checks.
		if (boundingVolume instanceof BoundingBox) {
			BoundingBox boundingBox = (BoundingBox)boundingVolume;
			Vector3 otherMin = boundingBox.getTransformedMin();
			Vector3 otherMax = boundingBox.getTransformedMax();
			Vector3 min = mTransformedMin;
			Vector3 max = mTransformedMax;		
			return (max.x <= otherMax.x) && (min.x >= otherMin.x) &&
					(max.y <= otherMax.y) && (min.y >= otherMin.y) &&
					(max.z <= otherMax.z) && (min.z >= otherMin.z);
		} else if (boundingVolume instanceof BoundingSphere) {
			//TODO: Implement
			return false;
		} else if (boundingVolume instanceof CameraFrustum) {
			//TODO: Implement
			return false;
		} else if (boundingVolume instanceof BoundingCone) {
			//TODO: Implement
			return false;
		} else {
			//This is more to notify developers of a spot they need to expand. It should never
			//occur in production code.
			RajLog.e("[" + this.getClass().getName() + "] Received a bounding box of unknown type: " 
					+ boundingVolume.getClass().getCanonicalName());
			throw new IllegalArgumentException("Received a bounding box of unknown type."); 
		}
	}

	@Override
	public String toString() {
		String str = "A_nAABBTree (" + mLevel + "): " + mChildRegion + " member/outside count: " + mMembers.size() + "/";
		if (mParent == null) {
			str = str + mOutside.size();
		} else {
			str = str + "NULL";
		}
		return str;
	}
}
