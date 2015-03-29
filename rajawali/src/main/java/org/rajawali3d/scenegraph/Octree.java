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

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.RajLog;


/**
 * Octree implementation specific to the Rajawali library.
 * 
 * Child partitions will inherit the behavior (recursive add, division threshold, etc.)
 * of the root node in the graph.
 * 
 * This system divides space into 8 equal portions called octants.
 * 
 * The octant order follows the conventional algebraic numbering
 * for 3D Euclidean space. Note that they follow the axis ordering
 * and OpenGL uses a rotated coordinate system when compared to 
 * Euclidean mathematics. Thus, assuming no camera rotation or similar
 * effects:
 * @see <a href="http://en.wikipedia.org/wiki/Octant_(solid_geometry)">
	 http://en.wikipedia.org/wiki/Octant_(solid_geometry)</a>
 * <pre> 
 *     Octant     | Screen Region
 * ---------------|---------------
 *       0        | +X/+Y/+Z
 *       1        | -X/+Y/+Z 
 *       2        | -X/-Y/+Z
 *       3        | +X/-Y/+Z
 *       4        | +X/+Y/-Z
 *       5        | -X/+Y/-Z
 *       6        | -X/-Y/-Z
 *       7        | +X/-Y/-Z
 * </pre>
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class Octree extends A_nAABBTree {

	protected static final int[] COLORS = new int[]{
		0xFF8A2BE2, 0xFF0000FF, 0xFFD2691E, 0xFF008000,
		0xFFD2B000, 0xFF00FF00, 0xFFFF00FF, 0xFF40E0D0
	};

	/**
	 * Default constructor. Initializes the root node with default merge/division
	 * behavior.
	 */
	public Octree() {
		super();
		init();
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
	 * would ordinarily span a boundry.
	 */
	public Octree(int mergeThreshold, int splitThreshold, int shrinkThreshold, int growThreshold, int overlap) {
		super(mergeThreshold, splitThreshold, shrinkThreshold, growThreshold, overlap);
	}

	/**
	 * Constructor to setup a child node with specified merge/split and 
	 * grow/shrink behavior.
	 * 
	 * @param parent Octree which is the parent of this partition.
	 * @param maxMembers int containing the divide threshold count. When more 
	 * members than this are added, a partition will divide into 8 children.
	 * @param minMembers int containing the merge threshold count. When fewer
	 * members than this exist, a partition will recursively merge to its ancestors.
	 * @param overlap int containing the percentage overlap between two adjacent
	 * partitions. This allows objects to be nested deeper in the tree when they
	 * would ordinarily span a boundry.
	 */
	public Octree(Octree parent, int mergeThreshold, int splitThreshold, int shrinkThreshold, int growThreshold, int overlap) {
		super(parent, mergeThreshold, splitThreshold, shrinkThreshold, growThreshold, overlap);
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.AD_AABBTree#init()
	 */
	@Override
	protected void init() {
		//Pre-allocate storage here to favor modification speed
		CHILD_COUNT = 8;
		mChildren = new Octree[CHILD_COUNT];
		mMembers = Collections.synchronizedList(new CopyOnWriteArrayList<IGraphNodeMember>());
		if (mParent == null) //mOutside should not be used for children, thus we want to force the Null pointer.
			mOutside = Collections.synchronizedList(new CopyOnWriteArrayList<IGraphNodeMember>());
		mChildLengths = new Vector3();
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.A_nAABBTree#setChildRegion(int, rajawali.math.Number3D)
	 */
	@Override
	protected void setChildRegion(int octant, Vector3 side_lengths) {
		mChildRegion = octant;
		Vector3 min = mParent.getMin();
		Vector3 max = mParent.getMax();
		switch (mChildRegion) {
		case 0: //+X/+Y/+Z
			mMax.setAll(mParent.getMax());
			mMin.subtractAndSet(mMax, side_lengths);
			break;
		case 1: //-X/+Y/+Z 
			mMax.x = min.x + side_lengths.x;
			mMax.y = max.y;
			mMax.z = max.z;
			mMin.x = min.x;
			mMin.y = max.y - side_lengths.y;
			mMin.z = max.z - side_lengths.z;
			break;
		case 2: //-X/-Y/+Z
			mMax.x = min.x + side_lengths.x;
			mMax.y = min.y + side_lengths.y;
			mMax.z = max.z;
			mMin.x = min.x;
			mMin.y = min.y;
			mMin.z = max.z - side_lengths.z;
			break;
		case 3: //+X/-Y/+Z
			mMax.x = max.x;
			mMax.y = min.y + side_lengths.y;
			mMax.z = max.z;
			mMin.x = max.x - side_lengths.x;
			mMin.y = min.y;
			mMin.z = max.z - side_lengths.z;
			break;
		case 4: //+X/+Y/-Z
			mMax.x = max.x;
			mMax.y = max.y;
			mMax.z = min.z + side_lengths.z;
			mMin.x = max.x - side_lengths.x;
			mMin.y = max.y - side_lengths.y;
			mMin.z = min.z;
			break;
		case 5: //-X/+Y/-Z
			mMax.x = min.x + side_lengths.x;
			mMax.y = max.y;
			mMax.z = min.z + side_lengths.z;
			mMin.x = min.x;
			mMin.y = max.y - side_lengths.y;
			mMin.z = min.z;
			break;
		case 6: //-X/-Y/-Z
			mMin.setAll(min);
			mMax.addAndSet(mMin, side_lengths);
			break;
		case 7: //+X/-Y/-Z
			mMax.x = max.x;
			mMax.y = min.y + side_lengths.y;
			mMax.z = min.z + side_lengths.z;
			mMin.x = max.x - side_lengths.x;
			mMin.y = min.y;
			mMin.z = min.z;
			break;
		default:
			return;
		}
		super.setChildRegion(octant, side_lengths);
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.A_nAABBTree#destroy()
	 */
	@Override
	protected void destroy() {
		RajLog.d("[" + this.getClass().getName() + "] Destroying octree node: " + this);
		//TODO: Implement
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.scenegraph.A_nAABBTree#split()
	 */
	@Override
	protected void split() {
		RajLog.d("[" + this.getClass().getName() + "] Spliting node: " + this);
		//Populate child array
		for (int i = 0; i < CHILD_COUNT; ++i) {
			if (mChildren[i] == null) {
				mChildren[i] = new Octree(this, mMergeThreshold,
						mSplitThreshold, mShrinkThreshold, mGrowThreshold, mOverlap);
			}
			mChildren[i].setBoundingColor(COLORS[i]);
			mChildren[i].setChildRegion(i, mChildLengths);
		}
		super.split();
	}

	@Override
	public String toString() {
		String str = "Octant: " + mChildRegion + " member/outside count: " + mMembers.size() + "/";
		if (mParent == null) {
			str = str + mOutside.size();
		} else {
			str = str + "NULL";
		}
		return str;
	}
}