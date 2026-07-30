#include "dsp_engine.h"

namespace dsp {

Engine::Engine() : mGain(1.0f) {
    // Initialize a 10-band Graphic EQ
    double frequencies[] = {31.25, 62.5, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
    for (int i = 0; i < 10; ++i) {
        Biquad bq;
        bq.setType(BiquadType::PEAK);
        bq.setSampleRate(48000.0);
        bq.setFc(frequencies[i]);
        bq.setQ(1.414); // Standard Q for octave bands
        bq.setPeakGain(0.0); // Flat initially
        mBands.push_back(bq);
    }
}

Engine::~Engine() {
    // Cleanup
}

void Engine::setBandGain(int index, float gain) {
    if (index >= 0 && index < mBands.size()) {
        mBands[index].setPeakGain(gain);
    }
}

void Engine::process(float* buffer, int numSamples) {
    for (int i = 0; i < numSamples; ++i) {
        float sample = buffer[i] * mGain;
        // Process sample through each EQ band
        for (auto& band : mBands) {
            sample = band.process(sample);
        }
        buffer[i] = sample;
    }
}

} // namespace dsp
