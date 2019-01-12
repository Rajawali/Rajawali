#!/bin/sh
# Get Android NDK
echo Downloading NDK...
URL=https://dl.google.com/android/repository/android-ndk-r13b-linux-x86_64.zip
wget $URL -O ndk.zip
echo Installing NDK...
unzip ndk.zip 1> /dev/null 2>&1
export ANDROID_NDK_HOME=`pwd`/android-ndk-r13b
chmod a+x $ANDROID_NDK_HOME/ndk-build
echo NDK installed on $ANDROID_NDK_HOME
