#include "dsp_engine.h"
#include <cmath>
#include <algorithm>

#if defined(__ARM_NEON) || defined(__aarch64__)
#include <arm_neon.h>
#define HAS_ARM_NEON 1
#else
#define HAS_ARM_NEON 0
#endif

namespace dsp {

Engine::Engine() 
    : mPreampGainLinear(1.0f),
      mSampleRate(48000.0f),
      mLimiterEnabled(true),
      mBassBoostEnabled(false),
      mCrossfeedEnabled(false),
      mCrossfeedStrength(0.5f) {

    double frequencies[] = {31.25, 62.5, 125.0, 250.0, 500.0, 1000.0, 2000.0, 4000.0, 8000.0, 16000.0};
    for (int i = 0; i < 10; ++i) {
        Biquad bqLeft;
        bqLeft.setType(BiquadType::PEAK);
        bqLeft.setSampleRate(mSampleRate);
        bqLeft.setFc(frequencies[i]);
        bqLeft.setQ(1.414);
        bqLeft.setPeakGain(0.0);
        mBandsLeft.push_back(bqLeft);

        Biquad bqRight = bqLeft;
        mBandsRight.push_back(bqRight);
    }

    // Bass Boost setup (Low Shelf at 80Hz)
    mBassBoostLeft.setType(BiquadType::LOWSHELF);
    mBassBoostLeft.setSampleRate(mSampleRate);
    mBassBoostLeft.setFc(80.0);
    mBassBoostLeft.setQ(0.707);
    mBassBoostLeft.setPeakGain(0.0);
    mBassBoostRight = mBassBoostLeft;

    // Crossfeed setup (Lowpass around 700Hz for head-shadowing simulation)
    mCrossfeedLowpassLeft.setType(BiquadType::LOWPASS);
    mCrossfeedLowpassLeft.setSampleRate(mSampleRate);
    mCrossfeedLowpassLeft.setFc(700.0);
    mCrossfeedLowpassLeft.setQ(0.707);
    mCrossfeedLowpassRight = mCrossfeedLowpassLeft;
}

Engine::~Engine() {
}

void Engine::setSampleRate(float sampleRate) {
    std::lock_guard<std::mutex> lock(mLock);
    mSampleRate = sampleRate;
    for (auto& band : mBandsLeft) band.setSampleRate(sampleRate);
    for (auto& band : mBandsRight) band.setSampleRate(sampleRate);
    mBassBoostLeft.setSampleRate(sampleRate);
    mBassBoostRight.setSampleRate(sampleRate);
    mCrossfeedLowpassLeft.setSampleRate(sampleRate);
    mCrossfeedLowpassRight.setSampleRate(sampleRate);
}

void Engine::setPreampGain(float gainDb) {
    std::lock_guard<std::mutex> lock(mLock);
    mPreampGainLinear = powf(10.0f, gainDb / 20.0f);
}

void Engine::setBandGain(int index, float gain) {
    std::lock_guard<std::mutex> lock(mLock);
    if (index >= 0 && index < (int)mBandsLeft.size()) {
        mBandsLeft[index].setPeakGain(gain);
        mBandsRight[index].setPeakGain(gain);
    }
}

void Engine::setBandParameters(int index, int type, float fc, float q, float gain) {
    std::lock_guard<std::mutex> lock(mLock);
    if (index >= (int)mBandsLeft.size()) {
        mBandsLeft.resize(index + 1);
        mBandsRight.resize(index + 1);
    }

    BiquadType bqType = static_cast<BiquadType>(type);
    mBandsLeft[index].setType(bqType);
    mBandsLeft[index].setFc(fc);
    mBandsLeft[index].setQ(q);
    mBandsLeft[index].setPeakGain(gain);

    mBandsRight[index] = mBandsLeft[index];
}

void Engine::setLimiterEnabled(bool enabled) {
    std::lock_guard<std::mutex> lock(mLock);
    mLimiterEnabled = enabled;
}

void Engine::setBassBoost(bool enabled, float gainDb, float cutoffFreq) {
    std::lock_guard<std::mutex> lock(mLock);
    mBassBoostEnabled = enabled;
    mBassBoostLeft.setFc(cutoffFreq);
    mBassBoostLeft.setPeakGain(gainDb);
    mBassBoostRight.setFc(cutoffFreq);
    mBassBoostRight.setPeakGain(gainDb);
}

void Engine::setCrossfeed(bool enabled, float strength) {
    std::lock_guard<std::mutex> lock(mLock);
    mCrossfeedEnabled = enabled;
    mCrossfeedStrength = std::max(0.0f, std::min(1.0f, strength));
}

float Engine::applyLimiter(float sample) {
    if (!mLimiterEnabled) return sample;
    const float threshold = 0.95f;
    if (sample > threshold) {
        return threshold + (1.0f - threshold) * tanhf((sample - threshold) / (1.0f - threshold));
    } else if (sample < -threshold) {
        return -threshold + (1.0f - threshold) * tanhf((sample + threshold) / (1.0f - threshold));
    }
    return sample;
}

void Engine::process(float* buffer, int numSamples) {
    std::lock_guard<std::mutex> lock(mLock);

#if HAS_ARM_NEON
    // Vectorized pre-amp scaling with NEON
    int vecCount = numSamples / 4;
    int remainder = numSamples % 4;
    float32x4_t vGain = vdupq_n_f32(mPreampGainLinear);

    for (int i = 0; i < vecCount; ++i) {
        float32x4_t vSamples = vld1q_f32(buffer + i * 4);
        vSamples = vmulq_f32(vSamples, vGain);
        vst1q_f32(buffer + i * 4, vSamples);
    }
    for (int i = vecCount * 4; i < numSamples; ++i) {
        buffer[i] *= mPreampGainLinear;
    }
#else
    for (int i = 0; i < numSamples; ++i) {
        buffer[i] *= mPreampGainLinear;
    }
#endif

    // Biquad cascading and peak limiting
    for (int i = 0; i < numSamples; ++i) {
        float sample = buffer[i];

        if (mBassBoostEnabled) {
            sample = mBassBoostLeft.process(sample);
        }

        for (auto& band : mBandsLeft) {
            sample = band.process(sample);
        }

        buffer[i] = applyLimiter(sample);
    }
}

void Engine::processStereo(float* left, float* right, int numSamples) {
    std::lock_guard<std::mutex> lock(mLock);

    for (int i = 0; i < numSamples; ++i) {
        float sL = left[i] * mPreampGainLinear;
        float sR = right[i] * mPreampGainLinear;

        // Crossfeed processing
        if (mCrossfeedEnabled) {
            float crossL = mCrossfeedLowpassLeft.process(sR) * mCrossfeedStrength * 0.35f;
            float crossR = mCrossfeedLowpassRight.process(sL) * mCrossfeedStrength * 0.35f;
            sL = (sL * (1.0f - mCrossfeedStrength * 0.15f)) + crossL;
            sR = (sR * (1.0f - mCrossfeedStrength * 0.15f)) + crossR;
        }

        // Bass Boost
        if (mBassBoostEnabled) {
            sL = mBassBoostLeft.process(sL);
            sR = mBassBoostRight.process(sR);
        }

        // EQ bands
        for (size_t b = 0; b < mBandsLeft.size(); ++b) {
            sL = mBandsLeft[b].process(sL);
            sR = mBandsRight[b].process(sR);
        }

        left[i] = applyLimiter(sL);
        right[i] = applyLimiter(sR);
    }
}

} // namespace dsp
