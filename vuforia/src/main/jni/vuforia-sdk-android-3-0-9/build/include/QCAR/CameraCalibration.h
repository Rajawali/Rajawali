/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    CameraCalibration.h

@brief
    Header file for CameraCalibration class.
===============================================================================*/
#ifndef _QCAR_CAMERACALIBRATION_H_
#define _QCAR_CAMERACALIBRATION_H_

// Include files
#include <QCAR/Vectors.h>
#include <QCAR/NonCopyable.h>

namespace QCAR
{

/// Holds intrinsic camera parameters
class QCAR_API CameraCalibration : private NonCopyable
{
public:
    /// Returns the resolution of the camera as 2D vector.
    virtual Vec2F getSize() const = 0;

    /// Returns the focal length in x- and y-direction as 2D vector.
    virtual Vec2F getFocalLength() const = 0;

    /// Returns the principal point as 2D vector.
    virtual Vec2F getPrincipalPoint() const = 0;

    /// Returns the radial distortion as 4D vector.
    virtual Vec4F getDistortionParameters() const = 0;

protected:

    virtual ~CameraCalibration() {}
};

} // namespace QCAR

#endif // _QCAR_CAMERACALIBRATION_H_
