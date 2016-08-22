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
package org.rajawali3d;

import android.opengl.GLES20;
import org.rajawali3d.Geometry3D.BufferType;

import java.nio.Buffer;

public class BufferInfo {
	public int rajawaliHandle = -1;
	public int bufferHandle = -1;
	public BufferType bufferType;
	public Buffer buffer;
	public int target;
	public int byteSize;
	public int usage;
    public int stride = 0;
    public int offset = 0;
    public int type = GLES20.GL_FLOAT;

	public BufferInfo() {
		this.usage = GLES20.GL_STATIC_DRAW;
	}

	public BufferInfo(BufferType bufferType, Buffer buffer) {
		this.bufferType = bufferType;
		this.buffer = buffer;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb
				.append("Key: ").append(rajawaliHandle)
			.append(" Handle: ").append(bufferHandle)
			.append(" type: ").append(bufferType)
			.append(" target: ").append(target)
			.append(" byteSize: ").append(byteSize)
			.append(" usage: ").append(usage);
		return sb.toString();
	}
}