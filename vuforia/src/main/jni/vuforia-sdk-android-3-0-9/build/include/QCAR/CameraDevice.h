/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    CameraDevice.h

@brief
    Header file for CameraDevice class.
===============================================================================*/
#ifndef _QCAR_CAMERADEVICE_H_
#define _QCAR_CAMERADEVICE_H_

// Include files
#include <QCAR/NonCopyable.h>
#include <QCAR/VideoMode.h>
#include <QCAR/CameraCalibration.h>

namespace QCAR
{

/// Implements access to the phone's built-in camera
class QCAR_API CameraDevice : private NonCopyable
{
public:
    enum MODE
    {
        MODE_DEFAULT = -1,                ///< Default camera mode
        MODE_OPTIMIZE_SPEED = -2,         ///< Fast camera mode
        MODE_OPTIMIZE_QUALITY = -3,       ///< High-quality camera mode
    };

    enum FOCUS_MODE 
    {
        FOCUS_MODE_NORMAL,           ///< Default focus mode
        FOCUS_MODE_TRIGGERAUTO,      ///< Triggers a single autofocus operation
        FOCUS_MODE_CONTINUOUSAUTO,   ///< Continuous autofocus mode
        FOCUS_MODE_INFINITY,         ///< Focus set to infinity
        FOCUS_MODE_MACRO             ///< Macro mode for close-up focus
    };

    enum CAMERA
    {
        CAMERA_DEFAULT,              ///< Default camera device.  Usually BACK
        CAMERA_BACK,                 ///< Rear facing camera
        CAMERA_FRONT                 ///< Front facing camera
    };

    /// Returns the CameraDevice singleton instance.
    static CameraDevice& getInstance();

    /// Initializes the camera.
    virtual bool init(CAMERA camera = CAMERA_DEFAULT) = 0;

    /// Deinitializes the camera.
    /**
     *  Any resources created or used so far are released. Note that this
     *  function should not be called during the execution of the
     *  UpdateCallback and if so will return false.
     */
    virtual bool deinit() = 0;

    /// Starts the camera. Frames are being delivered.
    /**
     *  Depending on the type of the camera it may be necessary to perform
     *  configuration tasks before it can be started.
     */
    virtual bool start() = 0;

    /// Stops the camera if video feed is not required (e.g. in non-AR mode
    /// of an application).
    virtual bool stop()  = 0;

    /// Returns the number of available video modes.
    /**
     *  This is device specific and can differ between mobile devices or operating
     *  system versions.
     */
    virtual int getNumVideoModes() = 0;

    /// Returns the video mode currently selected.
    /**
     *  If no video mode is set then Vuforia chooses a video mode.
     */
    virtual VideoMode getVideoMode(int nIndex) = 0;

    /// Chooses a video mode out of the list of modes
    /*
     *  This function can be only called after the camera device has been
     *  initialized but not started yet. Once you have started the camera and
     *  you need the select another video mode, you need to stop(), deinit(),
     *  then init() the camera before calling selectVideoMode() again.
     */
    virtual bool selectVideoMode(int index) = 0;

    /// Provides read-only access to camera calibration data.
    virtual const CameraCalibration& getCameraCalibration() const = 0;

    /// Enable/disable torch mode if the device supports it.
    /**
     *  Returns true if the requested operation was successful, False
     *  otherwise.
     */
    virtual bool setFlashTorchMode(bool on) = 0;

    /// Set the requested focus mode if the device supports it.
    /**
     *  The allowed values are FOCUS_MODE_NORMAL, FOCUS_MODE_TRIGGERAUTO,
     *  FOCUS_MODE_CONTINUOUSAUTO, FOCUS_MODE_INFINITY, FOCUS_MODE_MACRO,
     *  though not all modes are supported on all devices. Returns true if
     *  the requested operation was successful, False otherwise.
     *  Also note that triggering a single autofocus event using 
     *  FOCUS_MODE_TRIGGERAUTO may stop continuous autofocus if that focus
     *  mode was set earlier.
     */
    virtual bool setFocusMode(int focusMode) = 0;
};

} // namespace QCAR

#endif // _QCAR_CAMERADEVICE_H_
