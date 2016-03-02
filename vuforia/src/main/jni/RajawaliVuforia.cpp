#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <math.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <QCAR/QCAR.h>
#include <QCAR/CameraDevice.h>
#include <QCAR/Renderer.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Trackable.h>
#include <QCAR/TrackableResult.h>
#include <QCAR/MarkerResult.h>
#include <QCAR/Tool.h>
#include <QCAR/MarkerTracker.h>
#include <QCAR/ImageTracker.h>
#include <QCAR/TrackerManager.h>
#include <QCAR/CameraCalibration.h>
#include <QCAR/Marker.h>
#include <QCAR/UpdateCallback.h>
#include <QCAR/DataSet.h>
#include <QCAR/TargetFinder.h>
#include <QCAR/Tracker.h>
#include <QCAR/ImageTarget.h>
#include <QCAR/CylinderTarget.h>
#include <QCAR/MultiTarget.h>

#include "Utils.h"

#ifdef __cplusplus
extern "C" {
#endif

unsigned int screenWidth = 0;
unsigned int screenHeight = 0;
unsigned int videoWidth = 0;
unsigned int videoHeight = 0;
unsigned int maximumSimultaneousImageTargets = 1;

bool isActivityInPortraitMode = false;
bool activateDataSet = false;
bool activateExtendedTracking = false;
bool isExtendedTrackingActivated = false;

QCAR::DataSet* dataSetToActivate = NULL;

QCAR::Matrix44F projectionMatrix;

//New global vars for Cloud Reco
bool scanningMode = false;
static const size_t CONTENT_MAX = 256;
char lastTargetId[CONTENT_MAX];
char targetMetadata[CONTENT_MAX];

static const char* kAccessKey = NULL;
static const char* kSecretKey = NULL;

class ImageTargets_UpdateCallback: public QCAR::UpdateCallback {
	virtual void QCAR_onUpdate(QCAR::State&) {

		if (dataSetToActivate != NULL) {
			QCAR::TrackerManager& trackerManager =
					QCAR::TrackerManager::getInstance();
			QCAR::ImageTracker* imageTracker =
					static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
							QCAR::ImageTracker::getClassType()));
			if (imageTracker == 0) {
				LOG("Failed to activate data set.");
				return;
			}
			imageTracker->activateDataSet(dataSetToActivate);

			if(isExtendedTrackingActivated)
			{
			    LOG("Activate extended tracking.");
				for (int tIdx = 0; tIdx < dataSetToActivate->getNumTrackables(); tIdx++)
				{
					QCAR::Trackable* trackable = dataSetToActivate->getTrackable(tIdx);
					trackable->startExtendedTracking();
				}
			}

			dataSetToActivate = NULL;
		} else if(activateExtendedTracking == true && isExtendedTrackingActivated == false) {
			LOG("Activating extended tracking!");
			activateExtendedTracking = false;

			QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
		    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
		          trackerManager.getTracker(QCAR::ImageTracker::getClassType()));

		    QCAR::DataSet* currentDataSet = imageTracker->getActiveDataSet();
		    if (imageTracker == 0 || currentDataSet == 0)
		    	return;
			//LOG("NUM TRACKABLES %d", currentDataSet->getNumTrackables());
		    for (int tIdx = 0; tIdx < currentDataSet->getNumTrackables(); tIdx++)
		    {
		    	LOG("TRACKABLE %d", tIdx);
		        QCAR::Trackable* trackable = currentDataSet->getTrackable(tIdx);
		        if(!trackable->startExtendedTracking()) {
		        	LOG("Couldn't start extended tracking");
		        } else {
		        	LOG("Successfully started extended tracking");
		        }
		    }

		    isExtendedTrackingActivated = true;
		}

		if (scanningMode) {
			QCAR::TrackerManager& trackerManager =
					QCAR::TrackerManager::getInstance();
			QCAR::ImageTracker* imageTracker =
					static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
							QCAR::ImageTracker::getClassType()));

			// Get the target finder:
			QCAR::TargetFinder* targetFinder = imageTracker->getTargetFinder();

			// Check if there are new results available:
			const int statusCode = targetFinder->updateSearchResults();

			if (statusCode < 0) {
				char errorMessage[80];
				LOG(errorMessage, "Error with status code %d", statusCode);
			} else if (statusCode
					== QCAR::TargetFinder::UPDATE_RESULTS_AVAILABLE) {
				// Process new search results
				if (targetFinder->getResultCount() > 0) {
					const QCAR::TargetSearchResult* result =
							targetFinder->getResult(0);

					// Check if this target is suitable for tracking:
					if (result->getTrackingRating() > 0) {
						// Create a new Trackable from the result:
						QCAR::Trackable* newTrackable =
								targetFinder->enableTracking(*result);

						if (newTrackable != 0) {
							LOG(
									"Successfully created new trackable '%s' with rating '%d'.",

									newTrackable->getName(), result->getTrackingRating());

							if (strcmp(result->getUniqueTargetId(),
									lastTargetId) != 0) {
								// Copies the new target Metadata
								snprintf(targetMetadata, CONTENT_MAX, "%s",
										result->getMetaData());
								//LOG(targetMetadata);

							}

							strcpy(lastTargetId, result->getUniqueTargetId());

							// Stop Cloud Reco scanning
							targetFinder->stop();

							scanningMode = false;
						}
					}
				}
			}
		}
	}
};

