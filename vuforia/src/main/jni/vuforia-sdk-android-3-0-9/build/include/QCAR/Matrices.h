/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    Matrices.h

@brief
    Header file for Matrix34F and Matrix44F structs.
===============================================================================*/
#ifndef _QCAR_MATRIX_H_
#define _QCAR_MATRIX_H_

namespace QCAR
{

/// Matrix with 3 rows and 4 columns of float items
struct Matrix34F {
    float data[3*4];   ///< Array of matrix items
};


/// Matrix with 4 rows and 4 columns of float items
struct Matrix44F {
    float data[4*4];   ///< Array of matrix items
};

} // namespace QCAR

#endif //_QCAR_MATRIX_H_
