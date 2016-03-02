/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    ImageTarget.h

@brief
    Header file for ImageTarget class.
===============================================================================*/
#ifndef _QCAR_IMAGETARGET_H_
#define _QCAR_IMAGETARGET_H_

// Include files
#include <QCAR/Trackable.h>
#include <QCAR/Vectors.h>

namespace QCAR
{

// Forward declarations
class Area;
class VirtualButton;

/// A flat natural feature target
/**
 *  Methods to modify an ImageTarget must not be called while the
 *  corresponding DataSet is active. The dataset must be deactivated first
 *  before reconfiguring an ImageTarget.
 */
class QCAR_API ImageTarget : public Trackable
{
public:

    /// Returns the Trackable class' type
    static Type getClassType();

    /// Returns the system-wide unique id of the target.
    /**
     *  The target id uniquely identifies an ImageTarget across multiple
     *  Vuforia sessions. The system wide unique id may be generated off-line.
     *  This is opposed to the function getId() which is a dynamically
     *  generated id and which uniquely identifies a Trackable within one run
     *  of Vuforia only.
     */
    virtual const char* getUniqueTargetId() const = 0;

    /// Returns the size (width and height) of the target (in 3D scene units).
    virtual Vec2F getSize() const = 0;

    /// Set the size (width and height) of the target (in 3D scene units).
    /**
     *  The dataset this ImageTarget belongs to must not be active when calling
     *  this function or it will fail. Returns true if the size was set
     *  successfully, false otherwise.
     */
    virtual bool setSize(const Vec2F& size) = 0;

    /// Returns the number of virtual buttons defined for this ImageTarget.
    virtual int getNumVirtualButtons() const = 0;

    /// Provides write access to a specific virtual button.
    virtual VirtualButton* getVirtualButton(int idx) = 0;

    /// Provides read-only access to a specific virtual button.
    virtual const VirtualButton* getVirtualButton(int idx) const = 0;

    /// Returns a virtual button by its name
    /**
     *  Returns NULL if no virtual button with that name
     *  exists in this ImageTarget
     */
    virtual VirtualButton* getVirtualButton(const char* name) = 0;

    /// Returns a virtual button by its name
    /**
     *  Returns NULL if no virtual button with that name
     *  exists in this ImageTarget
     */
    virtual const VirtualButton* getVirtualButton(const char* name) const = 0;

    /// Creates a new virtual button and adds it to the ImageTarget
    /**
     *  Returns NULL if the corresponding DataSet is currently active.
     */
    virtual VirtualButton* createVirtualButton(const char* name, const Area& area) = 0;

    /// Removes and destroys one of the ImageTarget's virtual buttons
    /**
     *  Returns false if the corresponding DataSet is currently active.
     */
    virtual bool destroyVirtualButton(VirtualButton* button) = 0;

    /// Returns the meta data string for this ImageTarget.
    virtual const char* getMetaData() const = 0;

};

} // namespace QCAR

#endif //_QCAR_IMAGETARGET_H_
