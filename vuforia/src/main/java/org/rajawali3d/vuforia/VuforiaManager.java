package org.rajawali3d.vuforia;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.qualcomm.QCAR.QCAR;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

public class VuforiaManager {
    protected static int TRACKER_TYPE_IMAGE  = 0;
    protected static int TRACKER_TYPE_MARKER = 1;

    private static final int APPSTATUS_UNINITED         = -1;
    private static final int APPSTATUS_INIT_APP         = 0;
    private static final int APPSTATUS_INIT_QCAR        = 1;
    private static final int APPSTATUS_INIT_APP_AR      = 2;
    private static final int APPSTATUS_INIT_TRACKER     = 3;
    private static final int APPSTATUS_INIT_CLOUDRECO   = 4;
    private static final int APPSTATUS_INITED           = 5;
    private static final int APPSTATUS_CAMERA_STOPPED   = 6;
    private static final int APPSTATUS_CAMERA_RUNNING   = 7;
    private static final int FOCUS_MODE_NORMAL          = 0;
    private static final int FOCUS_MODE_CONTINUOUS_AUTO = 1;

    // These codes match the ones defined in TargetFinder.h for Cloud Reco service
    static final int INIT_SUCCESS                        = 2;
    static final int INIT_ERROR_NO_NETWORK_CONNECTION    = -1;
    static final int INIT_ERROR_SERVICE_NOT_AVAILABLE    = -2;
    static final int UPDATE_ERROR_AUTHORIZATION_FAILED   = -1;
    static final int UPDATE_ERROR_PROJECT_SUSPENDED      = -2;
    static final int UPDATE_ERROR_NO_NETWORK_CONNECTION  = -3;
    static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE  = -4;
    static final int UPDATE_ERROR_BAD_FRAME_QUALITY      = -5;
    static final int UPDATE_ERROR_UPDATE_SDK             = -6;
    static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
    static final int UPDATE_ERROR_REQUEST_TIMEOUT        = -8;

    private static final String NATIVE_LIB_VUFORIA          = "Vuforia";
    private static final String NATIVE_LIB_RAJAWALI_VUFORIA = "RajawaliVuforia";

    private final Object mShutdownLock = new Object();

    private ISurfaceRenderer mRenderer;
    private SurfaceView      mSurfaceView;
    private int mScreenWidth  = 0;
    private int mScreenHeight = 0;
    private int mAppStatus    = APPSTATUS_UNINITED;
    private int mFocusMode;
    private int mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    private InitQCARTask mInitQCARTask;
    private boolean mUseCloudRecognition = false;
    private       InitCloudRecoTask mInitCloudRecoTask;
    private       boolean           mRajawaliIsInitialized;
    private final VuforiaConsumer   mConsumer;

    static {
        loadLibrary(NATIVE_LIB_VUFORIA);
        loadLibrary(NATIVE_LIB_RAJAWALI_VUFORIA);
    }

    public interface VuforiaConsumer {

        @NonNull
        Activity getActivity();

        @NonNull
        ISurface getRenderSurface();

        void initialize();

        void onPostCloudRecoInit(boolean success, @Nullable String message);

        void onPostQcarInit(boolean success, @Nullable String message);
    }

    public VuforiaManager(@NonNull VuforiaConsumer consumer) {
        mConsumer = consumer;
    }

    /**
     * An async task to initialize cloud-based recognition asynchronously.
     */
    private class InitCloudRecoTask extends AsyncTask<Void, Integer, Boolean> {
        // Initialize with invalid value
        private int mInitResult = -1;

        protected Boolean doInBackground(Void... params) {
            // Prevent the onDestroy() method to overlap:
            synchronized (mShutdownLock) {
                // Init cloud-based recognition:
                mInitResult = initCloudReco();
                return mInitResult == INIT_SUCCESS;
            }
        }


