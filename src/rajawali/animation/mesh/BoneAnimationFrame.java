package rajawali.animation.mesh;

import rajawali.Geometry3D;
import rajawali.bounds.BoundingBox;
import rajawali.math.Number3D;

public class BoneAnimationFrame implements IAnimationFrame {
	private String mName;
	private BoundingBox mBounds;
	private Skeleton mSkeleton;
	private int mFrameIndex;
	
	public BoneAnimationFrame() {
		mBounds = new BoundingBox();
		mSkeleton = new Skeleton();
	}
	
	@Override
	public Geometry3D getGeometry() {
		return null;
	}

	@Override
	public void setGeometry(Geometry3D geometry) {
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public void setName(String name) {
		mName = name;
	}
	
	public void setBounds(Number3D min, Number3D max) {
		mBounds.setMin(min);
		mBounds.setMax(max);
	}
	
	public BoundingBox getBoundingBox() {
		return mBounds;
	}
	
	public void setFrameIndex(int index) {
		mFrameIndex = index;
	}
	
	public int getFrameIndex() {
		return mFrameIndex;
	}
	
	public Skeleton getSkeleton() {
		return mSkeleton;
	}
}
