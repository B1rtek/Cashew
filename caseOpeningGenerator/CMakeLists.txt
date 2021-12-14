cmake_minimum_required(VERSION 3.10)
set(PROJECT_NAME "sqlgenLinux")
project(${PROJECT_NAME})

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -lcurl")

add_executable(${PROJECT_NAME} sqlgenLinux.cpp)
target_link_libraries(${PROJECT_NAME} curl)

