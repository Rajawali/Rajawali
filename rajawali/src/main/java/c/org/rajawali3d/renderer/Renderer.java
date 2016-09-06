package c.org.rajawali3d.renderer;

import android.graphics.SurfaceTexture;
import android.view.MotionEvent;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.ISurface.ANTI_ALIASING_CONFIG;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Renderer implements ISurfaceRenderer {
    @Override public double getFrameRate() {
        return 0;
    }

    @Override public void setFrameRate(int rate) {

    }

    @Override public void setFrameRate(double rate) {

    }

    @Override public void setAntiAliasingMode(ANTI_ALIASING_CONFIG config) {

    }

    @Override public void setRenderSurface(ISurface surface) {

    }

    @Override public void onPause() {

    }

    @Override public void onResume() {

    }

    @Override public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {

    }

    @Override public void onRenderSurfaceDestroyed(SurfaceTexture surface) {

    }

    @Override public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {

    }

    @Override public void onRenderFrame(GL10 gl) {

    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                 int yPixelOffset) {

    }

    @Override public void onTouchEvent(MotionEvent event) {

    }
}
