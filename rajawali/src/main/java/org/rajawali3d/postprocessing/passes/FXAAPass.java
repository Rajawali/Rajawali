package org.rajawali3d.postprocessing.passes;

import android.opengl.GLES20;
import org.rajawali3d.R;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

/**
 * Adds a Fast Approximate Antialiasing (FXAA) post processing pass to the scene. The implementation is taken from
 * <a href="http://www.geeks3d.com/20110405/fxaa-fast-approximate-anti-aliasing-demo-glsl-opengl-test-radeon-geforce/">Geeks 3D</a>.
 *
 * <b>Use of this effect requires GL ES 3.0 or better.</b>.
 * <b>Use of this effect requires the following extension GL_EXT_gpu_shader4</b>
 *
 * A quick synopsis from the above reference follows:
 * <ul>
 *     <li>GPU based MLAA implementation by Timothy Lottes (Nvidia)</li>
 *     <li>Multiple quality options</li>
 * </ul>
 *
 * Pros & Cons:
 * <ul>
 *     <li>Superb antialiased long edges</li>
 *     <li>Smooth overall picture</li>
 *     <li>Reasonably fast</li>
 *     <li>Moving pictures do not benefit as much</li>
 *     <li>"Blurry"</li>
 * </ul>
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class FXAAPass extends EffectPass {

    private static final String TAG = "FXAAPass";

    public FXAAPass() {
        super();
        createMaterial(new FXAAVertexShader(), new FXAAFragmentShader());
    }

    @Override
    public void render(Scene scene, Renderer renderer, ScreenQuad screenQuad, RenderTarget writeTarget,
                                 RenderTarget readTarget, long ellapsedTime, double deltaTime) {
        //Log.d(TAG, "Rendering FXAA Pass at time: " + ellapsedTime);
        super.render(scene, renderer, screenQuad, writeTarget, readTarget, ellapsedTime, deltaTime);
    }

    protected class FXAAVertexShader extends VertexShader {

        private int rtWHandle;
        private int rtHHandle;

        public FXAAVertexShader() {
            super(R.raw.fxaa_vertex_shader);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            rtWHandle = getUniformLocation(programHandle, "rt_w");
            rtHHandle = getUniformLocation(programHandle, "rt_h");
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(rtWHandle, mReadTarget.getWidth());
            GLES20.glUniform1f(rtHHandle, mReadTarget.getHeight());
        }
    }

    protected class FXAAFragmentShader extends FragmentShader {

        private int rtWHandle;
        private int rtHHandle;

        public FXAAFragmentShader() {
            super(R.raw.fxaa_fragment_shader);
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);
            rtWHandle = getUniformLocation(programHandle, "rt_w");
            rtHHandle = getUniformLocation(programHandle, "rt_h");
        }

        @Override
        public void applyParams() {
            super.applyParams();
            GLES20.glUniform1f(rtWHandle, mReadTarget.getWidth());
            GLES20.glUniform1f(rtHHandle, mReadTarget.getHeight());
        }
    }
}
