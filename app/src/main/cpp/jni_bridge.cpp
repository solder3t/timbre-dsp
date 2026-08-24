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
Java_com_timbre_dsp_DSPEngine_processStereoBuffer(JNIEnv* env, jobject /* this */, jlong engineHandle, jfloatArray left, jfloatArray right, jint numSamples) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);

    jfloat* cLeft = env->GetFloatArrayElements(left, nullptr);
    jfloat* cRight = env->GetFloatArrayElements(right, nullptr);
    if (cLeft == nullptr || cRight == nullptr) {
        if (cLeft) env->ReleaseFloatArrayElements(left, cLeft, 0);
        if (cRight) env->ReleaseFloatArrayElements(right, cRight, 0);
        return;
    }

    engine->processStereo(cLeft, cRight, numSamples);

    env->ReleaseFloatArrayElements(left, cLeft, 0);
    env->ReleaseFloatArrayElements(right, cRight, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setBandGain(JNIEnv* env, jobject /* this */, jlong engineHandle, jint index, jfloat gain) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setBandGain(index, gain);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setBandParameters(JNIEnv* env, jobject /* this */, jlong engineHandle, jint index, jint type, jfloat fc, jfloat q, jfloat gain) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setBandParameters(index, type, fc, q, gain);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setPreampGain(JNIEnv* env, jobject /* this */, jlong engineHandle, jfloat gainDb) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setPreampGain(gainDb);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setLimiterEnabled(JNIEnv* env, jobject /* this */, jlong engineHandle, jboolean enabled) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setLimiterEnabled(enabled);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setBassBoost(JNIEnv* env, jobject /* this */, jlong engineHandle, jboolean enabled, jfloat gainDb, jfloat cutoffFreq) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setBassBoost(enabled, gainDb, cutoffFreq);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setCrossfeed(JNIEnv* env, jobject /* this */, jlong engineHandle, jboolean enabled, jfloat strength) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setCrossfeed(enabled, strength);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setImpulseResponse(JNIEnv* env, jobject /* this */, jlong engineHandle, jfloatArray leftIR, jfloatArray rightIR, jint length) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);

    jfloat* cLeft = leftIR != nullptr ? env->GetFloatArrayElements(leftIR, nullptr) : nullptr;
    jfloat* cRight = rightIR != nullptr ? env->GetFloatArrayElements(rightIR, nullptr) : nullptr;

    engine->setImpulseResponse(cLeft, cRight, length);

    if (cLeft != nullptr) env->ReleaseFloatArrayElements(leftIR, cLeft, 0);
    if (cRight != nullptr) env->ReleaseFloatArrayElements(rightIR, cRight, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_timbre_dsp_DSPEngine_setConvolutionEnabled(JNIEnv* env, jobject /* this */, jlong engineHandle, jboolean enabled, jfloat wetDryRatio) {
    if (engineHandle == 0) return;
    auto* engine = reinterpret_cast<dsp::Engine*>(engineHandle);
    engine->setConvolutionEnabled(enabled, wetDryRatio);
}
