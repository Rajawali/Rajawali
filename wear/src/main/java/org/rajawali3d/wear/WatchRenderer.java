package org.rajawali3d.wear;

import android.content.Context;
import android.view.MotionEvent;
import org.rajawali3d.materials.MaterialManager;
import org.rajawali3d.renderer.Renderer;
import c.org.rajawali3d.gl.Capabilities;

/**
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public abstract class WatchRenderer extends Renderer {

    public WatchRenderer(Context context) {
        super(context);
    }

    void create() {
        Capabilities.getInstance();

        mGLES_Major_Version = 2;
        mGLES_Minor_Version = 1;

        //TODO: Fix watch rendering

        /*mTextureManager = TextureManager.getInstance();
        mTextureManager.setContext(getContext());
        mTextureManager.registerRenderer(this);*/

        mMaterialManager = MaterialManager.getInstance();
        mMaterialManager.setContext(getContext());
        mMaterialManager.registerRenderer(this);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset,
                                 int yPixelOffset) {
        // This method is unused
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        // This method is unused
    }
}
