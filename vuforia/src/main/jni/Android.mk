#==============================================================================
#            Copyright (c) 2010-2013 QUALCOMM Austria Research Center GmbH.
#            All Rights Reserved.
#            Qualcomm Confidential and Proprietary
#==============================================================================


# An Android.mk file must begin with the definition of the LOCAL_PATH
# variable. It is used to locate source files in the development tree. Here
# the macro function 'my-dir' (provided by the build system) is used to return
# the path of the current directory.

LOCAL_PATH := $(call my-dir)

# The following section is used for copying the libQCAR.so prebuilt library
# into the appropriate folder (libs/armeabi and libs/armeabi-v7a respectively)
# and setting the include path for library-specific header files.

include $(CLEAR_VARS)
LOCAL_MODULE := Vuforia
LOCAL_SRC_FILES = vuforia-sdk-android-3-0-9/build/lib/$(TARGET_ARCH_ABI)/libVuforia.so
LOCAL_EXPORT_C_INCLUDES := vuforia-sdk-android-3-0-9/build/include
include $(PREBUILT_SHARED_LIBRARY)

#-----------------------------------------------------------------------------

# The CLEAR_VARS variable is provided by the build system and points to a
# special GNU Makefile that will clear many LOCAL_XXX variables for you
# (e.g. LOCAL_MODULE, LOCAL_SRC_FILES, LOCAL_STATIC_LIBRARIES, etc...),
# with the exception of LOCAL_PATH. This is needed because all build
# control files are parsed in a single GNU Make execution context where
# all variables are global.

include $(CLEAR_VARS)

# The LOCAL_MODULE variable must be defined to identify each module you
# describe in your Android.mk. The name must be *unique* and not contain
# any spaces. Note that the build system will automatically add proper
# prefix and suffix to the corresponding generated file. In other words,
# a shared library module named 'foo' will generate 'libfoo.so'.

LOCAL_MODULE := RajawaliVuforia

LOCAL_C_INCLUDES := ./jni/vuforia-sdk-android-3-0-9/build/include

TARGET_ARCH_ABI := armeabi-v7a

# This sample always uses OpenGL ES 2.0.

OPENGLES_LIB  := -lGLESv2
OPENGLES_DEF  := -DUSE_OPENGL_ES_2_0

# An optional set of compiler flags that will be passed when building
# C and C++ source files.
#
# The flag "-Wno-write-strings" removes warnings about deprecated conversion
#   from string constant to 'char*'.
# The flag "-Wno-psabi" removes warning about "mangling of 'va_list' has
#   changed in GCC 4.4" when compiled with certain Android NDK versions.

LOCAL_CFLAGS := -fpic -Wno-write-strings -Wno-psabi $(OPENGLES_DEF)

# The list of additional linker flags to be used when building your
# module. Use the "-l" prefix in front of the name of libraries you want to
# link to your module.

LOCAL_LDLIBS := \
    -llog $(OPENGLES_LIB)

# The list of shared libraries this module depends on at runtime.
# This information is used at link time to embed the corresponding information
# in the generated file. Here we reference the prebuilt library defined earlier
# in this makefile.

LOCAL_SHARED_LIBRARIES := Vuforia

# The LOCAL_SRC_FILES variables must contain a list of C/C++ source files
# that will be built and assembled into a module. Note that you should not
# list header file and included files here because the build system will
# compute dependencies automatically for you, just list the source files
# that will be passed directly to a compiler.

LOCAL_SRC_FILES := RajawaliVuforia.cpp Utils.cpp

# By default, ARM target binaries will be generated in 'thumb' mode, where
# each instruction is 16-bit wide. You can set this variable to 'arm' to
# set the generation of the module's object files to 'arm' (32-bit
# instructions) mode, resulting in potentially faster yet somewhat larger
# binary code.

LOCAL_ARM_MODE := arm

# BUILD_SHARED_LIBRARY is a variable provided by the build system that
# points to a GNU Makefile script being in charge of collecting all the
# information you have defined in LOCAL_XXX variables since the latest
# 'include $(CLEAR_VARS)' statement, determining what and how to build.
# Replace it with the statement BUILD_STATIC_LIBRARY to generate a static
# library instead.

include $(BUILD_SHARED_LIBRARY)