ImageTargets_UpdateCallback updateCallback;

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_setActivityPortraitMode(JNIEnv *,
		jobject, jboolean isPortrait) {
	isActivityInPortraitMode = isPortrait;
}

JNIEXPORT int JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_initTracker(JNIEnv *env,
		jobject object, jint trackerType) {
	LOG("Java_org_rajawali3d_vuforia_VuforiaManager_initTrackerox");

	// Initialize the marker tracker:
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();

	int type = (int) trackerType;

	if (type == 0) {
		QCAR::TrackerManager& trackerManager =
				QCAR::TrackerManager::getInstance();
		QCAR::Tracker* tracker = trackerManager.initTracker(
				QCAR::ImageTracker::getClassType());
		if (tracker == NULL) {
			LOG("Failed to initialize ImageTracker.");
			return 0;
		}

		LOG("Successfully initialized ImageTrackerx.");
	} else if (type == 1) {
		QCAR::Tracker* trackerBase = trackerManager.initTracker(
				QCAR::MarkerTracker::getClassType());
		QCAR::MarkerTracker* markerTracker =
				static_cast<QCAR::MarkerTracker*>(trackerBase);
		if (markerTracker == NULL) {
			LOG("Failed to initialize MarkerTracker.");
			return 0;
		}

		LOG("Successfully initialized MarkerTracker.");
	}

	return 1;
}

JNIEXPORT int JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_createFrameMarker(JNIEnv* env,
		jobject object, jint markerId, jstring markerName, jfloat width,
		jfloat height) {
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::Tracker* trackerBase = trackerManager.getTracker(
			QCAR::MarkerTracker::getClassType());

	if (trackerBase != 0) {
		QCAR::MarkerTracker* markerTracker =
				static_cast<QCAR::MarkerTracker*>(trackerBase);

		const char *nativeString = env->GetStringUTFChars(markerName, NULL);

		if (!markerTracker->createFrameMarker((int) markerId, nativeString,
				QCAR::Vec2F((float) width, (float) height))) {
			LOG("Failed to create frame marker.");
		}
		LOG("Successfully created frame marker.");
		env->ReleaseStringUTFChars(markerName, nativeString);
	}
}

