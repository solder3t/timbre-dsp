#include <jni.h>
#include <string>
#include <android/log.h>
#include "dsp_engine.h"

#define LOG_TAG "TimbreJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_timbre_dsp_DSPEngine_createEngine(JNIEnv* env, jobject /* this */) {
    LOGI("Creating DSP Engine");
    auto* engine = new dsp::Engine();
    return reinterpret_cast<jlong>(engine);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_destroyEngine(JNIEnv* env, jobject /* this */, jlong engineHandle) {
    LOGI("Destroying DSP Engine");
    if (engineHandle != 0) {
        auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
        delete engine;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_processBuffer(JNIEnv* env, jobject /* this */, jlong engineHandle, jfloatArray buffer, jint numSamples) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    
    jfloat* cBuffer = env->GetFloatArrayElements(buffer, nullptr);
    if (cBuffer == nullptr) return;

    engine->process(cBuffer, numSamples);

    env->ReleaseFloatArrayElements(buffer, cBuffer, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setBandGain(JNIEnv* env, jobject /* this */, jlong engineHandle, jint index, jfloat gain) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setBandGain(index, gain);
}
