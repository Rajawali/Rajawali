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
package org.rajawali3d.renderer;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.renderer.pip.SubRenderer;
import org.rajawali3d.renderer.pip.WorkaroundScreenQuad;
import org.rajawali3d.scene.Scene;

/**
 * <p>
 * Creates a renderer that renders two scenes through separate <code>SubRenderer</code>s.
 * In order to use this, first create the <code>PipRenderer</code>, then create and assign the
 * <code>SubRenderer</code>s through <code>setMainRenderer</code> (for the main content)
 * <code>setMinirenderer</code> (for the content in the mini view).
 * The <code>pipScale</code> and <code>pipMargin</code> parameters control how the mini scene
 * is placed.
 * </p>
 */
public class PipRenderer extends Renderer {
    private static final String TAG = "PipRenderer";

    private RenderTarget mMainRenderTarget;
    private RenderTarget mMiniRenderTarget;

    private WorkaroundScreenQuad mMiniQuad;
    private WorkaroundScreenQuad mMainQuad;
    private Material mMiniQuadMaterial;
    private Material mMainQuadMaterial;

    private Scene mCompositeScene;

    private SubRenderer mMiniRenderer;
    private SubRenderer mMainRenderer;

    // These variables control where the minimap is placed. Note they are specified in standardized
    // OpenGL coordinates 0 to 1
    private final float mPipScale;
    private final float mPipMarginX;
    private final float mPipMarginY;

    private float mMiniXmin, mMiniXmax, mMiniYmin, mMiniYmax;

    /**
     * @param pipScale   Size of the mini view from 0 to 1, i.e.: ratio of full screen to mini
     * @param pipMarginX Margin space from the mini view to the edge of the main view in pixels
     * @param pipMarginY Margin space from the mini view to the edge of the main view in pixels
     */
    public PipRenderer(Context context, float pipScale, float pipMarginX, float pipMarginY) {
        super(context);
        mPipScale = pipScale;
        mPipMarginX = pipMarginX;
        mPipMarginY = pipMarginY;
    }

    public void setMiniRenderer(SubRenderer miniRenderer) {
        mMiniRenderer = miniRenderer;
    }

    public void setMainRenderer(SubRenderer mainRenderer) {
        mMainRenderer = mainRenderer;
    }

    @Override
    public void initScene() {
        mMainQuadMaterial = new Material();
        mMainQuadMaterial.setColorInfluence(0);

        mMiniQuadMaterial = new Material();
        mMiniQuadMaterial.setColorInfluence(0);

        mMainQuad = new WorkaroundScreenQuad();
        mMainQuad.setMaterial(mMainQuadMaterial);
        mMainQuad.setTransparent(true);

        // Set-up viewport dimensions of mini quad for touch event processing
        setupMiniTouchLimits();

        mMiniQuad = new WorkaroundScreenQuad();
        // Set the size of the mini view using a scale factor (mPipScale times the main view)
        mMiniQuad.setScale(mPipScale);
        // Position the mini view in the top right corner
        // For X and Y, the position is:
        //   50% screen shift to the right/top minus half the size of the minimap to bring it back
        //   left/bottom into full view plus a little bit more left/bottom to leave margin
        mMiniQuad.setX(.5d - mPipScale / 2d - mPipMarginX / mDefaultViewportWidth);
        mMiniQuad.setY(.5d - mPipScale / 2d - mPipMarginY / mDefaultViewportHeight);
        mMiniQuad.setMaterial(mMiniQuadMaterial);

        mMainRenderTarget =
                new RenderTarget("pipMainRT", mDefaultViewportWidth, mDefaultViewportHeight);
        mMainRenderTarget.setFullscreen(false);
        mMiniRenderTarget =
                new RenderTarget("pipMiniRT", mDefaultViewportWidth, mDefaultViewportHeight);
        mMiniRenderTarget.setFullscreen(false);

        addRenderTarget(mMainRenderTarget);
        addRenderTarget(mMiniRenderTarget);

        mCompositeScene = getCurrentScene();
        mCompositeScene.addChild(mMainQuad);
        mCompositeScene.addChild(mMiniQuad);

        try {
            mMiniQuadMaterial.addTexture(mMiniRenderTarget.getTexture());
            mMainQuadMaterial.addTexture(mMainRenderTarget.getTexture());
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        // Init main scene
        mMainRenderer.initScene();

        // Init mini scene
        mMiniRenderer.initScene();
    }

    @Override
    protected void onRender(final long elapsedTime, final double deltaTime) {
        try {
            // Render mini scene into mini quad
            switchSceneDirect(mMiniRenderer.getCurrentScene());
            mMiniRenderer.doRender();
            setRenderTarget(mMiniRenderTarget);
            render(elapsedTime, deltaTime);

            // Render main scene into main quad
            switchSceneDirect(mMainRenderer.getCurrentScene());
            mMainRenderer.doRender();
            setRenderTarget(mMainRenderTarget);
            render(elapsedTime, deltaTime);

            // Render everything into the surface
            switchSceneDirect(mCompositeScene);
            setRenderTarget(null);
            render(elapsedTime, deltaTime);
        } catch (Throwable t) {
            Log.e(TAG, "Exception in render loop.", t);
        }
    }

    /**
     * Calculate the min and max X and Y coordinates of the mini scene, in screen coordinates.
     */
    private void setupMiniTouchLimits() {
        // Start and end of the Quad in OpenGL standardized coordinates
        float minX = 1 - mPipScale - mPipMarginX / mDefaultViewportWidth;
        float maxX = 1 - mPipMarginX / mDefaultViewportWidth;

        float minY = 1 - mPipScale - mPipMarginX / mDefaultViewportHeight;
        float maxY = 1 - mPipMarginX / mDefaultViewportHeight;

        mMiniXmin = minX * mDefaultViewportWidth;
        mMiniXmax = maxX * mDefaultViewportWidth;
        // Note that Y is reversed between OpenGL (+up) and screen coordinates (+down)
        mMiniYmin = (1 - maxY) * mDefaultViewportHeight;
        mMiniYmax = (1 - minY) * mDefaultViewportHeight;
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        // TODO(adamantivm)
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // If the event falls into the mini scene, forward the event to the mini renderer
        // TODO(adamantivm) See if there is a way to delegate the calculation of whether a click
        // falls inside a Quad or not, so that we don't have to do the calculation ourselves
        if (x > mMiniXmin && x < mMiniXmax && y > mMiniYmin && y < mMiniYmax) {
            mMiniRenderer.onTouchEvent(event);
            // Otherwise dispatch it to the main renderer
        } else {
            mMainRenderer.onTouchEvent(event);
        }
    }
}
