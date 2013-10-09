package rajawali.postprocessing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;



public abstract class APostProcessingEffect implements IPostProcessingEffect {
	protected List<IPass> mPasses;
	protected PostProcessingManager mManager;
	
	public void addPass(IPass pass) {
		if(mPasses == null)
			mPasses = Collections.synchronizedList(new CopyOnWriteArrayList<IPass>());
		mPasses.add(pass);
		mManager.setComponentsDirty();
	}

	public void insertPass(int index, IPass pass) {
		mPasses.add(index, pass);
		mManager.setComponentsDirty();
	}

	public void removePass(IPass pass) {
		mPasses.remove(pass);
		mManager.setComponentsDirty();
	}
	
	public void setManager(PostProcessingManager manager)
	{
		mManager = manager;
	}
}
