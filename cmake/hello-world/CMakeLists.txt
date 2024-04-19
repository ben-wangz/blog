cmake_minimum_required(VERSION 3.1...3.29)
project(
    HelloWorld
    VERSION 1.0
    LANGUAGES CXX)

add_library(HelloWorldLib src/include/ src/math.cpp )
add_executable(HelloWorld src/main.cpp)

target_link_libraries(HelloWorld PRIVATE HelloWorldLib)