        protected void onPostExecute(Boolean result) {
            RajLog.d("InitCloudRecoTask::onPostExecute: execution " + (result ? "successful" : "failed"));

            if (result) {
                // Done loading the tracker, update application status:
                updateApplicationStatus(APPSTATUS_INITED);
            } else {
                updateApplicationStatus(APPSTATUS_INITED);
                // Create dialog box for display error:
                String logMessage;
                if (mInitResult == QCAR.INIT_DEVICE_NOT_SUPPORTED) {
                    logMessage = "Failed to initialize QCAR because this " +
                                 "device is not supported.";
                } else {
                    logMessage = "Failed to initialize CloudReco.";
                }
                RajLog.e("InitQCARTask::onPostExecute: " + logMessage + " Exiting.");
                mConsumer.onPostCloudRecoInit(result, logMessage);
            }
        }
    }

    /**
     * An async task to initialize QCAR asynchronously.
     */
    private class InitQCARTask extends AsyncTask<Void, Integer, Boolean> {
        private int mProgressValue = -1;

        protected Boolean doInBackground(Void... params) {
            synchronized (mShutdownLock) {
                QCAR.setInitParameters(mConsumer.getActivity(), QCAR.GL_20);

                do {
                    mProgressValue = QCAR.init();
                    publishProgress(mProgressValue);
                } while (!isCancelled() && mProgressValue >= 0
                         && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }

        protected void onProgressUpdate(Integer... values) {
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                RajLog.d("InitQCARTask::onPostExecute: QCAR " + "initialization successful");
                updateApplicationStatus(APPSTATUS_INIT_TRACKER);
            } else {
                String logMessage;
                if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED) {
                    logMessage = "Failed to initialize QCAR because this " + "device is not supported.";
                } else {
                    logMessage = "Failed to initialize QCAR.";
                }
                RajLog.e("InitQCARTask::onPostExecute: " + logMessage + " Exiting.");
                mConsumer.onPostQcarInit(result, logMessage);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        storeScreenDimensions();
    }

    public void startVuforia() {
        updateApplicationStatus(APPSTATUS_INIT_APP);
    }

    public void onResume() {
        QCAR.onResume();

        if (mAppStatus == APPSTATUS_CAMERA_STOPPED) {
            updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        storeScreenDimensions();

        if (QCAR.isInitialized() && (mAppStatus == APPSTATUS_CAMERA_RUNNING)) {
            setProjectionMatrix();
        }
    }

    public void onPause() {
        if (mAppStatus == APPSTATUS_CAMERA_RUNNING) {
            updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
        }
        QCAR.onPause();
    }

    public void onDestroy() {
        if (mInitQCARTask != null &&
            mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED) {
            mInitQCARTask.cancel(true);
            mInitQCARTask = null;
        }

        if (mInitCloudRecoTask != null
            && mInitCloudRecoTask.getStatus() != InitCloudRecoTask.Status.FINISHED) {
            mInitCloudRecoTask.cancel(true);
            mInitCloudRecoTask = null;
        }

        synchronized (mShutdownLock) {
            destroyTrackerData();
            deinitApplicationNative();
            deinitTracker();
            deinitCloudReco();
            QCAR.deinit();
        }

        System.gc();
    }

    private void storeScreenDimensions() {
        // Query display dimensions:
        DisplayMetrics metrics = new DisplayMetrics();
        mConsumer.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    private synchronized void updateApplicationStatus(int appStatus) {
        if (mAppStatus == appStatus) {
            return;
        }

        mAppStatus = appStatus;

        switch (mAppStatus) {
            case APPSTATUS_INIT_APP:
                initApplication();
                updateApplicationStatus(APPSTATUS_INIT_QCAR);
                break;

            case APPSTATUS_INIT_QCAR:
                try {
                    mInitQCARTask = new InitQCARTask();
                    mInitQCARTask.execute();
                } catch (Exception e) {
                    RajLog.e("Initializing QCAR SDK failed");
                }
                break;

            case APPSTATUS_INIT_TRACKER:
                setupTracker();
                break;

            case APPSTATUS_INIT_CLOUDRECO:
                if (mUseCloudRecognition) {
                    try {
                        mInitCloudRecoTask = new InitCloudRecoTask();
                        mInitCloudRecoTask.execute();
                    } catch (Exception e) {
                        RajLog.e("Failed to initialize CloudReco");
                    }
                } else {
                    updateApplicationStatus(APPSTATUS_INITED);
                }
                break;

            case APPSTATUS_INIT_APP_AR:
                initApplicationAR();
                updateApplicationStatus(APPSTATUS_INIT_CLOUDRECO);
                break;

            case APPSTATUS_INITED:
                System.gc();
                updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
                break;

            case APPSTATUS_CAMERA_STOPPED:
                stopCamera();
                break;

            case APPSTATUS_CAMERA_RUNNING:
                startCamera();
                setProjectionMatrix();
                mFocusMode = FOCUS_MODE_CONTINUOUS_AUTO;
                if (!setFocusMode(mFocusMode)) {
                    mFocusMode = FOCUS_MODE_NORMAL;
                    setFocusMode(mFocusMode);
                }

                if (!mRajawaliIsInitialized) {
                    initRajawali(mConsumer.getRenderSurface());
                    mRajawaliIsInitialized = true;
                }
                break;

            default:
                throw new RuntimeException("Invalid application state");
        }
    }

    private void initApplication() {
        mConsumer.getActivity().setRequestedOrientation(mScreenOrientation);
        setActivityPortraitMode(mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        storeScreenDimensions();

        mConsumer.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                                                     WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void setScreenOrientation(final int screenOrientation) {
        mScreenOrientation = screenOrientation;
    }

    public int getScreenOrientation() {
        return mScreenOrientation;
    }

    protected void setupTracker() {
        updateApplicationStatus(APPSTATUS_INIT_APP_AR);
    }

    protected void initApplicationAR() {
        initApplicationNative(mScreenWidth, mScreenHeight);
    }

    protected void initRajawali(@NonNull ISurface surface) {
        if (mRenderer == null) {
            RajLog.e("initRajawali(): You need so set a renderer first.");
        }
        surface.setSurfaceRenderer(mRenderer);
        mConsumer.initialize();
    }

    public void setRenderer(ISurfaceRenderer renderer) {
        mRenderer = renderer;
    }

    public void useCloudRecognition(boolean value) {
        mUseCloudRecognition = value;
    }

    protected native void initApplicationNative(int width, int height);

    protected native void setActivityPortraitMode(boolean isPortrait);

    protected native void setMaxSimultaneousImageTargets(int maxSimultaneousImageTargets);

    protected native void deinitApplicationNative();

    protected native void activateAndStartExtendedTracking();

    public native int initTracker(int trackerType);

    protected native int createFrameMarker(int markerId, String markerName, float width, float height);

    protected native int createImageMarker(String dataSetFile);

    public native void deinitTracker();

    private native int destroyTrackerData();

    protected native void startCamera();

    protected native void stopCamera();

    protected native void setProjectionMatrix();

    protected native boolean autofocus();

    protected native boolean setFocusMode(int mode);

    public native int initCloudReco();

    public native void setCloudRecoDatabase(String kAccessKey, String kSecretKey);

    public native void deinitCloudReco();

    public native void enterScanningModeNative();

    public native int initCloudRecoTask();

    public native boolean getScanningModeNative();

    public native String getMetadataNative();

    public native boolean startExtendedTracking();

    public native boolean stopExtendedTracking();

    /**
     * A helper for loading native libraries stored in "libs/armeabi*".
     */
    public static boolean loadLibrary(String nLibName) {
        try {
            System.loadLibrary(nLibName);
            RajLog.i("Native library lib" + nLibName + ".so loaded");
            return true;
        } catch (UnsatisfiedLinkError ulee) {
            RajLog.e("The library lib" + nLibName +
                     ".so could not be loaded");
        } catch (SecurityException se) {
            RajLog.e("The library lib" + nLibName +
                     ".so was not allowed to be loaded");
        }

        return false;
    }
}
