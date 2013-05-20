package rajawali.animation.mesh;

public class SkeletalAnimationSequence implements IAnimationSequence {
	private SkeletalAnimationFrame[] mFrames;
	private float[] mFrameData;
	private String mName;
	private int mNumFrames;
	private int mFrameRate;
	
	public SkeletalAnimationSequence(String name)
	{
		mName = name;
	}

	public void setFrames(SkeletalAnimationFrame[] frames)
	{
		mFrames = frames;
	}
	
	public SkeletalAnimationFrame[] getFrames()
	{
		return mFrames;
	}
	
	public SkeletalAnimationFrame getFrame(int index)
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
