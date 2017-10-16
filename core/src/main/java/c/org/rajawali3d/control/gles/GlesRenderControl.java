package c.org.rajawali3d.control.gles;

import android.content.Context;
import android.opengl.GLES20;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.control.BaseRenderControl;
import c.org.rajawali3d.control.RenderContextType;
import c.org.rajawali3d.control.RenderControlClient;
import c.org.rajawali3d.control.RenderSurfaceView;
import c.org.rajawali3d.surface.SurfaceRenderer;
import java.util.Locale;


/**
 * Shared implementations for GLES {@link SurfaceRenderer} extensions of {@link BaseRenderControl}
 *
 * @author Randy Picolet
 */

public abstract class GlesRenderControl extends BaseRenderControl {

    protected GlesRenderControl(Context context, RenderSurfaceView renderSurfaceView,
                                RenderControlClient renderControlClient, double initialFrameRate) {
        super(context, renderSurfaceView, renderControlClient, initialFrameRate);
    }

    /**
     * Shared implementation for responding to GL context creation events; simply determines the context version and
     * forwards up to {@link BaseRenderControl#onRenderContextAcquired(RenderContextType, int, int)}
     */
    @RenderThread
    public void specifyRenderContextAcquired() {

        // Derive the major and minor version numbers

        // In case we cannot parse the version number string, assume OpenGL ES 2.0
        int glesMajorVersion = 2;
        int glesMinorVersion = 0;

        String glesVersionString = GLES20.glGetString(GLES20.GL_VERSION);
        logD("Open GL ES Version String: " + glesVersionString);
        String[] versionWords = glesVersionString.split(" ");
        if (versionWords.length >= 3) {
            String[] versionParts = versionWords[2].split("\\.");
            if (versionParts.length >= 2) {
                glesMajorVersion = Integer.parseInt(versionParts[0]);
                versionParts[1] = versionParts[1].replaceAll("([^0-9].+)", "");
                glesMinorVersion = Integer.parseInt(versionParts[1]);
            }
        }
        logD(String.format(Locale.US, "Derived GL ES Version: %d.%d", glesMajorVersion, glesMinorVersion));

        // Forward this event up to the generic SurfaceRenderer method in BaseRenderControl
        onRenderContextAcquired(RenderContextType.OPEN_GL_ES, glesMajorVersion, glesMinorVersion);
    }

    protected void paintSurfaceBackground() {
        // TODO
        // set viewport to whole screen,
        // clear scissor,
        // set background color, and
        // clear
    }
}
