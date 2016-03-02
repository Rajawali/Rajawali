/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    VirtualButton.h

@brief
    Header file for VirtualButton class.
===============================================================================*/
#ifndef _QCAR_VIRTUALBUTTON_H_
#define _QCAR_VIRTUALBUTTON_H_

// Include files
#include <QCAR/NonCopyable.h>
#include <QCAR/Vectors.h>

namespace QCAR
{

class Area;


/// A virtual button on a trackable
/**
 *  Methods to modify a VirtualButton must not be called while the
 *  corresponding DataSet is active. The dataset must be deactivated first
 *  before reconfiguring a VirtualButton.
 */
class QCAR_API VirtualButton : private NonCopyable
{
public:
    /// Sensitivity of press detection
    enum SENSITIVITY {
        HIGH,           ///< Fast detection
        MEDIUM,         ///< Balananced between fast and robust
        LOW             ///< Robust detection
    };

    /// Defines a new area for the button area in 3D scene units (the
    /// coordinate system is local to the ImageTarget).
    /**
     *  This method must not be called while the corresponding DataSet is
     *  active or it will return false.
     */
    virtual bool setArea(const Area& area) = 0;

    /// Returns the currently set Area
    virtual const Area& getArea() const = 0;
    
    /// Sets the sensitivity of the virtual button
    /**
     *  Sensitivity allows deciding between fast and robust button press
     *  measurements. This method must not be called while the corresponding
     *  DataSet is active or it will return false.
     */
    virtual bool setSensitivity(SENSITIVITY sensitivity) = 0;

    /// Enables or disables a virtual button
    /**
     *  This method must not be called while the corresponding DataSet is
     *  active or it will return false.
     */
    virtual bool setEnabled(bool enabled) = 0;

    /// Returns true if the virtual button is active (updates while tracking).
    virtual bool isEnabled() const = 0;

    /// Returns the name of the button as ASCII string.
    virtual const char* getName() const = 0;

    /// Returns a unique id for this virtual button.
    virtual int getID() const = 0;

protected:
    virtual ~VirtualButton() {}
};

} // namespace QCAR

#endif //_QCAR_VIRTUALBUTTON_H_
