/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    MultiTargetResult.h

@brief
    Header file for MultiTargetResult class.
===============================================================================*/
#ifndef _QCAR_MULTITARGETRESULT_H_
#define _QCAR_MULTITARGETRESULT_H_

// Include files
#include <QCAR/TrackableResult.h>
#include <QCAR/MultiTarget.h>

namespace QCAR
{

/// Result for a MultiTarget.
class QCAR_API MultiTargetResult : public TrackableResult
{
public:

    /// Returns the TrackableResult class' type
    static Type getClassType();

    /// Returns the corresponding Trackable that this result represents
    virtual const MultiTarget& getTrackable() const = 0;

    /// Returns the number of Trackables that form this MultiTarget
    virtual int getNumPartResults() const = 0;

    // Provides access to the TrackableResult for a specific part
    virtual const TrackableResult* getPartResult(int idx) const = 0;

    // Provides access to the TrackableResult for a specific part
    virtual const TrackableResult* getPartResult(const char* name) const = 0;
};

} // namespace QCAR

#endif //_QCAR_MULTITARGETRESULT_H_
