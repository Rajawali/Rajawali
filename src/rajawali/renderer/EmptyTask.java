package rajawali.renderer;


public final class EmptyTask extends AFrameTask {

	private final AFrameTask.TYPE mType;
	
	public EmptyTask(AFrameTask.TYPE type) {
		mType = type;
	}

	@Override
	public AFrameTask.TYPE getFrameTaskType() {
		return mType;
	}
}
