package org.rajawali3d.examples.examples;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.views.GithubLogoView;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.IDisplay;
import org.rajawali3d.view.ISurface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class AExampleFragment extends Fragment implements IDisplay, OnClickListener {

	public static final String BUNDLE_EXAMPLE_URL = "BUNDLE_EXAMPLE_URL";
	public static final String BUNDLE_EXAMPLE_TITLE = "BUNDLE_EXAMPLE_TITLE";

	protected ProgressBar      mProgressBarLoader;
	protected GithubLogoView   mImageViewExampleLink;
	protected String           mExampleUrl;
	protected String           mExampleTitle;
    protected FrameLayout      mLayout;
    protected ISurface         mRenderSurface;
    protected ISurfaceRenderer mRenderer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle bundle = getArguments();
		if (bundle == null || !bundle.containsKey(BUNDLE_EXAMPLE_URL)) {
			throw new IllegalArgumentException(getClass().getSimpleName()
					+ " requires " + BUNDLE_EXAMPLE_URL
					+ " argument at runtime!");
		}

		mExampleUrl = bundle.getString(BUNDLE_EXAMPLE_URL);
		mExampleTitle = bundle.getString(BUNDLE_EXAMPLE_TITLE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the view
        mLayout = (FrameLayout) inflater.inflate(getLayoutID(), container, false);

		mLayout.findViewById(R.id.relative_layout_loader_container).bringToFront();

        // Find the TextureView
		mRenderSurface = (ISurface) mLayout.findViewById(R.id.rajwali_surface);

		// Create the loader
		mProgressBarLoader = (ProgressBar) mLayout.findViewById(R.id.progress_bar_loader);
		mProgressBarLoader.setVisibility(View.GONE);

		// Set the example link
		mImageViewExampleLink = (GithubLogoView) mLayout.findViewById(R.id.image_view_example_link);
		mImageViewExampleLink.setOnClickListener(this);

		getActivity().setTitle(mExampleTitle);

        // Create the renderer
        mRenderer = createRenderer();
        onBeforeApplyRenderer();
        applyRenderer();
		return mLayout;
	}

    protected void onBeforeApplyRenderer() {

    }

    protected void applyRenderer() {
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }

    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.image_view_example_link:
			if (mImageViewExampleLink == null) throw new IllegalStateException("Example link is null!");

			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExampleUrl));
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (mLayout != null)
			mLayout.removeView((View) mRenderSurface);
	}

    @Override
    public int getLayoutID() {
        return R.layout.rajawali_textureview_fragment;
    }

	protected void hideLoader() {
		mProgressBarLoader.post(new Runnable() {
			@Override
			public void run() {
				mProgressBarLoader.setVisibility(View.GONE);
			}
		});
	}

	protected void showLoader() {
		mProgressBarLoader.post(new Runnable() {
			@Override
			public void run() {
				mProgressBarLoader.setVisibility(View.VISIBLE);
			}
		});
	}

	protected abstract class AExampleRenderer extends Renderer {

		public AExampleRenderer(Context context) {
			super(context);
		}

        @Override
        public void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {

        }

        @Override
        public void onTouchEvent(MotionEvent event) {

        }

        @Override
		public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
			showLoader();
			super.onRenderSurfaceCreated(config, gl, width, height);
			hideLoader();
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
        }
    }
}
