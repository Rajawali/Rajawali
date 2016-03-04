/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    Tool.h

@brief
    Header file for global Tool functions.
===============================================================================*/
#ifndef _QCAR_TOOL_H_
#define _QCAR_TOOL_H_

// Include files
#include <QCAR/System.h>
#include <QCAR/Matrices.h>
#include <QCAR/Vectors.h>

namespace QCAR
{

// Forward declarations
class CameraCalibration;

/// Tool functions
namespace Tool
{
    /// Returns a 4x4 col-major OpenGL model-view matrix from a 3x4 Vuforia pose
    /// matrix.
    /**
     *  Vuforia uses 3x4 row-major matrices for pose data. convertPose2GLMatrix()
     *  takes such a pose matrix and returns an OpenGL compatible model-view
     *  matrix.
     */
    QCAR_API Matrix44F convertPose2GLMatrix(const Matrix34F& pose);

    /// Returns an OpenGL style projection matrix.
    QCAR_API Matrix44F getProjectionGL(const CameraCalibration& calib,
                                       float nearPlane, float farPlane);

    /// Projects a 3D scene point into the camera image(device coordinates)
    /// given a pose in form of a 3x4 matrix.
    /**
     *  The projectPoint() function takes a 3D point in scene coordinates and
     *  transforms it using the given pose matrix. It then projects it into the
     *  camera image (pixel coordinates) using the given camera calibration.
     *  Note that camera coordinates are usually different from screen
     *  coordinates, since screen and camera resolution can be different.
     *  Transforming from camera to screen coordinates requires another
     *  transformation using the settings applied to the Renderer via the
     *  VideoBackgroundConfig structure.
     */
    QCAR_API Vec2F projectPoint(const CameraCalibration& calib,
                                const Matrix34F& pose, const Vec3F& point);

    /// Multiplies two Vuforia pose matrices
    /**
     *  In order to apply a transformation A on top of a transformation B,
     *  perform: multiply(B,A).
     */
    QCAR_API Matrix34F multiply(const Matrix34F& matLeft,
                                const Matrix34F& matRight);

    /// Multiplies two Vuforia-style 4x4-matrices (row-major order)
    QCAR_API Matrix44F multiply(const Matrix44F& matLeft,
                                const Matrix44F& matRight);

    /// Multiplies 1 vector and 1 Vuforia-style 4x4-matrix (row-major order)
    QCAR_API Vec4F multiply(const Vec4F& vec,
                            const Matrix44F& mat);

    /// Multiplies two GL-style matrices (col-major order)
    QCAR_API Matrix44F multiplyGL(const Matrix44F& matLeft,
                                  const Matrix44F& matRight);

    /// Sets the translation part of a 3x4 pose matrix
    QCAR_API void setTranslation(Matrix34F& pose,
                                 const Vec3F& translation);

    /// Sets the rotation part of a 3x4 pose matrix using axis-angle as input
    /**
     *  The axis parameter defines the 3D axis around which the pose rotates.
     *  The angle parameter defines the angle in degrees for the rotation
     *  around that axis.
     */
    QCAR_API void setRotation(Matrix34F& pose,
                              const Vec3F& axis, float angle);

} // namespace Tool

} // namespace QCAR

#endif //_QCAR_TOOL_H_
