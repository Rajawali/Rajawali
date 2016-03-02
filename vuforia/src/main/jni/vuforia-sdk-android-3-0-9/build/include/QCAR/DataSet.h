/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    DataSet.h

@brief
    Header file for DataSet class.
===============================================================================*/
#ifndef _QCAR_DATASET_H_
#define _QCAR_DATASET_H_

// Include files
#include <QCAR/NonCopyable.h>
#include <QCAR/System.h>
#include <QCAR/QCAR.h>

namespace QCAR
{

// Forward declarations:
class Trackable;
class MultiTarget;
class TrackableSource;

/// A container of one or more trackables.
/**
 *  A dataset may contain multiple ImageTargets and MultiTargets.
 *  An empty DataSet instance is created using the DataSet factory function
 *  provided by the ImageTracker class. The dataset is then loaded given a
 *  dataset XML and corresponding dataset DAT file. The dataset may be loaded
 *  from the storage locations defined below. Note that the root filename of the
 *  dataset DAT file and XML file must be the same. Once loaded the dataset can
 *  be activated using ImageTracker::activateDataSet().
 *  Methods to modify a DataSet must not be called while it is active. The
 *  DataSet must be deactivated first before reconfiguring it.
 */
class QCAR_API DataSet : private NonCopyable
{
public:
    
    /// Deprecated enum, use QCAR::STORAGE_TYPE instead.
    /// Types of storage locations for datasets
    enum STORAGE_TYPE {
        STORAGE_APP,            ///< Storage private to the application
        STORAGE_APPRESOURCE,    ///< Storage for assets bundled with the
                                ///< application
        STORAGE_ABSOLUTE        ///< Helper type for specifying an absolute path
    };


    /// Checks if the dataset exists at the specified path and storage location
    /**
     *  Returns true if both the dataset XML and DAT file exist at the
     *  given storage location. The relative path to the dataset XML must be
     *  passed to this function for all storage locations other than
     *  STORAGE_ABSOLUTE.
     */
    static bool exists(const char* path, QCAR::STORAGE_TYPE storageType);

    /// Checks if the dataset exists at the specified path and storage location
    /**
     *  Returns true if both the dataset XML and DAT file exist at the
     *  given storage location. The relative path to the dataset XML must be
     *  passed to this function for all storage locations other than
     *  STORAGE_ABSOLUTE.
     *  
     *  This version is now deprecated, please use QCAR::STORAGE_TYPE based 
     *  method instead.
     */
    static bool exists(const char* path, STORAGE_TYPE storageType);

    /// Loads the dataset at the specified path and storage location
    /**
     *  Returns true if the dataset was loaded successfully. After loading,
     *  individual Trackables can be accessed using getNumTrackables() and
     *  getTrackable(). The relative path to the dataset XML must be passed to
     *  this function for all storage locations other than STORAGE_ABSOLUTE.
     *  Note that loading a dataset may take significant time and therefore
     *  it is recommended to load datasets in the background.
     *
     *  This version is now deprecated, please use QCAR::STORAGE_TYPE based 
     *  method instead.
     */
    virtual bool load(const char* path, STORAGE_TYPE storageType) = 0;

    /// Loads the dataset at the specified path and storage location
    /**
     *  Returns true if the dataset was loaded successfully. After loading,
     *  individual Trackables can be accessed using getNumTrackables() and
     *  getTrackable(). The relative path to the dataset XML must be passed to
     *  this function for all storage locations other than STORAGE_ABSOLUTE.
     *  Note that loading a dataset may take significant time and therefore
     *  it is recommended to load datasets in the background.
     */
    virtual bool load(const char* path, QCAR::STORAGE_TYPE storageType) = 0;

    /// Returns the overall number of 3D trackable objects in this data set.
    /**
     *  Trackables that are part of other trackables (e.g. an ImageTarget that
     *  is part of a MultiTarget) is not counted here and not delivered
     *  by DataSet::getTrackable().
     */
    virtual int getNumTrackables() const = 0;
    
    /// Returns a pointer to a trackable object.
    /**
     *  Trackables that are part of other trackables (e.g. an ImageTarget that
     *  is part of a MultiTarget) is not delivered by this method.
     *  Such trackables can be accesses via the trackable they are part of.
     *  E.g. use MultiTarget::getPart() to access the respective ImageTargets.
     */
    virtual Trackable* getTrackable(int idx) = 0;

    /// Creates a new Trackable from the given TrackableSource and registers
    /// it with the dataset
    /**
     *  Use DataSet::destroy() to destroy the returned Trackable
     *  if it is no longer required.
     *  This method must not be called while the dataset is active or it will
     *  return NULL.
     */
    virtual Trackable* createTrackable(const TrackableSource* source) = 0;

    /// Creates a new MultiTarget and registers it with the dataset
    /**
     *  Use DataSet::destroy() to destroy the returned MultiTarget
     *  if it is no longer required.
     *  This method must not be called while the dataset is active or it will
     *  return NULL.
     */
    virtual MultiTarget* createMultiTarget(const char* name) = 0;

    /// Destroys a Trackable
    /**
     *  This method must not be called while the dataset is active or it will
     *  return false.
     */
    virtual bool destroy(Trackable* trackable) = 0;

    /// Checks if this DataSet's Trackable capacity is reached.
    /**
     *  Returns true if the number of Trackables created in this DataSet
     *  has reached the maximum capacity, false otherwise.
     */
    virtual bool hasReachedTrackableLimit() = 0;

    /// Checks if this dataset is active
    /**
     * Returns true if the dataset is active
     */
    virtual bool isActive() const = 0;

    virtual ~DataSet()  {}
};

} // namespace QCAR

#endif //_QCAR_DATASET_H_
