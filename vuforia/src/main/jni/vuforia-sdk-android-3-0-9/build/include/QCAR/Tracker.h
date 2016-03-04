/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    Tracker.h

@brief
    Header file for Tracker class.
===============================================================================*/
#ifndef _QCAR_TRACKER_H_
#define _QCAR_TRACKER_H_

// Include files
#include <QCAR/NonCopyable.h>
#include <QCAR/Type.h>

namespace QCAR
{

/// Base class for all tracker types.
/**
 *  The class exposes generic functionality for starting and stopping a
 *  given Tracker as well as querying the tracker type.
 */
class QCAR_API Tracker : private NonCopyable
{
public:

    /// Returns the Tracker class' type
    static Type getClassType();

    /// Returns the Tracker instance's type
    virtual Type getType() const = 0;

    /// Checks whether the Tracker instance's type equals or has been
    /// derived from a give type
    virtual bool isOfType(Type type) const = 0;

    /// Starts the Tracker
    virtual bool start() = 0;

    /// Stops the Tracker
    virtual void stop() = 0;

    virtual ~Tracker() {}
};

} // namespace QCAR

#endif //_QCAR_TRACKER_H_
