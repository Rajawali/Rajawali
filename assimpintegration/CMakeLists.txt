# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

find_library( log-lib log )

find_library( z-lib z )

add_subdirectory( ../assimp assimpbin )

include_directories(
                    ../assimp/include
                    ${CMAKE_CURRENT_BINARY_DIR}/assimpbin/include/assimp
                    ${CMAKE_CURRENT_BINARY_DIR}/assimpbin
)

FILE(GLOB assimp_src
    "../assimp/code/*.cpp"
    "../assimp/code/*.c"
    "../assimp/code/*.h"
    "../assimp/code/*.hpp"
)

MESSAGE( STATUS "ASSIMP Sources: " ${assimp_src} )

add_library( assimp-native SHARED src/main/cpp/native.c ${assimp_src} )

target_link_libraries( assimp-native ${log-lib} ${z-lib} )