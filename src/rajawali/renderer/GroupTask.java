package rajawali.renderer;

import java.util.Collection;


public final class GroupTask extends AFrameTask {

	private final AFrameTask.TYPE mType;
	private final Collection<AFrameTask> mCollection;
	
	public GroupTask(AFrameTask.TYPE type) {
		mType = type;
		mCollection = null;
	}
	
	public GroupTask(Collection<AFrameTask> collection) {
		mType = null;
		mCollection = collection;
	}
	
	@Override
	public TYPE getFrameTaskType() {
		return mType;
	}

	public Collection<AFrameTask> getCollection() {
		return mCollection;
	}
}
