package org.rajawali3d.postprocessing.passes;

import android.opengl.GLES20;
import androidx.annotation.NonNull;

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

    public FXAAPass() {
        super();
        createMaterial(new FXAAVertexShader(), new FXAAFragmentShader());
    }

    @Override
    public void render(@NonNull Scene scene, @NonNull Renderer renderer, @NonNull ScreenQuad screenQuad, @NonNull RenderTarget writeTarget,
                       @NonNull RenderTarget readTarget, long elapsedTime, double deltaTime) {
        //Log.d(TAG, "Rendering FXAA Pass at time: " + elapsedTime);
        super.render(scene, renderer, screenQuad, writeTarget, readTarget, elapsedTime, deltaTime);
    }

    protected class FXAAVertexShader extends VertexShader {

        private int rtWHandle;
        private int rtHHandle;

        FXAAVertexShader() {
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

        FXAAFragmentShader() {
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
