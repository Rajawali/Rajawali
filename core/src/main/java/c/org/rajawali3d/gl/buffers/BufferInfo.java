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
package c.org.rajawali3d.gl.buffers;

import android.opengl.GLES20;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import net.jcip.annotations.NotThreadSafe;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.Buffer;

/**
 * Wrapper for Vertex Buffer Object data. This class is not thread safe.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SuppressWarnings("WeakerAccess")
@NotThreadSafe
// TODO: These should probably be private with accessors instead
public class BufferInfo {

    public static final int BYTE_BUFFER   = 0;
    public static final int FLOAT_BUFFER  = 1;
    public static final int DOUBLE_BUFFER = 2;
    public static final int SHORT_BUFFER  = 3;
    public static final int INT_BUFFER    = 4;
    public static final int LONG_BUFFER   = 5;
    public static final int CHAR_BUFFER   = 6;

    @NonNull
    public BufferInfo copyWithKey(@IntRange(from = 0) int key) {
        final BufferInfo clone = new BufferInfo(bufferType, buffer);
        clone.target = target;
        clone.glHandle = glHandle;
        clone.rajawaliHandle = key;
        clone.elementSize = elementSize;
        clone.usage = usage;
        clone.offset = offset;
        clone.stride = stride;
        clone.count = count;
        clone.type = type;
        return clone;
    }

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ BYTE_BUFFER, FLOAT_BUFFER, DOUBLE_BUFFER, SHORT_BUFFER, INT_BUFFER, LONG_BUFFER, CHAR_BUFFER })
    public @interface BufferType {}


    public int rajawaliHandle = -1;
    public int glHandle       = -1;
    public @BufferType   int    bufferType;
    public               Buffer buffer; //TODO: Does it make sense to let this be nullable?
    public @BufferTarget int    target;
    public               int    elementSize;
    public @BufferUsage int usage  = GLES20.GL_STATIC_DRAW;
    public              int stride = 0; // How far to jump to the next vertex
    public int count = 1; // The number of values per vertex
    public              int offset = 0; // How far into the buffer to jump to the first vertex
    public              int type   = GLES20.GL_FLOAT;

    public BufferInfo() {
    }

    public BufferInfo(@BufferType int bufferType, @NonNull Buffer buffer) {
        this.bufferType = bufferType;
        this.buffer = buffer;
    }

    @NonNull
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Key: ").append(rajawaliHandle)
                .append(" Handle: ").append(glHandle)
                .append(" type: ").append(bufferType)
                .append(" target: ").append(target)
                .append(" elementSize: ").append(elementSize)
                .append(" usage: ").append(usage);
        return sb.toString();
    }
}