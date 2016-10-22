package c.org.rajawali3d;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.rajawali3d.GlTestActivity;

import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Extend ActivityInstrumentationTestCase2 for testing GL.  Subclasses can
 * use {@link #runOnGlThreadAndWait(Runnable)} and {@link #getGl()} to test from the
 * GL thread.</p>
 *
 * <p>Note: assumes a dummy activity, the test overrides the activity view and
 * renderer.  This framework is intended to test independent GL code.</p>
 *
 * @author Darrell Anderson
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="http://stackoverflow.com/a/9038147/1259881">Stack Overflow</a>
 */
public abstract class GlTestCase {

    @Rule
    public ActivityTestRule<GlTestActivity> activityRule = new ActivityTestRule<>(GlTestActivity.class, false, true);

    private final Object lock = new Object();

    private Activity      activity      = null;
    private GLSurfaceView glSurfaceView = null;
    private GL10          gl10          = null;

    // ------------------------------------------------------------
    // Expose GL context and GL thread.
    // ------------------------------------------------------------

    public GLSurfaceView getGlSurfaceView() {
        return glSurfaceView;
    }

    public GL10 getGl() {
        return gl10;
    }

    /**
     * Run on the GL thread.  Blocks until finished.
     */
    public void runOnGlThreadAndWait(final Runnable runnable) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        glSurfaceView.queueEvent(new Runnable() {
            public void run() {
                runnable.run();
                latch.countDown();
            }
        });
        latch.await();  // wait for runnable to finish
    }

    // ------------------------------------------------------------
    // Normal users should not care about code below this point.
    // ------------------------------------------------------------

    /**
     * Dummy renderer, exposes the GL context for {@link #getGl()}.
     */
    private class MockRenderer implements GLSurfaceView.Renderer {
        @Override
        public void onDrawFrame(GL10 gl) {
            ;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            ;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            synchronized (lock) {
                gl10 = gl;
                lock.notifyAll();
            }
        }
    }

    /**
     * On the first call, set up the GL context.
     */
    protected void setUp(@Nullable final String title) throws Exception {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        // If the activity hasn't changed since last setUp, assume the
        // surface is still there.
        final Activity activity = activityRule.getActivity(); // launches activity
        if (activity == this.activity) {
            glSurfaceView.onResume();
            return;  // same activity, assume surface is still there
        }

        // New or different activity, set up for GL.
        this.activity = activity;

        glSurfaceView = new GLSurfaceView(activity);
        gl10 = null;

        // Attach the renderer to the view, and the view to the activity.
        glSurfaceView.setRenderer(new MockRenderer());
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (title != null) {
                    activity.setTitle(title);
                }
                activity.setContentView(glSurfaceView);
            }
        });

        // Wait for the renderer to get the GL context.
        synchronized (lock) {
            while (gl10 == null) {
                lock.wait();
            }
        }
    }

    protected void tearDown() throws Exception {
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }
}
