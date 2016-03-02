/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    Image.h

@brief
    Header file for Image class.
===============================================================================*/
#ifndef _QCAR_IMAGE_H_
#define _QCAR_IMAGE_H_

// Include files
#include <QCAR/QCAR.h>
#include <QCAR/NonCopyable.h>

namespace QCAR
{

/// An image as e.g. returned by the CameraDevice object
/**
 *  The image's pixel buffer can have a different size than the
 *  getWidth() and getHeight() methods report. This is e.g. the
 *  case when an image is used for rendering as a texture without
 *  non-power-of-two support.
 *  The real size of the image's pixel buffer can be queried using
 *  getBufferWidth() and getBufferHeight(). 
 */
class QCAR_API Image : private NonCopyable
{
public:
    /// Returns the width of the image in pixels
    /**
     *  getWidth() returns the number of pixels in the pixel buffer that make up
     *  the used image area. The pixel buffer can be wider than this. Use
     *  getBufferWidth() to find out the real width of the pixel buffer.
     */
    virtual int getWidth() const = 0;

    /// Returns the height of the image in pixels
    /**
     *  getHeight() returns the number of pixel rows in the pixel buffer that
     *  make up the used image area. The pixel buffer can have more rows than
     *  that. Use getBufferHeight() to find out the real number of rows that fit
     *  into the buffer.
     */
    virtual int getHeight() const = 0;

    /// Returns the number bytes from one row of pixels to the next row
    /**
     *  Per default the stride is number-of-pixels times bytes-per-pixel.
     *  However, in some cases there can be additional padding bytes at
     *  the end of a row (e.g. to support power-of-two textures).
     */
    virtual int getStride() const = 0;

    /// Returns the number of pixel columns that fit into the pixel buffer
    /**
     *  Per default the number of columns that fit into the pixel buffer
     *  is identical to the width of the image.
     *  However, in some cases there can be additional padding columns at
     *  the right side of an image (e.g. to support power-of-two textures).
     */
    virtual int getBufferWidth() const = 0;

    /// Returns the number of rows that fit into the pixel buffer
    /**
     *  Per default the number of rows that fit into the pixel buffer
     *  is identical to the height of the image.
     *  However, in some cases there can be additional padding rows at
     *  the bottom of an image (e.g. to support power-of-two textures).
     */
    virtual int getBufferHeight() const = 0;

    /// Returns the pixel format of the image
    virtual PIXEL_FORMAT getFormat() const = 0;

    /// Provides read-only access to pixel data
    virtual const void* getPixels() const = 0;

protected:
    virtual ~Image() {}
};

} // namespace QCAR

#endif //_QCAR_IMAGE_H_
