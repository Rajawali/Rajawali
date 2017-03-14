package org.rajawali3d.examples.examples;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.core.RenderControlClient;
import c.org.rajawali3d.scene.AScene;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.surface.gles.GLESTextureView;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.views.GitHubLogoView;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public abstract class AExampleFragment extends Fragment implements RenderControlClient, OnClickListener {

    public static final String BUNDLE_EXAMPLE_URL = "BUNDLE_EXAMPLE_URL";

    protected ProgressBar     mProgressBar;
    protected GitHubLogoView  mImageViewExampleLink;
    protected String          mExampleUrl;
    protected FrameLayout     mLayout;
    protected GLESTextureView mSurfaceView;
    protected RenderControl   mRenderControl;
    protected AScene          mScene;
    protected SurfaceSize     mSurfaceSize;

    /*
     * Fragment Lifecycle callbacks
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle == null || !bundle.containsKey(BUNDLE_EXAMPLE_URL)) {
            throw new IllegalArgumentException(getClass().getSimpleName()
                    + " requires " + BUNDLE_EXAMPLE_URL + " argument at runtime!");
        }

        mExampleUrl = bundle.getString(BUNDLE_EXAMPLE_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the view
        mLayout = (FrameLayout) inflater.inflate(getLayoutId(), container, false);

        mLayout.findViewById(R.id.relative_layout_loader_container).bringToFront();

        // Find the GLESSurfaceTextureView
        mSurfaceView = (GLESTextureView) mLayout.findViewById(R.id.rajwali_surface);

        // Create the loader progress bar
        mProgressBar = (ProgressBar) mLayout.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        // Set the example link
        mImageViewExampleLink = (GitHubLogoView) mLayout.findViewById(R.id.image_view_example_link);
        mImageViewExampleLink.setOnClickListener(this);

        // Configure the SurfaceView
        setSurfaceConfigurations();
        mSurfaceView.configure(this);

        return mLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgressBar();

        // TODO model correct Android usage of onResume, whatever it is
        mSurfaceView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        // TODO model correct Android usage of onPause, whatever it is
        mSurfaceView.onPause();
    }

    // TODO is this necessary? If not, let's remove it
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mLayout != null)
            mLayout.removeView((View) mSurfaceView);
    }


    /**
     * Override to define any GLESSurfaceTextureView-specific configuration options (such as anti-aliasing),
     * or to override such options specified as styleable layout attributes
     */
    protected void setSurfaceConfigurations() {
    }

    /*
     * RenderControlClient callbacks
     */

    @RenderThread
    @Override
    @CallSuper
    public void onRenderControlAvailable(RenderControl renderControl, SurfaceSize surfaceSize) {
        mRenderControl = renderControl;
        mSurfaceSize = surfaceSize;
    }

    @RenderThread
    @Override
    @CallSuper
    public void onSurfaceSizeChanged(SurfaceSize surfaceSize) {
        mSurfaceSize = surfaceSize;
    }

    //
    //
    //

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_example_link:
                if (mImageViewExampleLink == null)
                    throw new IllegalStateException("Example link is null!");

                try {
                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExampleUrl));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    ExceptionDialog dialog = ExceptionDialog.newInstance(
                            getString(R.string.exception_dialog_title),
                            getString(R.string.exception_dialog_message_no_browser));

                    dialog.show(getFragmentManager(), ExceptionDialog.TAG);
                }
                break;
        }
    }

    //
    //
    //

    @CallSuper
    protected void hideProgressBar() {
        mProgressBar.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    @CallSuper
    protected void showProgressBar() {
        mProgressBar.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @LayoutRes
    protected int getLayoutId() {
        return R.layout.rajawali_textureview_fragment;
    }
}
