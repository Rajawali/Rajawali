/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    ImageTargetBuilder.h

@brief
    Header file for ImageTargetBuilder class.
===============================================================================*/
#ifndef _QCAR_IMAGE_TARGET_BUILDER_H_
#define _QCAR_IMAGE_TARGET_BUILDER_H_

// Include files
#include <QCAR/System.h>

namespace QCAR
{

class TrackableSource;

/// ImageTargetBuilder
class QCAR_API ImageTargetBuilder
{
public:

   enum FRAME_QUALITY {
       FRAME_QUALITY_NONE = -1, ///< getFrameQualty was called oustside of scanning mode
       FRAME_QUALITY_LOW = 0,   ///< Poor number of features for tracking
       FRAME_QUALITY_MEDIUM,    ///< Sufficient number features for tracking
       FRAME_QUALITY_HIGH,      ///< Ideal number of features for tracking
   };
   

   /// Build an Image Target Trackable source from the next available camera frame 
   /**
    * Build an Image Target Trackable Source from the next available camera frame.
    * This is an asynchronous process, the result of which will be available from
    * getTrackableSource().
    *
    * Note, the ImageTargetBuilder class must be in scan mode for a successful
    * target to be built.  This allows you to provide feedback to the end user
    * as to what the quality of the current frame is before creating a target.
    *
    * This method will return true if the build was successfully started, and false
    * if an invalid name or sceenSizeWidth is provided.
    */
   virtual bool build(const char* name, float sceneSizeWidth) = 0;


   /// Start the scanning mode, allowing calls to getFrameQuality()
   /**
    * Starts the internal frame scanning process, allowing calls to getFrameQuality()
    */
   virtual void startScan() = 0;


   /// Stop the scanning mode
   /**
    * Stop the scanning mode, getFrameQuality will return FRAME_QUALITY_NONE until
    * startScan is called again.  Stopping scan mode will reduce the overall system
    * utilization when not building ImageTargets.
    */
   virtual void stopScan() = 0;

   
   /// Get frame quality, available after startScan is called.
   /**
    * Will return the frame quality for the last available camera frame, a value
    * of FRAME_QUALITY_NONE will be returned if the scanning mode was not enabled.
    * via the startScan() method.
    */
   virtual FRAME_QUALITY getFrameQuality() = 0;

   
   /// Returns a trackable source object to be used in adding a new target to a dataset
   /**
    * This method will return a TrackableSource to be provided to the DataSet.  This 
    * API will return NULL until a trackable source is available.  This trackable
    * source will be provided via this api until build() is called again, at which
    * point it will return NULL again until a successful build step has occured.
    */
   virtual TrackableSource* getTrackableSource() = 0;

protected:
   virtual ~ImageTargetBuilder()  {}

};

} // namespace QCAR

#endif //_QCAR_IMAGE_TARGET_BUILDER_H_
