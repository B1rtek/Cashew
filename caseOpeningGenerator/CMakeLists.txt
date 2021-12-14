cmake_minimum_required(VERSION 3.10)
set(PROJECT_NAME "sqlgen")
project(${PROJECT_NAME})

set(CMAKE_CXX_STANDARD 14)

if(WIN32)
    add_executable(sqlgenWin32 sqlgenWindows.cpp)
else()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -lcurl")
    add_executable(sqlgenLinux sqlgenLinux.cpp)
    target_link_libraries(${PROJECT_NAME} curl)
endif()

