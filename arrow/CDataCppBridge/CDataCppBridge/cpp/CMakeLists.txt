cmake_minimum_required(VERSION 3.1...3.29)
project(
    CDataCppBridge
    VERSION 1.0
    LANGUAGES CXX)
find_package(Arrow REQUIRED)

add_library(
    CDataCppBridgeLib
    SHARED
    src/include/CDataCppBridge.h
    src/CDataCppBridge.cpp
)
target_link_libraries(CDataCppBridgeLib PRIVATE arrow_shared)
target_include_directories(CDataCppBridgeLib PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/src")
