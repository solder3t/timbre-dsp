#ifndef TIMBRE_DSP_ENGINE_H
#define TIMBRE_DSP_ENGINE_H

#include <vector>
#include <mutex>
#include "biquad.h"

namespace dsp {

class Engine {
public:
    Engine();
    ~Engine();

    void process(float* buffer, int numSamples);
    void processStereo(float* left, float* right, int numSamples);
    
    // Equalizer controls
    void setBandGain(int index, float gain);
    void setBandParameters(int index, int type, float fc, float q, float gain);
    void setPreampGain(float gainDb);
    void setSampleRate(float sampleRate);

    // Advanced DSP modules
    void setLimiterEnabled(bool enabled);
    void setBassBoost(bool enabled, float gainDb, float cutoffFreq);
    void setCrossfeed(bool enabled, float strength);

    // Convolution engine (.wav / .irs Impulse Response)
    void setImpulseResponse(const float* leftIR, const float* rightIR, int length);
    void setConvolutionEnabled(bool enabled, float wetDryRatio);

private:
    float mPreampGainLinear;
    float mSampleRate;
    bool mLimiterEnabled;
    bool mBassBoostEnabled;
    bool mCrossfeedEnabled;
    float mCrossfeedStrength;

    // Convolution state
    bool mConvolutionEnabled;
    float mConvolutionWetDry;
    std::vector<float> mIrLeft;
    std::vector<float> mIrRight;
    std::vector<float> mConvHistoryLeft;
    std::vector<float> mConvHistoryRight;
    int mConvHistoryIndex;

    std::vector<Biquad> mBandsLeft;
    std::vector<Biquad> mBandsRight;
    Biquad mBassBoostLeft;
    Biquad mBassBoostRight;

    // Crossfeed filters (Bauer / Chu Moy model)
    Biquad mCrossfeedLowpassLeft;
    Biquad mCrossfeedLowpassRight;

    std::mutex mLock;

    float applyLimiter(float sample);
    void applyConvolution(float* left, float* right, int numSamples);
};

} // namespace dsp

#endif // TIMBRE_DSP_ENGINE_H
