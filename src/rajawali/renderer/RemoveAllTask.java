package rajawali.renderer;


public final class RemoveAllTask extends AFrameTask {

	private final AFrameTask.TYPE mType; 
	
	public RemoveAllTask(AFrameTask.TYPE type) {
		mType = type;
	}
	
	@Override
	public TYPE getFrameTaskType() {
		return mType;
	}

}