JNIEXPORT int JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_createImageMarker(JNIEnv* env,
		jobject object, jstring dataSetFile) {
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::ImageTracker* imageTracker =
			static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
					QCAR::ImageTracker::getClassType()));

	if (imageTracker == NULL) {
		LOG("Failed to load tracking data set because the ImageTracker has not"
		" been initialized.");
		return 0;
	}

	QCAR::DataSet* dataSet = imageTracker->createDataSet();
	if (dataSet == 0) {
		LOG("Failed to create a new tracking data.");
		return 0;
	}

	// Load the data sets:
	const char *nativeString = env->GetStringUTFChars(dataSetFile, NULL);
	if (!dataSet->load(nativeString, QCAR::DataSet::STORAGE_APPRESOURCE)) {
		LOG("Failed to load data set.");
		env->ReleaseStringUTFChars(dataSetFile, nativeString);
		return 0;
	}
	env->ReleaseStringUTFChars(dataSetFile, nativeString);

	// Activate the data set:
	if (!imageTracker->activateDataSet(dataSet)) {
		LOG("Failed to activate data set.");
		return 0;
	}

	LOG("Successfully loaded and activated data set.");

	return 1;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_deinitTracker(JNIEnv *, jobject) {
	LOG("Java_org_rajawali3d_vuforia_VuforiaManager_deinitTracker");

	// Deinit the marker tracker, this will destroy all created frame markers:
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	if (trackerManager.getTracker(QCAR::MarkerTracker::getClassType()) != NULL)
		trackerManager.deinitTracker(QCAR::MarkerTracker::getClassType());
	if (trackerManager.getTracker(QCAR::ImageTracker::getClassType()) != NULL)
		trackerManager.deinitTracker(QCAR::ImageTracker::getClassType());
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_renderFrame(JNIEnv* env,
		jobject object, jint frameBufferId, jint frameBufferTextureId) {

	//LOG("Java_com_qualcomm_QCARSamples_FrameMarkers_GLRenderer_renderFrame");
	jclass ownerClass = env->GetObjectClass(object);

	// Clear color and depth buffer
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	// Get the state from QCAR and mark the beginning of a rendering section
	QCAR::State state = QCAR::Renderer::getInstance().begin();

	glBindFramebuffer(GL_FRAMEBUFFER, (int)frameBufferId);
	glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, (int)frameBufferTextureId, 0);
	// Explicitly render the Video Background
	QCAR::Renderer::getInstance().drawVideoBackground();
	jfloatArray modelViewMatrixOut = env->NewFloatArray(16);

	// Did we find any trackables this frame?
	for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
		// Get the trackable:
		const QCAR::TrackableResult* trackableResult = state.getTrackableResult(
				tIdx);
		const QCAR::Trackable& trackable = trackableResult->getTrackable();

		QCAR::Matrix44F modelViewMatrix = QCAR::Tool::convertPose2GLMatrix(
				trackableResult->getPose());
		if (isActivityInPortraitMode)
			Utils::rotatePoseMatrix(90.0f, 0, 1.0f, 0,
					&modelViewMatrix.data[0]);
		Utils::rotatePoseMatrix(-90.0f, 1.0f, 0, 0, &modelViewMatrix.data[0]);

		if (trackable.isOfType(QCAR::Marker::getClassType())) {
			jmethodID foundFrameMarkerMethod = env->GetMethodID(ownerClass,
					"foundFrameMarker", "(I[F)V");
			env->SetFloatArrayRegion(modelViewMatrixOut, 0, 16,
					modelViewMatrix.data);
			env->CallVoidMethod(object, foundFrameMarkerMethod,
					(jint) trackable.getId(), modelViewMatrixOut);
		} else if (trackable.isOfType(QCAR::CylinderTarget::getClassType())
				|| trackable.isOfType(QCAR::ImageTarget::getClassType())
				|| trackable.isOfType(QCAR::MultiTarget::getClassType())) {
			jmethodID foundImageMarkerMethod = env->GetMethodID(ownerClass,
					"foundImageMarker", "(Ljava/lang/String;[F)V");
			env->SetFloatArrayRegion(modelViewMatrixOut, 0, 16,
					modelViewMatrix.data);
			const char* trackableName = trackable.getName();
			jstring trackableNameJava = env->NewStringUTF(trackableName);
			env->CallVoidMethod(object, foundImageMarkerMethod,
					trackableNameJava, modelViewMatrixOut);
		}
	}
	env->DeleteLocalRef(modelViewMatrixOut);

	if (state.getNumTrackableResults() == 0) {
		jmethodID noFrameMarkersFoundMethod = env->GetMethodID(ownerClass,
				"noFrameMarkersFound", "()V");
		env->CallVoidMethod(object, noFrameMarkersFoundMethod);
	}

	glBindFramebuffer(GL_FRAMEBUFFER, 0);

	QCAR::Renderer::getInstance().end();
}

// Initialize State Variables for Cloud Reco
void initStateVariables() {
	lastTargetId[0] = '\0';
	scanningMode = false;
}

