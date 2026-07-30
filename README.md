# Timbre DSP

Timbre DSP is a high-performance Android audio equalizer and processing engine. 

It provides a dual-mode routing system that applies a custom C++ native 10-band Graphic EQ either via a Shizuku/ADB unrooted hook (Mode A) or via a Magisk system-wide injection (Mode B).

## Architecture

- **The Engine (Backend):** Written in C++ using the NDK for maximum performance. It features a custom 10-band Graphic Equalizer powered by standard Biquad filter implementations (Peak, Notch, Low/High Shelf).
- **The Bridge:** Exposes the C++ engine to the Kotlin frontend using JNI.
- **The UI (Frontend):** Built natively in Kotlin with Jetpack Compose. Includes a Foreground Service to ensure the DSP engine remains active in the background.

## Routing Modes

### Mode A: Shizuku (Unrooted)
Timbre uses an Android `NotificationListenerService` to identify active media sessions. It then attaches a standard `DynamicsProcessing` audio effect to these external `AudioSessionId`s using Shizuku permissions, similar to apps like Wavelet.

### Mode B: Root Injection
For rooted users, Timbre provides a Magisk module template that injects the compiled `libtimbre_dsp.so` native library directly into the Android system audio pipeline, ensuring system-wide processing of all audio.

## Building

Open the project in Android Studio and build normally. The C++ code is integrated via CMake and will compile alongside the Kotlin code.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
