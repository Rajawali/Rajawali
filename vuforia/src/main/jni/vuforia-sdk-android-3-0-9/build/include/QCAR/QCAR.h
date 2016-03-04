/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    QCAR.h

@brief
    Header file for global QCAR methods.
===============================================================================*/
#ifndef _QCAR_QCAR_H_
#define _QCAR_QCAR_H_

// Include files
#include <QCAR/System.h>

namespace QCAR
{

// Forward declarations
class UpdateCallback;
class VideoSource;

/// Initialization flags
/**
 *  Use when calling init()
 */
enum INIT_FLAGS {
    GL_11 = 1,          ///< Enables OpenGL ES 1.1 rendering
    GL_20 = 2           ///< Enables OpenGL ES 2.0 rendering
};

/// Return codes for init() function
enum {
    INIT_ERROR = -1,                            ///< Error during initialization
    INIT_DEVICE_NOT_SUPPORTED = -2,             ///< The device is not supported
};


/// Pixel encoding types
enum PIXEL_FORMAT {
    UNKNOWN_FORMAT = 0,         ///< Unknown format - default pixel type for
                                ///< undefined images
    RGB565 = 1,                 ///< A color pixel stored in 2 bytes using 5
                                ///< bits for red, 6 bits for green and 5 bits
                                ///< for blue
    RGB888 = 2,                 ///< A color pixel stored in 3 bytes using
                                ///< 8 bits each
    GRAYSCALE = 4,              ///< A grayscale pixel stored in one byte
    YUV = 8,                    ///< A color pixel stored in 12 or more bits
                                ///< using Y, U and V planes
    RGBA8888 = 16,              ///< A color pixel stored in 32 bits using 8 bits
                                ///< each and an alpha channel.
    INDEXED = 32,               ///< One byte per pixel where the value maps to
                                ///< a domain-specific range.
};


/// Use when calling setHint()
enum HINT {
    /// How many image targets to detect and track at the same time
    /**
     *  This hint tells the tracker how many image shall be processed
     *  at most at the same time. E.g. if an app will never require
     *  tracking more than two targets this value should be set to 2.
     *  Default is: 1.
     */
    HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS = 0,
};

/// Types of storage locations for datasets
enum STORAGE_TYPE {
    STORAGE_APP,            ///< Storage private to the application
    STORAGE_APPRESOURCE,    ///< Storage for assets bundled with the
                            ///< application
    STORAGE_ABSOLUTE        ///< Helper type for specifying an absolute path
};


/// Deinitializes Vuforia
void QCAR_API deinit();


/// Sets a hint for the Vuforia SDK
/**
 *  Hints help the SDK to understand the developer's needs.
 *  However, depending on the device or SDK version the hints
 *  might not be taken into consideration.
 *  Returns false if the hint is unknown or deprecated.
 *  For a boolean value 1 means true and 0 means false.
 */
bool QCAR_API setHint(unsigned int hint, int value);


/// Registers an object to be called when new tracking data is available
void QCAR_API registerCallback(UpdateCallback* object);


/// Enables the delivery of certain pixel formats via the State object
/**
 *  Per default the state object will only contain images in formats
 *  that are required for internal processing, such as gray scale for
 *  tracking. setFrameFormat() can be used to enforce the creation of
 *  images with certain pixel formats. Notice that this might include
 *  additional overhead.
 */
bool QCAR_API setFrameFormat(PIXEL_FORMAT format, bool enabled);


/// Returns the number of bits used to store a single pixel of a given format
/**
 *  Returns 0 if the format is unknown.
 */
int QCAR_API getBitsPerPixel(PIXEL_FORMAT format);


/// Indicates whether the rendering surface needs to support an alpha channel
/// for transparency
bool QCAR_API requiresAlpha();


/// Returns the number of bytes for a buffer with a given size and format
/**
 *  Returns 0 if the format is unknown.
 */
int QCAR_API getBufferSize(int width, int height, PIXEL_FORMAT format);


/// Executes AR-specific tasks upon the onResume activity event
void QCAR_API onResume();


/// Executes AR-specific tasks upon the onResume activity event
void QCAR_API onPause();


/// Executes AR-specific tasks upon the onSurfaceCreated render surface event
void QCAR_API onSurfaceCreated();


/// Executes AR-specific tasks upon the onSurfaceChanged render surface event
void QCAR_API onSurfaceChanged(int width, int height);

} // namespace QCAR

#endif //_QCAR_QCAR_H_
