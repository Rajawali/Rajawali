/**
 * Copyright 2017 Dennis Ippel
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
package c.org.rajawali3d.sceneview.render.gles;

import android.opengl.GLES20;

/**
 * Wrapper for an off-screen framebuffer object
 *
 * @author Randy Picolet
 */

public class GlesFramebufferObject extends GlesFramebuffer {

    protected boolean stencilBuffer;

    protected int depthBufferHandle;
    protected int stencilBufferHandle;

    void create() {

    }

    void unbind() {
        if (handle != DEFAULT_HANDLE) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, DEFAULT_HANDLE);
        } else {
            throw new IllegalStateException("");
        }
    }

    void destroy() {

    }
}
