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
package org.rajawali3d.geometry;

import android.opengl.GLES20;
import android.support.annotation.IntDef;
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

    public static final int FLOAT_BUFFER = 0;
    public static final int INT_BUFFER = 1;
    public static final int SHORT_BUFFER = 2;
    public static final int BYTE_BUFFER = 3;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLOAT_BUFFER, INT_BUFFER, SHORT_BUFFER, BYTE_BUFFER})
    @interface BufferType {
    }

    public int rajawaliHandle = -1;
    public int glHandle       = -1;
    public @BufferType int bufferType;
    public Buffer     buffer;
    public int        target;
    public int        byteSize;
    public int        usage;
    public int stride = 0;
    public int offset = 0;
    public int type   = GLES20.GL_FLOAT;

    public BufferInfo() {
        usage = GLES20.GL_STATIC_DRAW;
    }

    public BufferInfo(@NonNull @BufferType int bufferType, @NonNull Buffer buffer) {
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
                .append(" byteSize: ").append(byteSize)
                .append(" usage: ").append(usage);
        return sb.toString();
    }
}