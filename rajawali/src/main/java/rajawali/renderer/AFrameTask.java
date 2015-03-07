/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
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
	 * NONE, ADD, REMOVE, REMOVE_ALL, REPLACE, RESET, RELOAD or INITIALIZE.
	 * 
	 * <p>NONE - Do nothing. <br>
	 * ADD - Adds an object to the related list. If an 
	 * index is specified. <br>
	 * ADD_ALL - Adds all objects from a collection to the
	 * related list.<br>
	 * REMOVE - Removes an object from the related list. 
	 * If object is null, an index must be specified.<br>
	 * REMOVE_ALL - Removes all objects from the related list, or
	 * if a collection is specified, all the matching objects.<br>
	 * REPLACE - Replaces an object in the related list 
	 * at the specified index.<br>
	 * RESET - Resets objects to their initial state.<br>
	 * RELOAD - Reloads objects.<br>
	 * INITIALIZE - Initializes objects.<br>
	 * </p>
	 */
	public enum TASK {NONE, ADD, ADD_ALL, REMOVE, REMOVE_ALL, REPLACE, RESET, RELOAD, INITIALIZE};
	
	/**
	 * The type of object this task is acting on.
	 */
	public enum TYPE {ANIMATION, CAMERA, LIGHT, OBJECT3D, PLUGIN, TEXTURE, SCENE, TEXTURE_MANAGER, COLOR_PICKER, MATERIAL, MATERIAL_MANAGER, RENDER_TARGET, EFFECT};
	
	private AFrameTask.TASK mFrameTask = AFrameTask.TASK.NONE; //The task to perform
	private int mFrameTaskIndex = UNUSED_INDEX; //The index to replace, if relevant
	private AFrameTask mNewObject; //The AFrameTask object to replace if used
	
	public static final int UNUSED_INDEX = -1;
	
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
	public void setTask(AFrameTask.TASK task) {
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
	public void setIndex(int index) {
		mFrameTaskIndex = index;
	}
	
	/**
	 * Gets the {@link AFrameTask} object this one replaces.
	 * 
	 * @return {@link AFrameTask} which needs to be replaced.
	 */
	public AFrameTask getNewObject() {
		return mNewObject;
	}
	
	/**
	 * Sets the replacement.
	 * 
	 * @param object {@link AFrameTask} object which will replace this one.
	 */
	public void setNewObject(AFrameTask object) {
		mNewObject = object;
	}
}