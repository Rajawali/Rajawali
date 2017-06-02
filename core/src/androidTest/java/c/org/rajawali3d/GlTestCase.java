package c.org.rajawali3d;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.core.RenderControlClient;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.surface.gles.GLESSurfaceView;
import org.junit.Rule;
import org.rajawali3d.GlTestActivity;

import java.util.concurrent.CountDownLatch;

/**
 * Extend ActivityInstrumentationTestCase2 for testing GL.  Subclasses can
 * use {@link #runOnGlThreadAndWait(Runnable)} to test from the
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

    private Activity            activity  = null;
    private GLESSurfaceView     glSurface = null;
    private Object              gl10      = null;
    private RenderControlClient renderer  = null;

    // ------------------------------------------------------------
    // Expose GL context and GL thread.
    // ------------------------------------------------------------

    public Context getContext() {
        return activity.getApplicationContext();
    }

    /**
     * Run on the GL thread.  Blocks until finished.
     */
    public void runOnGlThreadAndWait(final Runnable runnable) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        glSurface.queueEvent(new Runnable() {
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
     * Dummy renderer, exposes the GL context.
     */
    private class MockRenderer implements RenderControlClient {

        MockRenderer(@NonNull Context context) {
        }

        @Override
        public void onRenderControlAvailable(@NonNull RenderControl renderControl, @NonNull SurfaceSize surfaceSize) {
            synchronized (lock) {
                gl10 = new Object();
                lock.notifyAll();
            }
        }

        @Override
        public void onSurfaceSizeChanged(@NonNull SurfaceSize surfaceSize) {

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
            glSurface.onResume();
            return;  // same activity, assume surface is still there
        }

        // New or different activity, set up for GL.
        this.activity = activity;

        glSurface = new GLESSurfaceView(activity);

        // Attach the renderer to the view, and the view to the activity.
        renderer = new MockRenderer(activity);
        glSurface.configure(renderer);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (title != null) {
                    activity.setTitle(title);
                }
                activity.setContentView((View) glSurface);
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
        if (renderer != null) {
            glSurface.onPause();
        }
    }
}
