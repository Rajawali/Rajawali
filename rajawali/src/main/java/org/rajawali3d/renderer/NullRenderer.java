package org.rajawali3d.renderer;

import android.content.Context;

import org.rajawali3d.util.RajLog;

/**
 * Minimal {@link RajawaliRenderer} implementation which will cause no rendering to occur.
 *
 * @author Ian Thomas (toxicbakery@gmail.com)
 */
public final class NullRenderer extends RajawaliRenderer {

    public NullRenderer(Context context) {
        super(context);
        RajLog.w(this + ": Fragment created without renderer!");
    }

    @Override
    public void onSurfaceDestroyed() {
        stopRendering();
    }
}