void configureVideoBackground() {
	// Get the default video mode:
	QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
	QCAR::VideoMode videoMode = cameraDevice.getVideoMode(
			QCAR::CameraDevice::MODE_DEFAULT);

	// Configure the video background
	QCAR::VideoBackgroundConfig config;
	config.mEnabled = true;
	config.mSynchronous = true;
	config.mPosition.data[0] = 0.0f;
	config.mPosition.data[1] = 0.0f;

	if (isActivityInPortraitMode) {
		LOG("configureVideoBackground PORTRAIT");
		config.mSize.data[0] = videoMode.mHeight
				* (screenHeight / (float) videoMode.mWidth);
		config.mSize.data[1] = screenHeight;

		if (config.mSize.data[0] < screenWidth) {
			LOG(
					"Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
			config.mSize.data[0] = screenWidth;
			config.mSize.data[1] = screenWidth
					* (videoMode.mWidth / (float) videoMode.mHeight);
		}
	} else {
		LOG("configureVideoBackground LANDSCAPE");
		config.mSize.data[0] = screenWidth;
		config.mSize.data[1] = videoMode.mHeight
				* (screenWidth / (float) videoMode.mWidth);

		if (config.mSize.data[1] < screenHeight) {
			LOG(
					"Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
			config.mSize.data[0] = screenHeight
					* (videoMode.mWidth / (float) videoMode.mHeight);
			config.mSize.data[1] = screenHeight;
		}
	}

	videoWidth = config.mSize.data[0];
	videoHeight = config.mSize.data[1];

	LOG(
			"Configure Video Background : Video (%d,%d), Screen (%d,%d), mSize (%d,%d)", videoMode.mWidth, videoMode.mHeight, screenWidth, screenHeight, config.mSize.data[0], config.mSize.data[1]);

	// Set the config:
	QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_initApplicationNative(JNIEnv* env,
		jobject obj, jint width, jint height) {
	LOG("Java_org_rajawali3d_vuforia_VuforiaManager_initApplicationNative");
	QCAR::setHint(QCAR::HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, maximumSimultaneousImageTargets);
	QCAR::registerCallback(&updateCallback);
	// Store screen dimensions
	screenWidth = width;
	screenHeight = height;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_deinitApplicationNative(
		JNIEnv* env, jobject obj) {
	isExtendedTrackingActivated = false;
	LOG(
			"Java_org_rajawali3d_vuforia_VuforiaManager_deinitApplicationNative");
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_startCamera(JNIEnv *env,
		jobject object) {
	LOG("Java_org_rajawali3d_vuforia_VuforiaManager_startCamera");

	// Select the camera to open, set this to QCAR::CameraDevice::CAMERA_FRONT
	// to activate the front camera instead.
	QCAR::CameraDevice::CAMERA camera = QCAR::CameraDevice::CAMERA_DEFAULT;

	// Initialize the camera:
	if (!QCAR::CameraDevice::getInstance().init(camera))
		return;

	// Configure the video background
	configureVideoBackground();

	// Select the default mode:
	if (!QCAR::CameraDevice::getInstance().selectVideoMode(
			QCAR::CameraDevice::MODE_DEFAULT))
		return;

	// Start the camera:
	if (!QCAR::CameraDevice::getInstance().start())
		return;

	// Start the tracker:
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::Tracker* markerTracker = trackerManager.getTracker(
			QCAR::MarkerTracker::getClassType());
	if (markerTracker != 0)
		markerTracker->start();

	QCAR::ImageTracker* imageTracker =
			static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
					QCAR::ImageTracker::getClassType()));
	if (imageTracker != 0)
		imageTracker->start();

	// Start cloud based recognition if we are in scanning mode:
	if (scanningMode) {
		QCAR::TargetFinder* targetFinder = imageTracker->getTargetFinder();
		assert(targetFinder != 0);

		targetFinder->startRecognition();
	}
}

JNIEXPORT jfloat JNICALL
Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_getFOV(JNIEnv *env,
		jobject object) {
	const QCAR::CameraCalibration& cameraCalibration =
			QCAR::CameraDevice::getInstance().getCameraCalibration();
	QCAR::Vec2F size = cameraCalibration.getSize();
	QCAR::Vec2F focalLength = cameraCalibration.getFocalLength();
	float fovRadians = 2 * atan(0.5f * size.data[1] / focalLength.data[1]);
	float fovDegrees = fovRadians * 180.0f / M_PI;

	return fovDegrees;
}

JNIEXPORT jint JNICALL
Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_getVideoWidth(JNIEnv *env,
		jobject object) {
	return videoWidth;
}

JNIEXPORT jint JNICALL
Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_getVideoHeight(JNIEnv *env,
		jobject object) {
	return videoHeight;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_stopCamera(JNIEnv *, jobject) {
	LOG("Java_org_rajawali3d_vuforia_VuforiaManager_stopCamera");

	// Stop the tracker:
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::Tracker* markerTracker = trackerManager.getTracker(
			QCAR::MarkerTracker::getClassType());
	if (markerTracker != 0)
		markerTracker->stop();

	QCAR::ImageTracker* imageTracker =
			static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
					QCAR::ImageTracker::getClassType()));
	if (imageTracker != 0)
		imageTracker->stop();

	QCAR::CameraDevice::getInstance().stop();

	// Stop Cloud Reco:
	QCAR::TargetFinder* targetFinder = imageTracker->getTargetFinder();
	assert(targetFinder != 0);

	targetFinder->stop();

	// Clears the trackables
	targetFinder->clearTrackables();

	QCAR::CameraDevice::getInstance().deinit();

	initStateVariables();
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_setProjectionMatrix(JNIEnv *env,
		jobject object) {
	LOG("Java_org_rajawali3d_vuforia_VuforiaManager_setProjectionMatrix");

	// Cache the projection matrix:
	const QCAR::CameraCalibration& cameraCalibration =
			QCAR::CameraDevice::getInstance().getCameraCalibration();
	projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f,
			2500.0f);
}

JNIEXPORT jboolean JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_autofocus(JNIEnv*, jobject) {
	return QCAR::CameraDevice::getInstance().setFocusMode(
			QCAR::CameraDevice::FOCUS_MODE_TRIGGERAUTO) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_setFocusMode(JNIEnv*, jobject,
		jint mode) {
	int qcarFocusMode;

	switch ((int) mode) {
	case 0:
		qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_NORMAL;
		break;

	case 1:
		qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_CONTINUOUSAUTO;
		break;

	case 2:
		qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_INFINITY;
		break;

	case 3:
		qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_MACRO;
		break;

	default:
		return JNI_FALSE;
	}

	return QCAR::CameraDevice::getInstance().setFocusMode(qcarFocusMode) ?
			JNI_TRUE : JNI_FALSE;
}

JNIEXPORT int JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_destroyTrackerData(JNIEnv *env,
		jobject object) {
	LOG("Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_destroyTrackerData");

	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::ImageTracker* imageTracker =
			static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
					QCAR::ImageTracker::getClassType()));
	if (imageTracker == NULL) {
		return 0;
	}

	for (int tIdx = 0; tIdx < imageTracker->getActiveDataSetCount(); tIdx++) {
		QCAR::DataSet* dataSet = imageTracker->getActiveDataSet(tIdx);
		imageTracker->deactivateDataSet(dataSet);
		imageTracker->destroyDataSet(dataSet);
	}

	return 1;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_activateAndStartExtendedTracking(JNIEnv*, jobject) {
	activateExtendedTracking = true;
}

JNIEXPORT jboolean JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_startExtendedTracking(JNIEnv*, jobject)
{
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
          trackerManager.getTracker(QCAR::ImageTracker::getClassType()));

    QCAR::DataSet* currentDataSet = imageTracker->getActiveDataSet();
    if (imageTracker == 0 || currentDataSet == 0)
    	return JNI_FALSE;

    for (int tIdx = 0; tIdx < currentDataSet->getNumTrackables(); tIdx++)
    {
        QCAR::Trackable* trackable = currentDataSet->getTrackable(tIdx);
        if(!trackable->startExtendedTracking())
        	return JNI_FALSE;
    }

    isExtendedTrackingActivated = true;
    return JNI_TRUE;
}


JNIEXPORT jboolean JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_stopExtendedTracking(JNIEnv*, jobject)
{
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
          trackerManager.getTracker(QCAR::ImageTracker::getClassType()));

    QCAR::DataSet* currentDataSet = imageTracker->getActiveDataSet();
    if (imageTracker == 0 || currentDataSet == 0)
    	return JNI_FALSE;

    for (int tIdx = 0; tIdx < currentDataSet->getNumTrackables(); tIdx++)
    {
    	QCAR::Trackable* trackable = currentDataSet->getTrackable(tIdx);
        if(!trackable->stopExtendedTracking())
        	return JNI_FALSE;
    }

    isExtendedTrackingActivated = false;
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_initRendering(JNIEnv* env,
		jobject obj) {
	LOG("Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_initRendering");
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_updateRendering(JNIEnv* env,
		jobject obj, jint width, jint height) {
	LOG("Java_org_rajawali3d_vuforia_RajawaliVuforiaRenderer_updateRendering");

	// Update screen dimensions
	screenWidth = width;
	screenHeight = height;

	// Reconfigure the video background
	configureVideoBackground();
}

JNIEXPORT int JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_initCloudReco(JNIEnv *, jobject) {
	LOG(
			"Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_initCloudReco");

	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::ImageTracker* imageTracker =
			static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
					QCAR::ImageTracker::getClassType()));

	assert(imageTracker != NULL);

	//Get the TargetFinder:
	QCAR::TargetFinder* targetFinder = imageTracker->getTargetFinder();
	assert(targetFinder != NULL);

	// Start initialization:
	if (targetFinder->startInit(kAccessKey, kSecretKey)) {
		targetFinder->waitUntilInitFinished();
	}

	int resultCode = targetFinder->getInitState();
	if (resultCode != QCAR::TargetFinder::INIT_SUCCESS) {
		LOG("Failed to initialize target finder.");
		return resultCode;
	}

	// Use the following calls if you would like to customize the color of the UI
	// targetFinder->setUIScanlineColor(1.0, 0.0, 0.0);
	// targetFinder->setUIPointColor(0.0, 0.0, 1.0);

	return resultCode;
}

