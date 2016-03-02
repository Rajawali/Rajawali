/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    ImageTargetResult.h

@brief
    Header file for ImageTargetResult class.
===============================================================================*/
#ifndef _QCAR_IMAGETARGETRESULT_H_
#define _QCAR_IMAGETARGETRESULT_H_

// Include files
#include <QCAR/TrackableResult.h>
#include <QCAR/ImageTarget.h>

namespace QCAR
{

// Forward declarations:
class VirtualButtonResult;

/// Result for an ImageTarget.
class QCAR_API ImageTargetResult : public TrackableResult
{
public:

    /// Returns the TrackableResult class' type
    static Type getClassType();

    /// Returns the corresponding Trackable that this result represents
    virtual const ImageTarget& getTrackable() const = 0;

    /// Returns the number of VirtualButtons defined for this ImageTarget
    virtual int getNumVirtualButtons() const = 0;

    /// Returns the VirtualButtonResult for a specific VirtualButton
    virtual const VirtualButtonResult* getVirtualButtonResult(int idx) const = 0;

    /// Returns the VirtualButtonResult for a specific VirtualButton
    virtual const VirtualButtonResult* getVirtualButtonResult(const char* name) const = 0;
};

} // namespace QCAR

#endif //_QCAR_IMAGETARGETRESULT_H_
