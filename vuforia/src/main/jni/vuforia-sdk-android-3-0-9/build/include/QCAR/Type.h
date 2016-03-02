/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    Type.h

@brief
    Header file for Type class.
===============================================================================*/
#ifndef _QCAR_TYPE_H_
#define _QCAR_TYPE_H_

// Include files
#include <QCAR/System.h>

namespace QCAR
{

/// Class supporting a Vuforia-internal type system
/**
 *  The size of a Type class instance is only 16 bits, therefore
 *  it should be passed around by value for efficiency reasons.
 */
class QCAR_API Type
{
public:
    
    Type();
    Type(UInt16 data);

    UInt16 getData() const;

    /// Checks whether the type is an exact match with
    /// or has been derived from another type:
    bool isOfType(const Type type) const;

private:
    /// Internal type data:
    UInt16 mData;
};

} // namespace QCAR


#endif // _QCAR_TYPE_H_
