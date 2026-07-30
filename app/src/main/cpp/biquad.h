#ifndef TIMBRE_BIQUAD_H
#define TIMBRE_BIQUAD_H

#include <cmath>

namespace dsp {

enum class BiquadType {
    LOWPASS,
    HIGHPASS,
    BANDPASS,
    NOTCH,
    PEAK,
    LOWSHELF,
    HIGHSHELF
};

class Biquad {
public:
    Biquad();
    ~Biquad() = default;

    void setType(BiquadType type);
    void setQ(double q);
    void setFc(double fc);
    void setPeakGain(double peakGaindB);
    void setSampleRate(double sampleRate);
    
    void calculateCoefficients();
    
    // Process a single sample
    float process(float in);

private:
    BiquadType type;
    double a0, a1, a2, b1, b2;
    double Fc, Q, peakGain;
    double z1, z2;
    double sampleRate;
};

} // namespace dsp

#endif // TIMBRE_BIQUAD_H
