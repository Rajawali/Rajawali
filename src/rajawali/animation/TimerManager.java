package rajawali.animation;

import java.util.Stack;
import java.util.Timer;

public class TimerManager {
	private static TimerManager mInstance;
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
	}
	
	public static TimerManager getInstance()
	{
		if(mInstance == null) mInstance = new TimerManager();
		return mInstance;
	}
}
