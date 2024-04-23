cmake_minimum_required(VERSION 3.1...3.29)
project(
    HelloWorld
    VERSION 1.0
    LANGUAGES CXX)

add_subdirectory(lib/geekmath)
add_executable(HelloWorld src/main.cpp)

target_link_libraries(HelloWorld PRIVATE geekmathLib)
