/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    System.h

@brief
    System specific definitions.
===============================================================================*/
#ifndef _QCAR_SYSTEM_H_
#define _QCAR_SYSTEM_H_

// Include files
#if defined(_WIN32_WCE) || defined(WIN32)
#  define QCAR_IS_WINDOWS
#endif


// Define exporting/importing of methods from module
//
#ifdef QCAR_IS_WINDOWS

#  ifdef QCAR_EXPORTS
#    define QCAR_API __declspec(dllexport)
#  elif defined(QCAR_STATIC)
#    define QCAR_API
#  else
#    define QCAR_API __declspec(dllimport)
#  endif

#else // !QCAR_IS_WINDOWS

#  ifdef QCAR_EXPORTS
#    define QCAR_API __attribute__((visibility("default"))) 
#  elif defined(QCAR_STATIC)
#    define QCAR_API
#  else
#    define QCAR_API __attribute__((visibility("default")))
#  endif

#endif


// Platform defines
#ifdef QCAR_IS_WINDOWS

namespace QCAR
{
    typedef unsigned __int16 UInt16;
}

#else // !QCAR_IS_WINDOWS

#include <stdio.h> 

namespace QCAR
{
    typedef __uint16_t UInt16;
}

#endif

#endif // _QCAR_SYSTEM_H_
