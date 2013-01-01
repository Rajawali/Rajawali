package rajawali.animation;

import java.util.Stack;
import java.util.Timer;

public class TimerManager {
	private static final ThreadLocal<TimerManager> mThreadLocalManager =
		new ThreadLocal<TimerManager>();
	private Stack<Timer> mTimers;
	
	private TimerManager() {
		mTimers = new Stack<Timer>();
	}
	
	public Timer createNewTimer() {
		Timer timer = new Timer();
		mTimers.add(timer);
		return timer;
	}
	
	public void killTimer(Timer timer) {
		timer.cancel();
		timer.purge();
		mTimers.remove(timer);
	}
	
	public void clear() {
		for(int i=0; i<mTimers.size(); ++i) {
			mTimers.get(i).cancel();
			mTimers.get(i).purge();
		}
		mTimers.clear();
		mThreadLocalManager.remove();
	}
	
	public static TimerManager getInstance()
	{
		TimerManager instance = mThreadLocalManager.get();
		if(instance == null)
		{
			instance = new TimerManager();
			mThreadLocalManager.set(instance);
		}

		return instance;
	}
}
