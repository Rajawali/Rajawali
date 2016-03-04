/*===============================================================================
Copyright (c) 2010-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.

@file 
    Vectors.h

@brief
    Header file for vector structs.
===============================================================================*/
#ifndef _QCAR_VECTOR_H_
#define _QCAR_VECTOR_H_

namespace QCAR 
{

/// 2D vector of float items
struct Vec2F
{
    Vec2F()  {}

    Vec2F(const float* v)
    {
        for(int i=0; i<2; i++)
            data[i]= v[i];
    }

    Vec2F(float v0, float v1)
    {
        data[0] = v0;
        data[1] = v1;
    }

    float data[2];
};


/// 3D vector of float items
struct Vec3F
{
    Vec3F()  {}

    Vec3F(const float* v)
    {
        for(int i=0; i<3; i++)
            data[i]= v[i];
    }

    Vec3F(float v0, float v1, float v2)
    {
        data[0] = v0;
        data[1] = v1;
        data[2] = v2;
    }

    float data[3];
};


/// 4D vector of float items
struct Vec4F
{
    Vec4F()  {}

    Vec4F(const float* v)
    {
        for(int i=0; i<4; i++)
            data[i]= v[i];
    }

    Vec4F(float v0, float v1, float v2, float v3)
    {
        data[0] = v0;
        data[1] = v1;
        data[2] = v2;
        data[3] = v3;
    }

    float data[4];
};


/// 2D vector of int items
struct Vec2I
{
    Vec2I()  {}
    Vec2I(const int* v)
    {
        for(int i=0; i<2; i++)
            data[i]= v[i];
    }

    Vec2I(int v0, int v1)
    {
        data[0] = v0;
        data[1] = v1;
    }

    int data[2];
};


/// 3D vector of int items
struct Vec3I
{
    Vec3I()  {}
    Vec3I(const int* v)
    {
        for(int i=0; i<3; i++)
            data[i]= v[i];
    }

    int data[3];
};


/// 4D vector of int items
struct Vec4I
{
    Vec4I()  {}
    Vec4I(const int* v)
    {
        for(int i=0; i<4; i++)
            data[i]= v[i];
    }

    int data[4];
};

} // namespace QCAR

#endif //_QCAR_VECTOR_H_
