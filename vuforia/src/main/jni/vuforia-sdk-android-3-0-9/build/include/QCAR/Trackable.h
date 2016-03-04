/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    Trackable.h

@brief
    Header file for Trackable class.
===============================================================================*/
#ifndef _QCAR_TRACKABLE_H_
#define _QCAR_TRACKABLE_H_

// Include files
#include <QCAR/NonCopyable.h>
#include <QCAR/Matrices.h>
#include <QCAR/System.h>
#include <QCAR/Type.h>

namespace QCAR
{

/// Base class for all objects that can be tracked.
/**
 *  Every Trackable has a name, an id and a type.
 */
class QCAR_API Trackable : private NonCopyable
{
public:

    /// Returns the Trackable class' type
    static Type getClassType();

    /// Returns the Trackable instance's type
    virtual Type getType() const = 0;

    /// Checks whether the Trackable instance's type equals or has been
    /// derived from a give type
    virtual bool isOfType(Type type) const = 0;
        
    /// Returns a unique id for all 3D trackable objects
    virtual int getId() const = 0;

    /// Returns the Trackable's name
    virtual const char* getName() const = 0;

    /// Sets the given user data for this Trackable. Returns true if successful
    virtual bool setUserData(void* userData) = 0;

    /// Returns the pointer previously set by setUserData()
    virtual void* getUserData() const = 0;

    /// Starts extended tracking for this Trackable. Returns true if successful
    virtual bool startExtendedTracking() = 0;

    /// Stops extended tracking for this Trackable. Returns true if successful
    virtual bool stopExtendedTracking() = 0;

    /// Returns true if extended tracking has been enabled, false otherwise.
    virtual bool isExtendedTrackingStarted() const = 0;

    virtual ~Trackable()  {}
};

} // namespace QCAR

#endif //_QCAR_TRACKABLE_H_
