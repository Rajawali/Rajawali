package rajawali.animation.mesh;

public class BoneAnimationSequence implements IAnimationSequence {
	private BoneAnimationFrame[] mFrames;
	private float[] mFrameData;
	private String mName;
	private int mNumFrames;
	private int mFrameRate;
	
	public BoneAnimationSequence(String name)
	{
		mName = name;
	}

	public void setFrames(BoneAnimationFrame[] frames)
	{
		mFrames = frames;
	}
	
	public BoneAnimationFrame[] getFrames()
	{
		return mFrames;
	}
	
	public BoneAnimationFrame getFrame(int index)
	{
		return mFrames[index];
	}
	
	public void setFrameData(float[] frameData)
	{
		mFrameData = frameData;
	}
	
	public float[] getFrameData()
	{
		return mFrameData;
	}
	
	public void setName(String name)
	{
		mName = name;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setNumFrames(int numFrames)
	{
		mNumFrames = numFrames;
	}
	
	public int getNumFrames()
	{
		return mNumFrames;
	}
	
	public void setFrameRate(int frameRate)
	{
		mFrameRate = frameRate;
	}
	
	public int getFrameRate()
	{
		return mFrameRate;
	}
}
