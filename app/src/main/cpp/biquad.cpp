#include "biquad.h"

namespace dsp {

Biquad::Biquad() {
    type = BiquadType::PEAK;
    a0 = 1.0;
    a1 = 0.0;
    a2 = 0.0;
    b1 = 0.0;
    b2 = 0.0;
    Fc = 1000.0;
    Q = 0.707;
    peakGain = 0.0;
    z1 = 0.0;
    z2 = 0.0;
    sampleRate = 48000.0;
    calculateCoefficients();
}

void Biquad::setType(BiquadType type) {
    this->type = type;
    calculateCoefficients();
}

void Biquad::setQ(double q) {
    this->Q = q;
    calculateCoefficients();
}

void Biquad::setFc(double fc) {
    this->Fc = fc;
    calculateCoefficients();
}

void Biquad::setPeakGain(double peakGaindB) {
    this->peakGain = peakGaindB;
    calculateCoefficients();
}

void Biquad::setSampleRate(double sampleRate) {
    this->sampleRate = sampleRate;
    calculateCoefficients();
}

void Biquad::calculateCoefficients() {
    double norm;
    double V = pow(10, fabs(peakGain) / 20.0);
    double K = tan(M_PI * Fc / sampleRate);
    
    switch (this->type) {
        case BiquadType::PEAK:
            if (peakGain >= 0) {    // boost
                norm = 1 / (1 + 1/Q * K + K * K);
                a0 = (1 + V/Q * K + K * K) * norm;
                a1 = 2 * (K * K - 1) * norm;
                a2 = (1 - V/Q * K + K * K) * norm;
                b1 = a1;
                b2 = (1 - 1/Q * K + K * K) * norm;
            } else {    // cut
                norm = 1 / (1 + V/Q * K + K * K);
                a0 = (1 + 1/Q * K + K * K) * norm;
                a1 = 2 * (K * K - 1) * norm;
                a2 = (1 - 1/Q * K + K * K) * norm;
                b1 = a1;
                b2 = (1 - V/Q * K + K * K) * norm;
            }
            break;
        // Other types like LOWSHELF, HIGHSHELF can be added here
        default:
            // Default to bypass
            a0 = 1.0;
            a1 = 0.0;
            a2 = 0.0;
            b1 = 0.0;
            b2 = 0.0;
            break;
    }
}

float Biquad::process(float in) {
    double out = in * a0 + z1;
    z1 = in * a1 + z2 - b1 * out;
    z2 = in * a2 - b2 * out;
    return (float)out;
}

} // namespace dsp
