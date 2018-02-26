cmake_minimum_required(VERSION 3.4.1)

# now build app's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall")

add_library(glFramebufferJNI SHARED
            gl_framebuffer.cpp)

# add lib dependencies
target_link_libraries(glFramebufferJNI
                      android
                      log
                      EGL
                      GLESv2)

