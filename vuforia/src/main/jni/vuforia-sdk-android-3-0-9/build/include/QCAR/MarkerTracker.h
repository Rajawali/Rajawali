/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    MarkerTracker.h

@brief
    Header file for MarkerTracker class.
===============================================================================*/
#ifndef _QCAR_MARKER_TRACKER_H_
#define _QCAR_MARKER_TRACKER_H_

// Include files
#include <QCAR/Tracker.h>
#include <QCAR/Vectors.h>

namespace QCAR
{

// Forward Declaration
class Marker;

/// MarkerTracker class.
/**
 *  The MarkerTracker tracks rectangular markers and provides methods for
 *  creating and destroying these dynamically.
 *  Note that the methods for creating and destroying markers should not be
 *  called while the MarkerTracker is working at the same time. Doing so will
 *  make these methods block and wait until the MarkerTracker has finished.
 *  The suggested way of doing this is during the execution of UpdateCallback,
 *  which guarantees that the MarkerTracker is not working concurrently.
 *  Alternatively the MarkerTracker can be stopped explicitly.
 */
class QCAR_API MarkerTracker : public Tracker
{
public:

    /// Returns the Tracker class' type
    static Type getClassType();

    /// Creates a new Marker
    /**
     *  Creates a new marker of the given name, size and id. Returns the new
     *  instance on success, NULL otherwise. Use MarkerTracker::destroyMarker
     *  to destroy the returned Marker when it is no longer needed.
     */   
    virtual Marker* createFrameMarker(int markerId, const char* name,
                                    const QCAR::Vec2F& size) = 0;

    /// Destroys a Marker 
    virtual bool destroyMarker(Marker* marker) = 0;

    /// Returns the total number of Markers that have been created.
    virtual int getNumMarkers() const = 0;

    /// Returns a pointer to a Marker object
    virtual Marker* getMarker(int idx) const = 0;
};

} // namespace QCAR

#endif //_QCAR_MARKER_TRACKER_H_
