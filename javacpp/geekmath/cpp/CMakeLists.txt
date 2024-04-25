cmake_minimum_required(VERSION 3.1...3.29)
project(
    HelloWorld
    VERSION 1.0
    LANGUAGES CXX)

add_library(
    geekmathLib
    SHARED
    src/include/geekmath.hpp
    src/geekmath.cpp
)
target_include_directories(geekmathLib PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/src")