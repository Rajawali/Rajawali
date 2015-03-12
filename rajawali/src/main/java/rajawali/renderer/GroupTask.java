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
