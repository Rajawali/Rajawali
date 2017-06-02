/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.renderer.pip;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.cameras.Camera2D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.postprocessing.passes.EffectPass;

/**
 * <p>
 * This is a ScreenQuad identical to Rajawali's except that it uses the old set of coordinates
 * for applying textures.
 * To be used until the issue is resolved, see https://github.com/Rajawali/Rajawali/pull/1508
 * </p>
 */
public class WorkaroundScreenQuad extends Object3D {
    private Camera2D mCamera;
    private Matrix4 mVPMatrix;
    private EffectPass mEffectPass;

    /**
     * Creates a new ScreenQuad.
     */
    public WorkaroundScreenQuad() {
        this(true);
    }

    /**
     * Creates a new ScreenQuad.
     */
    public WorkaroundScreenQuad(boolean createVBOs) {
        super();
        init(createVBOs);
    }

    private void init(boolean createVBOs) {
        mCamera = new Camera2D();
        mCamera.setProjectionMatrix(0, 0);
        mVPMatrix = new Matrix4();

        float[] vertices = new float[]{
                -.5f, .5f, 0,
                .5f, .5f, 0,
                .5f, -.5f, 0,
                -.5f, -.5f, 0
        };
        float[] textureCoords = new float[]{
                0, 1, 1, 1, 1, 0, 0, 0
        };
        float[] normals = new float[]{
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1
        };
        int[] indices = new int[]{0, 2, 1, 0, 3, 2};

        setData(vertices, normals, textureCoords, null, indices, createVBOs);

        vertices = null;
        normals = null;
        textureCoords = null;
        indices = null;

        mEnableDepthTest = false;
        mEnableDepthMask = false;
    }

    public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix,
                       final Matrix4 vMatrix, final Matrix4 parentMatrix, Material sceneMaterial) {
        final Matrix4 pMatrix = mCamera.getProjectionMatrix();
        final Matrix4 viewMatrix = mCamera.getViewMatrix();
        mVPMatrix.setAll(pMatrix).multiply(viewMatrix);
        super.render(mCamera, mVPMatrix, projMatrix, viewMatrix, null, sceneMaterial);
    }

    @Override
    protected void setShaderParams(Camera camera) {
        super.setShaderParams(camera);
        if (mEffectPass != null) {
            mEffectPass.setShaderParams();
        }
    }

    public void setEffectPass(EffectPass effectPass) {
        mEffectPass = effectPass;
    }
}
