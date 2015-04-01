package org.rajawali3d.surface;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.view.TextureView;

import org.rajawali3d.renderer.RajawaliRenderer;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class RajawaliTextureView extends TextureView {

    public RajawaliTextureView(Context context) {
        super(context);
    }

    public RajawaliTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RajawaliTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RajawaliTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public class RendererDelegate implements SurfaceTextureListener {

        private final RajawaliTextureView mRajawaliTextureView;
        private final RajawaliRenderer mRenderer;

        public RendererDelegate(RajawaliRenderer renderer, RajawaliTextureView textureView) {
            mRenderer = renderer;
            mRajawaliTextureView = textureView;
            mRajawaliTextureView.setSurfaceTextureListener(this);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mRenderer.onRenderSurfaceCreated(null, surface, width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mRenderer.onRenderSurfaceSizeChanged(surface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mRenderer.onRenderSurfaceDestroyed(surface);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            mRenderer.onRenderFrame(surface);
        }
    }
}
