/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    UpdateCallback.h

@brief
    Header file for UpdateCallback class.
===============================================================================*/
#ifndef _QCAR_UPDATECALLBACK_H_
#define _QCAR_UPDATECALLBACK_H_

// Include files
#include <QCAR/System.h>

namespace QCAR
{

// Forward declarations
class State;

/// UpdateCallback interface
class QCAR_API UpdateCallback
{
public:
    /// Called by the SDK right after tracking finishes
    virtual void QCAR_onUpdate(State& state) = 0;
};

} // namespace QCAR

#endif //_QCAR_UPDATECALLBACK_H_
