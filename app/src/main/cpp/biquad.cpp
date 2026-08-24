#include "biquad.h"
#include <cmath>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

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
    this->Q = (q > 0.001) ? q : 0.001;
    calculateCoefficients();
}

void Biquad::setFc(double fc) {
    this->Fc = (fc > 10.0) ? fc : 10.0;
    if (this->Fc > sampleRate * 0.499) {
        this->Fc = sampleRate * 0.499;
    }
    calculateCoefficients();
}

void Biquad::setPeakGain(double peakGaindB) {
    this->peakGain = peakGaindB;
    calculateCoefficients();
}

void Biquad::setSampleRate(double sampleRate) {
    this->sampleRate = (sampleRate > 8000.0) ? sampleRate : 48000.0;
    calculateCoefficients();
}

void Biquad::calculateCoefficients() {
    double w0 = 2.0 * M_PI * Fc / sampleRate;
    double cosw0 = cos(w0);
    double sinw0 = sin(w0);
    double alpha = sinw0 / (2.0 * Q);
    double A = pow(10.0, peakGain / 40.0);
    double norm = 1.0;

    double b0_val = 1.0, b1_val = 0.0, b2_val = 0.0;
    double a0_val = 1.0, a1_val = 0.0, a2_val = 0.0;

    switch (type) {
        case BiquadType::PEAK: {
            b0_val = 1.0 + alpha * A;
            b1_val = -2.0 * cosw0;
            b2_val = 1.0 - alpha * A;
            a0_val = 1.0 + alpha / A;
            a1_val = -2.0 * cosw0;
            a2_val = 1.0 - alpha / A;
            break;
        }
        case BiquadType::LOWSHELF: {
            double sqrtA2 = 2.0 * sqrt(A) * alpha;
            b0_val = A * ((A + 1.0) - (A - 1.0) * cosw0 + sqrtA2);
            b1_val = 2.0 * A * ((A - 1.0) - (A + 1.0) * cosw0);
            b2_val = A * ((A + 1.0) - (A - 1.0) * cosw0 - sqrtA2);
            a0_val = (A + 1.0) + (A - 1.0) * cosw0 + sqrtA2;
            a1_val = -2.0 * ((A - 1.0) + (A + 1.0) * cosw0);
            a2_val = (A + 1.0) + (A - 1.0) * cosw0 - sqrtA2;
            break;
        }
        case BiquadType::HIGHSHELF: {
            double sqrtA2 = 2.0 * sqrt(A) * alpha;
            b0_val = A * ((A + 1.0) + (A - 1.0) * cosw0 + sqrtA2);
            b1_val = -2.0 * A * ((A - 1.0) + (A + 1.0) * cosw0);
            b2_val = A * ((A + 1.0) + (A - 1.0) * cosw0 - sqrtA2);
            a0_val = (A + 1.0) - (A - 1.0) * cosw0 + sqrtA2;
            a1_val = 2.0 * ((A - 1.0) - (A + 1.0) * cosw0);
            a2_val = (A + 1.0) - (A - 1.0) * cosw0 - sqrtA2;
            break;
        }
        case BiquadType::LOWPASS: {
            b0_val = (1.0 - cosw0) / 2.0;
            b1_val = 1.0 - cosw0;
            b2_val = (1.0 - cosw0) / 2.0;
            a0_val = 1.0 + alpha;
            a1_val = -2.0 * cosw0;
            a2_val = 1.0 - alpha;
            break;
        }
        case BiquadType::HIGHPASS: {
            b0_val = (1.0 + cosw0) / 2.0;
            b1_val = -(1.0 + cosw0);
            b2_val = (1.0 + cosw0) / 2.0;
            a0_val = 1.0 + alpha;
            a1_val = -2.0 * cosw0;
            a2_val = 1.0 - alpha;
            break;
        }
        case BiquadType::BANDPASS: {
            b0_val = alpha;
            b1_val = 0.0;
            b2_val = -alpha;
            a0_val = 1.0 + alpha;
            a1_val = -2.0 * cosw0;
            a2_val = 1.0 - alpha;
            break;
        }
        case BiquadType::NOTCH: {
            b0_val = 1.0;
            b1_val = -2.0 * cosw0;
            b2_val = 1.0;
            a0_val = 1.0 + alpha;
            a1_val = -2.0 * cosw0;
            a2_val = 1.0 - alpha;
            break;
        }
    }

    norm = 1.0 / a0_val;
    a0 = b0_val * norm;
    a1 = b1_val * norm;
    a2 = b2_val * norm;
    b1 = a1_val * norm;
    b2 = a2_val * norm;
}

float Biquad::process(float in) {
    double out = in * a0 + z1;
    z1 = in * a1 + z2 - b1 * out;
    z2 = in * a2 - b2 * out;
    return (float)out;
}

} // namespace dsp
