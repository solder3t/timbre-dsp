#ifndef TIMBRE_DSP_ENGINE_H
#define TIMBRE_DSP_ENGINE_H

#include <vector>
#include "biquad.h"

namespace dsp {

class Engine {
public:
    Engine();
    ~Engine();

    void process(float* buffer, int numSamples);
    
    // Setup a 10-band parametric EQ as default
    void setBandGain(int index, float gain);

private:
    float mGain;
    std::vector<Biquad> mBands;
};

} // namespace dsp

#endif // TIMBRE_DSP_ENGINE_H
