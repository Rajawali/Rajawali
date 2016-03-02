/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    VideoBackgroundConfig.h

@brief
    Header file for VideoBackgroundConfig struct.
===============================================================================*/
#ifndef _QCAR_VIDEOBACKGROUNDCONFIG_H_
#define _QCAR_VIDEOBACKGROUNDCONFIG_H_

// Include files
#include <QCAR/Vectors.h>

namespace QCAR
{

enum VIDEO_BACKGROUND_REFLECTION
{
    VIDEO_BACKGROUND_REFLECTION_DEFAULT,  ///< Allows the SDK to set the recommended reflection settings for the current camera
    VIDEO_BACKGROUND_REFLECTION_ON,       ///< Overrides the SDK recommendation to force a reflection
    VIDEO_BACKGROUND_REFLECTION_OFF       ///< Overrides the SDK recommendation to disable reflection
};

/// Video background configuration
struct VideoBackgroundConfig
{
    /// Constructor to provide basic initalization. 
    VideoBackgroundConfig()
    {
        mEnabled = true;
        mSynchronous = true;
        mPosition.data[0] = 0;
        mPosition.data[1] = 0;
        mSize.data[0] = 0;
        mSize.data[1] = 0;
        mReflection = VIDEO_BACKGROUND_REFLECTION_DEFAULT;
    }

    /// Enables/disables rendering of the video background.
    bool mEnabled;

    /// Enables/disables synchronization of render and camera frame rate.
    /**
     *  If synchronization is enabled the SDK will attempt to match the
     *  rendering frame rate with the camera frame rate. This may result
     *  in a performance gain as potentially redundant render calls are
     *  avoided. Enabling this is not recommended if your augmented content
     *  needs to be animated at a rate higher than the rate at which the
     *  camera delivers frames.
     */
    bool mSynchronous;

    /// Relative position of the video background in the render target in
    /// pixels.
    /**
     *  Describes the offset of the center of video background to the
     *  center of the screen (viewport) in pixels. A value of (0,0) centers the
     *  video background, whereas a value of (-10,15) moves the video background
     *  10 pixels to the left and 15 pixels upwards.
     */
    Vec2I mPosition;

    /// Width and height of the video background in pixels
    /**
     *  Using the device's screen size for this parameter scales the image to
     *  fullscreen. Notice that if the camera's aspect ratio is different than
     *  the screen's aspect ratio this will create a non-uniform stretched
     *  image.
     */
    Vec2I mSize;

    /// Reflection parameter to control how the video background is rendered
    /**
     *  By setting this to VIDEO_BACKGROUND_REFLECTION_DEFAULT, the SDK will
     *  update the projection matrix and video background automatically to provide
     *  the best AR mode possible for the given camera on your specific device.
     *  For the BACK camera, this will generally result in no reflection at all.
     *  For the FRONT camera, this will generally result in a reflection to provide
     *  an "AR Mirror" effect.
     *  
     *  This can also be overridden by selecting VIDEO_BACKGROUND_REFLECTION_ON or
     *  VIDEO_BACKGROUND_REFLECTION_OFF.  This may be desirable in advanced use
     *  cases.
     */
    VIDEO_BACKGROUND_REFLECTION mReflection;
};

} // namespace QCAR

#endif //_QCAR_RENDERER_H_
