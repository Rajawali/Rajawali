/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    TrackableResult.h

@brief
    Header file for TrackableResult class.
===============================================================================*/
#ifndef _QCAR_TRACKABLERESULT_H_
#define _QCAR_TRACKABLERESULT_H_

// Include files
#include <QCAR/NonCopyable.h>
#include <QCAR/Matrices.h>
#include <QCAR/System.h>
#include <QCAR/Trackable.h>

namespace QCAR
{

/// Base class for all result objects.
/**
 *  A TrackableResult is an object that represents the state of a Trackable
 *  which was found in a given frame. Every TrackableResult has a corresponding
 *  Trackable, a type, a 6DOF pose and a status (e.g. tracked).
 */
class QCAR_API TrackableResult : private NonCopyable
{
public:

    /// Returns the TrackableResult class' type
    static Type getClassType();

    /// Returns the TrackableResult instance's type
    virtual Type getType() const = 0;

    /// Checks whether the TrackableResult instance's type equals or has been
    /// derived from a give type
    virtual bool isOfType(Type type) const = 0;

    /// Status of a TrackableResults
    enum STATUS {
        UNKNOWN,            ///< The state of the TrackableResult is unknown
        UNDEFINED,          ///< The state of the TrackableResult is not defined
                            ///< (this TrackableResult does not have a state)
        DETECTED,           ///< The TrackableResult was detected
        TRACKED,            ///< The TrackableResult was tracked
        EXTENDED_TRACKED    ///< The Trackable Result was extended tracked
    };

    /// Returns the tracking status
    virtual STATUS getStatus() const = 0;

    /// Returns the corresponding Trackable that this result represents
    virtual const Trackable& getTrackable() const = 0;

    /// Returns the current pose matrix in row-major order
    virtual const Matrix34F& getPose() const = 0;

    virtual ~TrackableResult()  {}
};

} // namespace QCAR

#endif //_QCAR_TRACKABLERESULT_H_
