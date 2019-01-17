package org.rajawali3d.vuforia;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import com.vuforia.*;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.RajLog;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public abstract class VuforiaRenderer extends Renderer {

    private final Vector3 position;
    private final Quaternion orientation;
    private final double[] modelViewMatrix;
    private final int videoMode;

    @SuppressWarnings("WeakerAccess")
    protected ScreenQuad backgroundQuad;
    @SuppressWarnings("WeakerAccess")
    protected RenderTarget backgroundRenderTarget;

    protected VuforiaManager vuforiaManager;

    public VuforiaRenderer(
            @NonNull Context context,
            @NonNull VuforiaManager vuforiaManager,
            int videoMode
    ) {
        super(context);
        position = new Vector3();
        orientation = new Quaternion();
        getCurrentCamera().setNearPlane(10);
        getCurrentCamera().setFarPlane(2500);
        modelViewMatrix = new double[16];
        this.vuforiaManager = vuforiaManager;
        this.videoMode = videoMode;
    }

    private static void copyMatrixArray(float[] src, double[] dst) {
        for (int i = 0; i < 16; i++) {
            dst[i] = src[i];
        }
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        vuforiaManager.onSurfaceCreated();
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, final int width, final int height) {
        RajLog.i("Vuforia onRenderSurfaceSizeChanged(" + width + ", " + height + ")");
        super.onRenderSurfaceSizeChanged(gl, width, height);

        vuforiaManager.onSurfaceChanged(width, height);

        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(videoMode);
        CameraCalibration cameraCalibration = cameraDevice.getCameraCalibration();

        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setPosition(new Vec2I(0, 0));

        int xSize, ySize;
        if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            xSize = (int) (vm.getHeight() * (height / (float) vm.getWidth()));
            ySize = height;
            if (xSize < width) {
                xSize = width;
                ySize = (int) (width * (vm.getWidth() / (float) vm.getHeight()));
            }
        } else {
            xSize = width;
            ySize = (int) (vm.getHeight() * (width / (float) vm.getWidth()));
            if (ySize < height) {
                xSize = (int) (height * (vm.getWidth() / (float) vm.getHeight()));
                ySize = height;
            }
        }
        config.setSize(new Vec2I(xSize, ySize));

        Vec2F size = cameraCalibration.getSize();
        Vec2F focalLength = cameraCalibration.getFocalLength();
        double fovRadians = 2 * Math.atan(0.5f * size.getData()[1] / focalLength.getData()[1]);
        double fovDegrees = fovRadians * 180.0 / Math.PI;
        getCurrentCamera().setProjectionMatrix(fovDegrees, vm.getWidth(), vm.getHeight());

        if (backgroundRenderTarget == null) {
            backgroundRenderTarget = new RenderTarget("rajVuforia", width, height);

            addRenderTarget(backgroundRenderTarget);
            Material material = new Material();
            material.setColorInfluence(0);
            try {
                material.addTexture(backgroundRenderTarget.getTexture());
            } catch (TextureException e) {
                e.printStackTrace();
            }

            backgroundQuad = new ScreenQuad();
            if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                backgroundQuad.setScaleY((float) height / (float) vm.getHeight());
            else
                backgroundQuad.setScaleX((float) width / (float) vm.getWidth());
            backgroundQuad.setMaterial(material);
            getCurrentScene().addChildAt(backgroundQuad, 0);
        }

        com.vuforia.Renderer.getInstance().setVideoBackgroundConfig(config);
    }

    private void transformPositionAndOrientation(float[] modelViewMatrix) {
        position.setAll(modelViewMatrix[12], -modelViewMatrix[13], -modelViewMatrix[14]);
        copyMatrixArray(modelViewMatrix, this.modelViewMatrix);
        orientation.fromMatrix(this.modelViewMatrix);

        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            position.setAll(modelViewMatrix[12], -modelViewMatrix[13],
                    -modelViewMatrix[14]);
            orientation.y = -orientation.y;
            orientation.z = -orientation.z;
        } else {
            position.setAll(-modelViewMatrix[13], -modelViewMatrix[12],
                    -modelViewMatrix[14]);
            double orX = orientation.x;
            orientation.x = -orientation.y;
            orientation.y = -orX;
            orientation.z = -orientation.z;
        }
    }

    @Override
    public void onRenderFrame(GL10 gl) {
        super.onRenderFrame(gl);
        com.vuforia.Renderer vuforiaRenderer = com.vuforia.Renderer.getInstance();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        vuforiaRenderer.begin(new State());
        int frameBufferHandle = backgroundRenderTarget.getFrameBufferHandle();
        int textureId = backgroundRenderTarget.getTexture().getTextureId();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferHandle);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
        vuforiaRenderer.updateVideoBackgroundTexture();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        vuforiaRenderer.end();
    }

    /**
     * Get the current screen orientation.
     *
     * @return Android screen orientation
     */
    private int getScreenOrientation() {
        return getContext().getResources().getConfiguration().orientation;
    }

}