JNIEXPORT int JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_deinitCloudReco(JNIEnv *,
		jobject) {

	// Get the image tracker:
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::ImageTracker* imageTracker =
			static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
					QCAR::ImageTracker::getClassType()));

	if (imageTracker == NULL) {
		LOG(
				"Failed to deinit CloudReco as the ImageTracker was not initialized.");
		return 0;
	}

	// Deinitialize Cloud Reco:
	QCAR::TargetFinder* finder = imageTracker->getTargetFinder();
	finder->deinit();

	return 1;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_enterScanningModeNative(JNIEnv*,
		jobject) {
	QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
	QCAR::ImageTracker* imageTracker =
			static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(
					QCAR::ImageTracker::getClassType()));

	assert(imageTracker != 0);

	QCAR::TargetFinder* targetFinder = imageTracker->getTargetFinder();
	assert(targetFinder != 0);

	// Start Cloud Reco
	targetFinder->startRecognition();

	// Clear all trackables created previously:
	targetFinder->clearTrackables();

	scanningMode = true;
}

JNIEXPORT bool JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_getScanningModeNative(JNIEnv*,
		jobject) {
	return scanningMode;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_setCloudRecoDatabase(JNIEnv* env,
		jobject, jstring AccessKey, jstring SecretKey) {
	const jbyte* argvv = (jbyte*) env->GetStringUTFChars(AccessKey, NULL);
	kAccessKey = (char *) argvv;
	argvv = (jbyte*) env->GetStringUTFChars(SecretKey, NULL);
	kSecretKey = (char *) argvv;
}

JNIEXPORT void JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_setMaxSimultaneousImageTargets(JNIEnv* env,
        jobject obj, jint maxSimImageTargets) {
    maximumSimultaneousImageTargets = (int)maxSimImageTargets;
}

JNIEXPORT jstring JNICALL
Java_org_rajawali3d_vuforia_VuforiaManager_getMetadataNative(JNIEnv* env,
		jobject) {
//	char *buf = (char*)malloc(CONTENT_MAX);
//	strcpy(buf, targetMetadata);
	jstring jstrBuf = env->NewStringUTF(targetMetadata);

	return jstrBuf;
}

#ifdef __cplusplus
}
#endif
