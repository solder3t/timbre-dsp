cmake_minimum_required(VERSION 3.22.1)

project("timbre_dsp")

add_library(
        timbre_dsp
        SHARED
        jni_bridge.cpp
        dsp_engine.cpp
        biquad.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        timbre_dsp
        ${log-lib}
)
