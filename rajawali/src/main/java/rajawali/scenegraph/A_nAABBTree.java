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
package rajawali.scenegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rajawali.ATransformable3D;
import rajawali.Camera;
import rajawali.bounds.BoundingBox;
import rajawali.bounds.BoundingSphere;
import rajawali.bounds.IBoundingVolume;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.util.RajLog;
import android.util.Log;

/**
 * Generic Axis Aligned Bounding Box based tree sorting hierarchy. Subclasses
 * are left to determine child count and any count specific behavior. This implementation
 * in general uses the methodology described in the tutorial listed below by Paramike, with
 * a few modifications to the behavior. 
 * 
 * Implementations of this could be Ternary trees (3), Octree (8), Icoseptree (27), etc.
 * The tree will try to nest objects as deeply as possible while trying to maintain an minimal
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

	//Expected to never leave its default identity state.
	protected final Matrix4 mMMatrix = new Matrix4(); //A model matrix to use for drawing the bounds of this node.
	protected final Vector3 mPosition = new Vector3(); //This node's center point in 3D space.

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
		BoundingBox bcube = null;
		BoundingSphere bsphere = null;
		Vector3 position = member.getScenePosition();
		double span_y = 0;
		double span_x = 0;
		double span_z = 0;
		if (volume == null) {
			span_x = 5.0;
			span_y = 5.0;
			span_z = 5.0;
		} else {
			if (volume instanceof BoundingBox) {
				bcube = (BoundingBox) volume;
				Vector3 min = bcube.getTransformedMin();
				Vector3 max = bcube.getTransformedMax();
				span_x = (max.x - min.x);
				span_y = (max.y - min.y);
				span_z = (max.z - min.z);
			} else if (volume instanceof BoundingSphere) {
				bsphere = (BoundingSphere) volume;
				span_x = 2.0*bsphere.getScaledRadius();
				span_y = span_x;
				span_z = span_x;
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
				if (volume instanceof BoundingBox) {
					BoundingBox bb = (BoundingBox) volume;
					test_against_min = bb.getTransformedMin();
					test_against_max = bb.getTransformedMax();
				} else if (volume instanceof BoundingSphere) {
					BoundingSphere bs = (BoundingSphere) volume;
					Vector3 bs_position = bs.getPosition();
					double radius = bs.getScaledRadius();
					Vector3 rad = new Vector3();
					rad.setAll(radius, radius, radius);
					test_against_min = Vector3.subtractAndCreate(bs_position, rad);
					test_against_max = Vector3.addAndCreate(bs_position, rad);
				} else {
					RajLog.e("[" + this.getClass().getName() + "] Received a bounding box of unknown type.");
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
		//TODO: Handle recursive add posibility

		if (mParent == null) {
			//We are the root node
			mBoundingColor.set(0xFFFF0000);
			if (getObjectCount() == 0) {
				//Set bounds based the incoming objects bounding box
				setBounds(object); 
				addToMembers(object);
			} else {
				//Check if object is in bounds
				if (contains(object.getTransformedBoundingVolume())) {
					//The object is fully in bounds
					internalAddObject(object);
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
			internalAddObject(object);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#addObjects(java.util.Collection)
	 */
	public void addObjects(Collection<IGraphNodeMember> objects) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#removeObject(rajawali.ATransformable3D)
	 */
	public synchronized void removeObject(IGraphNodeMember object) {
		RajLog.d("[" + this.getClass().getName() + "] Removing object: " + object + " from octree.");
		//TODO: Handle recursive add posibility
		//Retrieve the container object
		IGraphNode container = object.getGraphNode();
		if (container == null) {
			mOutside.remove(object);
		} else {
			if (container == this) {
				//If this is the container, process the removal
				//Remove the object from the members
				removeFromMembers(object);
				if (canMerge() && mParent != null) {
					//If we can merge, do it (if we are the root node, we can't)
					merge();
				}
			} else {
				//Defer the removal to the container
				container.removeObject(object);
			}
		}
		if (mParent == null && mSplit) shrink(); //Try to shrink the tree
	}
	
	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#removeObjects(java.util.Collection)
	 */
	public void removeObjects(Collection<IGraphNodeMember> objects) {
		// TODO Auto-generated method stub
		
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
		handleRecursiveUpdate((A_nAABBTree) container, object);
		RajLog.e("Rajawali", "Node: " + this + " Object Container: " + container);
	}

	/**
	 * Handles the potentially recursive process of the update. Will determine which node
	 * the object is now within.
	 * 
	 * @param container A_nAABBTree instance which is the prior container.
	 * @param object IGraphNodeMember which is being updated.
	 */
	protected void handleRecursiveUpdate(final A_nAABBTree container, IGraphNodeMember object) {
		//RajLog.i("Rajawali", "Handling recursive update potential.");
		A_nAABBTree local_container = container;
		boolean updated = false;
		while (!updated) {
			if (local_container.contains(object.getTransformedBoundingVolume())) {
				RajLog.v("Rajawali", "INSIDE");
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
						RajLog.i("Rajawali", "Fits in a single child.");
						local_container.removeFromMembers(object);
						mChildren[fits_in_child].internalAddObject(object);
						updated = true;
					} else { //TODO: WORKS
						RajLog.i("Rajwali", "Fits in multiple children, leaving in place.");
						updated = true;
					}
				} else {
					RajLog.i("Rajawali", "No children so we are leaving in same node.");
					if (!object.isInGraph()) {
						RajLog.i("Rajawali", "Removing from outside graph and moving to inside root.");
						local_container.mOutside.remove(object);
						local_container.internalAddObject(object);
					}
					updated = true;
				}
			} else {
				//RajLog.v("Rajawali", "OUTSIDE");
				if (local_container.mParent == null) { //TODO: WORKS
					//RajLog.v("Rajawali", "OUTSIDE");
					//RajLog.i("Rajawali", "Container is root node. Adding to outside.");
					if (object.isInGraph()) {
						local_container.removeFromMembers(object);
						local_container.addToOutside(object);
					}
					//RajLog.e("Rajawali", "Node after addToOutside: " + object.getGraphNode());
					updated = true;
				} else {
					RajLog.i("Rajawali", "Container is not root (" + local_container + "). Moving search up a level.");
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
	public void cullFromBoundingVolume(IBoundingVolume volume) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#displayGraph(boolean)
	 */
	public void displayGraph(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix) {
		drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, mMMatrix);
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
		if(!(boundingVolume instanceof BoundingBox)) return false;
		BoundingBox boundingBox = (BoundingBox)boundingVolume;
		Vector3 otherMin = boundingBox.getTransformedMin();
		Vector3 otherMax = boundingBox.getTransformedMax();
		Vector3 min = mTransformedMin;
		Vector3 max = mTransformedMax;		

		return (max.x >= otherMax.x) && (min.x <= otherMin.x) &&
				(max.y >= otherMax.y) && (min.y <= otherMin.y) &&
				(max.z >= otherMax.z) && (min.z <= otherMin.z);
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.IGraphNode#isContainedBy(rajawali.bounds.IBoundingVolume)
	 */
	public boolean isContainedBy(IBoundingVolume boundingVolume) {
		if(!(boundingVolume instanceof BoundingBox)) return false;
		BoundingBox boundingBox = (BoundingBox)boundingVolume;
		Vector3 otherMin = boundingBox.getTransformedMin();
		Vector3 otherMax = boundingBox.getTransformedMax();
		Vector3 min = mTransformedMin;
		Vector3 max = mTransformedMax;		

		return (max.x <= otherMax.x) && (min.x >= otherMin.x) &&
				(max.y <= otherMax.y) && (min.y >= otherMin.y) &&
				(max.z <= otherMax.z) && (min.z >= otherMin.z);
	}

	@Override
	public String toString() {
		String str = "A_nAABBTree: " + mChildRegion + " member/outside count: " + mMembers.size() + "/";
		if (mParent == null) {
			str = str + mOutside.size();
		} else {
			str = str + "NULL";
		}
		return str;
	}
}
