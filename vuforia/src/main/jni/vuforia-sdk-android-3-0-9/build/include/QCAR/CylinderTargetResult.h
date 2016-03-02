/*===============================================================================
Copyright (c) 2013-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    CylinderTargetResult.h

@brief
    Header file for CylinderTargetResult class.
===============================================================================*/
#ifndef _QCAR_CYLINDERTARGETRESULT_H_
#define _QCAR_CYLINDERTARGETRESULT_H_

// Include files
#include <QCAR/TrackableResult.h>
#include <QCAR/CylinderTarget.h>

namespace QCAR
{

/// Result for a CylinderTarget.
class QCAR_API CylinderTargetResult : public TrackableResult
{
public:

    /// Returns the TrackableResult class' type
    static Type getClassType();

    /// Returns the corresponding Trackable that this result represents
    virtual const CylinderTarget& getTrackable() const = 0;
};

} // namespace QCAR

#endif //_QCAR_CYLINDERTARGETRESULT_H_
