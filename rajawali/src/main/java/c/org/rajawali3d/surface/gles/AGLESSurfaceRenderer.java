package c.org.rajawali3d.surface.gles;

import c.org.rajawali3d.core.ARenderControl;
import c.org.rajawali3d.core.RenderContextType;
import c.org.rajawali3d.core.RenderControlClient;
import c.org.rajawali3d.core.RenderSurfaceView;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.surface.SurfaceRenderer;

import android.content.Context;
import android.opengl.GLES20;

import org.rajawali3d.util.RajLog;

import java.util.Locale;

/**
 * Shared implementation for GLES {@link SurfaceRenderer} extensions of {@link ARenderControl}
 *
 * @author Randy Picolet
 */

abstract class AGLESSurfaceRenderer extends ARenderControl {

    AGLESSurfaceRenderer(Context context, RenderSurfaceView renderSurfaceView,
                    RenderControlClient renderControlClient, double initialFrameRate) {
        super(context, renderSurfaceView, renderControlClient, initialFrameRate);
    }

    /**
     *
     */
    protected void onRenderContextAcquired() {
        // Initialize device Capabilities for client use
        Capabilities.getInstance();

        // In case we cannot parse the version number, assume OpenGL ES 2.0
        int glesMajorVersion = 2;
        int glesMinorVersion = 0;

        String[] versionString = (GLES20.glGetString(GLES20.GL_VERSION)).split(" ");
        RajLog.d("Open GL ES Version String: " + GLES20.glGetString(GLES20.GL_VERSION));
        if (versionString.length >= 3) {
            String[] versionParts = versionString[2].split("\\.");
            if (versionParts.length >= 2) {
                glesMajorVersion = Integer.parseInt(versionParts[0]);
                versionParts[1] = versionParts[1].replaceAll("([^0-9].+)", "");
                glesMinorVersion = Integer.parseInt(versionParts[1]);
            }
        }
        RajLog.d(String.format(Locale.US, "Derived GL ES Version: %d.%d", glesMajorVersion, glesMinorVersion));

        // Propagate to super
        super.onRenderContextAcquired(RenderContextType.OPEN_GL_ES, glesMajorVersion, glesMinorVersion);
    }
}
