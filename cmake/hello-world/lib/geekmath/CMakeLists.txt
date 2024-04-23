add_library(
    geekmathLib
    src/include/geekmath.hpp
    src/geekmath.cpp
)
target_include_directories(geekmathLib PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/src")