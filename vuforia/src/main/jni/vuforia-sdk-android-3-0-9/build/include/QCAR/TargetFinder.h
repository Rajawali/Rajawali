/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    TargetFinder.h

@brief
    Header file for TargetFinder class.
===============================================================================*/
#ifndef _QCAR_TARGET_FINDER_H_
#define _QCAR_TARGET_FINDER_H_

// Include files
#include <QCAR/System.h>
#include <QCAR/TargetSearchResult.h>
#include <QCAR/NonCopyable.h>

namespace QCAR
{

// Forward Declaration
class DataSet;
class ImageTarget;

/// A service that retrieves Targets using cloud-based recognition
class QCAR_API TargetFinder : private NonCopyable
{
public:

    /// Status codes returned by the init() function
    enum
    {
        INIT_DEFAULT = 0,                        ///< Initialization has not started
        INIT_RUNNING = 1,                        ///< Initialization is running
        INIT_SUCCESS = 2,                        ///< Initialization completed successfully
        INIT_ERROR_NO_NETWORK_CONNECTION = -1,   ///< No network connection
        INIT_ERROR_SERVICE_NOT_AVAILABLE = -2    ///< Service is not available
    };

    /// Status codes returned by the updateSearchResults() function
    enum
    {
        UPDATE_NO_MATCH = 0,                     ///< No matches since the last update
        UPDATE_NO_REQUEST = 1,                   ///< No recognition request since the last update
        UPDATE_RESULTS_AVAILABLE = 2,            ///< New search results have been found
        UPDATE_ERROR_AUTHORIZATION_FAILED = -1,  ///< Credentials are wrong or outdated
        UPDATE_ERROR_PROJECT_SUSPENDED = -2,     ///< The specified project was suspended.
        UPDATE_ERROR_NO_NETWORK_CONNECTION = -3, ///< Device has no network connection
        UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4, ///< Server not found, down or overloaded.
        UPDATE_ERROR_BAD_FRAME_QUALITY = -5,     ///< Low frame quality has been continuously observed
        UPDATE_ERROR_UPDATE_SDK = -6,            ///< SDK Version outdated.
        UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7,///< Client/Server clocks too far away.
        UPDATE_ERROR_REQUEST_TIMEOUT = -8        ///< No response to network request after timeout.
    };


    /// Starts initialization of the cloud-based recognition system.
    /**
     * Initialization of the cloud-based recognition system may take significant
     * time and is thus handled in a background process. Use getInitState() to
     * query the initialization progress and result. Pass in the user/password
     * for authenticating with the visual search server.
     */
    virtual bool startInit(const char* userAuth, const char* secretAuth) = 0;

     /// Returns the current state of the initialization process
    /**
     * Returns INIT_SUCCESS if the cloud-based recognition system was
     * initialized successfully. Initialization requires a network connection
     * to be available on the device, otherwise INIT_ERROR_NO_NETWORK_CONNECTION
     * is returned. If the cloud-based recognition service is not available this
     * function will return INIT_ERROR_SERVICE_NOT_AVAILABLE. Returns
     * INIT_DEFAULT if initialization has not been started. Returns INIT_RUNNING
     * if the initialization process has not completed.
     */
    virtual int getInitState() = 0;

    /// Wait for the the cloud-based recognition system initialization to complete.
    /**
     * This functions blocks execution until initialization is complete.
     */
    virtual void waitUntilInitFinished() = 0;

    /// Deinitializes the cloud-based recognition system
    virtual bool deinit() = 0;
    

    /// Starts visual recognition
    /**
     *  Starts continuous recognition of Targets from the cloud.
     *  Use updateSearchResults() and getResult() to retrieve search matches.
     */
    virtual bool startRecognition() = 0;

    /// Stops visual recognition
    virtual bool stop() = 0;



    /// Returns true if the TargetFinder is in 'requesting' mode
    /**
     *  When in 'requesting' mode the TargetFinder has issued a search 
     *  query to the recognition server and is waiting for the results.
     */
    virtual bool isRequesting() = 0;



    /// Update visual search results
    /**
     *  Clears and rebuilds the list of TargetSearchResults with results found
     *  since the last call to updateSearchResults(). Returns the status code
     *  UPDATE_RESULTS_AVAILABLE if new search results have been found.
     *  Targets that are already enabled for tracking are not included
     *  in the list of TargetSearchResults unless the target or its associated
     *  meta data has been updated since they were last enabled for tracking.
     */
    virtual int updateSearchResults() = 0;

    /// Get the number of visual search results
    virtual int getResultCount() const = 0;

    /// Returns a pointer to a search result instance
    /**
     *  Search results are owned by the TargetFinder. Each call to
     *  updateSearchResults() destroys and rebuilds the list of
     *  TargetSearchResult search.
     */
    virtual const TargetSearchResult* getResult(int idx) = 0;



    /// Enable this search result for tracking
    /**
     *  Creates an ImageTarget for local detection and tracking of this target.
     *  The pose of this target will be reported in the Vuforia State. Note that
     *  this call may result in an earlier ImageTarget that was enabled for
     *  tracking to be destroyed. Thus it is not advised to hold a pointer to an
     *  earlier created ImageTarget after calling enableTracking again. Returns
     *  NULL if the target failed to be enabled for tracking.
     */
    virtual ImageTarget* enableTracking(const TargetSearchResult& result) = 0;
    
    /// Clears all targets enabled for tracking
    /**
     *  Destroy all ImageTargets that have been created via enableTracking().
     */
    virtual void clearTrackables() = 0;

    /// Returns the number targets currently enabled for tracking.
    virtual int getNumImageTargets() const = 0;
    
    /// Returns a pointer to an ImageTarget object.
    virtual ImageTarget* getImageTarget(int idx) = 0;



    /// Sets the base color of the scanline in the scanning UI
    /**
     * The parameters allow you to set the Red, Green and Blue colors 
     * for the Scanline. They should be normalized values between 0 and 1.
     */
    virtual void setUIScanlineColor(float r, float g, float b) = 0; 

    /// Sets the base color of the points in the scanning UI
    /**
     * The parameters allow you to set the Red, Green and Blue colors 
     * for the Points. They should be normalized values between 0 and 1.
     * Note that this call triggers the keypoint texture to be recreated and
     * it should thus be avoided to called this every frame.
     */
    virtual void setUIPointColor(float r, float g, float b) = 0; 
};

} // namespace QCAR

#endif //_QCAR_TARGET_FINDER_H_
