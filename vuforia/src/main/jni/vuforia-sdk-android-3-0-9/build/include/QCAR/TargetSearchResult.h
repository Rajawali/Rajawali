/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    TargetSearchResult.h

@brief
    Header file for TargetSearchResult class.
===============================================================================*/
#ifndef _QCAR_TARGET_SEARCH_RESULT_H_
#define _QCAR_TARGET_SEARCH_RESULT_H_

// Include files
#include <QCAR/System.h>
#include <QCAR/NonCopyable.h>

namespace QCAR
{

/// A search result of a found target returned by the TargetFinder
class TargetSearchResult : private NonCopyable
{
public:
    /// Returns the name of the target
    virtual const char* getTargetName() const = 0;

    /// Returns the system-wide unique id of the target.
    virtual const char* getUniqueTargetId() const = 0;

    /// Returns the width of the target (in 3D scene units)
    virtual const float getTargetSize() const = 0;

    /// Returns the metadata associated with this target
    virtual const char* getMetaData() const = 0;

    /// Returns the tracking rating for this target
    /**
     *  The tracking rating represents a 5-star rating describing the
     *  suitability of this target for tracking on a scale from 0 to 5. A low
     *  tracking rating may result in poor tracking or unstable augmentation.
     */
    virtual unsigned char getTrackingRating() const = 0;
};

} // namespace QCAR

#endif //_QCAR_TARGET_SEARCH_RESULT_H_
