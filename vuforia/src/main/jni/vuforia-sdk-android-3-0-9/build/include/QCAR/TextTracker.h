/*===============================================================================
Copyright (c) 2013-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    TextTracker.h

@brief
    Header file for TextTracker class.
===============================================================================*/
#ifndef _QCAR_TEXT_TRACKER_H_
#define _QCAR_TEXT_TRACKER_H_

// Include files
#include <QCAR/Tracker.h>
#include <QCAR/Vectors.h>
#include <QCAR/WordList.h>
#include <QCAR/Rectangle.h>

namespace QCAR
{

/// The TextTracker controls the text recognition and tracking sub-system 
/// of Vuforia.
/**
 *  The TextTracker detects and tracks a single or multiple words in six
 *  degrees of freedom (6DOF).
 */
class QCAR_API TextTracker : public Tracker
{
public:
    
    enum UP_DIRECTION
    {
        REGIONOFINTEREST_UP_IS_0_HRS     = 1,
        REGIONOFINTEREST_UP_IS_3_HRS     = 2,
        REGIONOFINTEREST_UP_IS_6_HRS     = 3,
        REGIONOFINTEREST_UP_IS_9_HRS     = 4
    };

    /// Returns the Tracker class' type
    static Type getClassType();

    /// Defines the area of the image where text can be detected and tracked.
    /**
     *  Allows to define rectangular regions that represent the
     *  area where text can be detected and tracked respectively.
     *  For optimal performance the detection window should be kept small.
     *  Larger detection windows will result in longer detection times and may
     *  affect the user experience on some devices. A recommended detection
     *  window size is shown in the sample application.
     *  There is no performance impact to tracking text across the full camera
     *  image, but it may make sense to limit the tracking area if only parts
     *  of the camera image are visible to the user.
     *  The regions are defined in pixel units in the space defined by
     *  the input camera image. Please query the VideoMode from the 
     *  CameraDevice to query the resolution of the input camera image.
     *  Note that the detection window must be fully contained in the tracking
     *  window for this operation to succeed.
     */
    virtual bool setRegionOfInterest(const RectangleInt& detectionROI,
                                     const RectangleInt& trackingROI,
                                     const UP_DIRECTION upDirection) = 0;

    /// Returns the area of the input camera image where text can be detected.
    /**
     *  If no region of interest has been set using setRegionOfInterest, then
     *  the TextTracker will use a default sub-region of the full camera image. 
     *  In this case this function will only return valid values 
     *  after the first camera frame has been processed.
     */
    virtual void getRegionOfInterest(RectangleInt& detectionROI,
                                     RectangleInt& trackingROI,
                                     UP_DIRECTION& upDirection) const = 0;

    /// Returns the area of the input camera image where text can be detected.
    /**
     *  Please note that getRegionOfInterest(RectangleInt&, RectangleInt&,
     *  unsigned int&) is deprecated. Use getRegionOfInterest(RectangleInt&,
     *  RectangleInt&, UP_DIRECTION&) instead.
     *  If no region of interest has been set using setRegionOfInterest, then
     *  the TextTracker will use a default sub-region of the full camera image. 
     *  In this case this function will only return valid values 
     *  after the first camera frame has been processed.
     */
    virtual void getRegionOfInterest(RectangleInt& detectionROI,
                                     RectangleInt& trackingROI,
                                     unsigned int& upDirection) const = 0;

    /// Returns the WordList associated to this tracker.
    virtual WordList* getWordList() = 0;
};

} // namespace QCAR

#endif //_QCAR_TEXT_TRACKER_H_
