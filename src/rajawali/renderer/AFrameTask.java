/**
 * 
 */
package rajawali.renderer;



/**
 * This is an abstract class to extend for things which need to have
 * their addition/removal/modification in the scene properly regulated
 * by the {@link RajawaliRenderer} and OpenGL thread. 
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public abstract class AFrameTask {
	
	/**
	 * Which task needs to be performed. Can be one of 
	 * NONE, ADD, REMOVE, REMOVE_ALL or REPLACE.
	 * 
	 * <p>NONE - Do nothing. <br>
	 * ADD - Adds an object to the related list. If an 
	 * index is specified. <br>
	 * REMOVE - Removes an object from the related list. 
	 * If object is null, an index must be specified.<br>
	 * REMOVE_ALL - Removes all objects from the related list.<br>
	 * REPLACE - Replaces an object in the related list 
	 * at the specified index.</p>
	 */
	public enum TASK {NONE, ADD, REMOVE, REMOVE_ALL, REPLACE};
	
	/**
	 * The type of object this task is acting on.
	 */
	public enum TYPE {ANIMATION, CAMERA, LIGHT, OBJECT3D, PLUGIN, TEXTURE};
	
	private AFrameTask.TASK mFrameTask = AFrameTask.TASK.NONE; //The task to perform
	private int mFrameTaskIndex = UNUSED_INDEX; //The index to replace, if relevant
	
	static final int UNUSED_INDEX = -1;
	
	/**
	 * Gets the type of object this task acts on.
	 */
	public abstract TYPE getFrameTaskType();
	
	/**
	 * Retrieves the task to be performed.
	 * 
	 * @return {@link AFrameTask.TASK} to be performed.
	 */
	public AFrameTask.TASK getTask() {
		return mFrameTask;
	}
	
	/**
	 * Sets the task to be performed.
	 * 
	 * @param task {@AFrameTask.TASK} to be performed.
	 */
	void setTask(AFrameTask.TASK task) {
		mFrameTask = task;
	}
	
	/**
	 * Gets the index this task acts on.
	 * 
	 * @return int The index.
	 */
	public int getIndex() {
		return mFrameTaskIndex;
	}
	
	/**
	 * Sets the index this task acts on.
	 * 
	 * @param int The index.
	 */
	void setIndex(int index) {
		mFrameTaskIndex = index;
	}
}