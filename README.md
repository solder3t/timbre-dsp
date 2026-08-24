# Timbre DSP

**Timbre DSP** is a high-performance Android audio equalizer and processing engine built with modern Android development best practices (Jetpack Compose Material 3, Kotlin Coroutines & StateFlow) and a high-performance C++ NDK backend.

It provides a multi-mode audio routing engine that attaches professional audio processing either via **Shizuku / ADB privileged hooks**, **Android AudioEffect broadcasts**, or **Magisk / KernelSU / APatch root system-wide injection**.

---

## 🌟 Key Features

### 1. 🎛️ Equalizer & Visualizer
- **Interactive Frequency Response Curve:** Real-time Bézier curve rendering magnitude response across 20 Hz – 20,000 Hz with touch-draggable band nodes.
- **10-Band Graphic Equalizer:** Precision octave-band sliders with fine dB steps (-15 dB to +15 dB).
- **AutoEq Database Integration:** Search and apply calibrated target curves for over 4,000+ headphone models (Sony, Apple AirPods, Sennheiser, Bose, Samsung, Moondrop, Audio-Technica, Beyerdynamic, etc.).
- **Rich Preset System:** 14+ built-in genre presets (Flat, Bass Boost, Acoustic, Electronic, Rock, Vocal Booster, etc.) + custom preset saving and management.

### 2. ⚡ Dynamics & Acoustics Engine
- **Pre-Amp Gain:** Master input attenuation/boost (-15 dB to +15 dB).
- **Anti-Clipping Peak Limiter:** Smooth hyperbolic tangent (`tanh`) saturation preventing digital clipping distortion.
- **Sub-Bass Enhancer:** Dedicated low-shelf bass synthesizer with configurable cutoff frequency (40 Hz – 200 Hz) and boost level.
- **Binaural Crossfeed (Chu Moy / Bauer):** Natural acoustic crossfeed for headphones reducing listening fatigue during extended sessions.
- **3D Spatial Widener & High-Frequency Clarity:** Soundstage expansion and vocal brilliance exciters.

### 3. 🔌 Multi-Mode Routing & Session Discovery
- **Shizuku Mode (Unrooted):** Automatic permission requests and IPC binder integration. Queries `dumpsys media.audio_flinger` to hook active playback streams in apps like Spotify, Apple Music, YouTube Music, and Tidal.
- **Broadcast Mode (Standard):** Intercepts standard audio player broadcasts (`ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION`).
- **Root Mode (SU / Magisk / KernelSU / APatch):** In-app Magisk module generator and direct root installer for system-wide AudioFlinger injection.
- **Real-Time Session Tracking:** Live dashboard displaying connected audio sessions and player package names.
- **In-App Real-Time Audio Test Player:** Play musical test chords processed directly through the C++ DSP engine without needing third-party music apps running.

### 4. 📱 Modern UI & System Integration
- **Material 3 Expressive UI:** Tabbed navigation (Equalizer, Effects, Sessions, Setup).
- **Diagnostics & Permission Setup Wizard:** Step-by-step checklist with one-tap action buttons for Shizuku, Root, Notification Listener, and Battery Optimization exemption.
- **Android Quick Settings Tile:** Toggle Timbre DSP on/off instantly from the notification quick settings panel.
- **Foreground Service Notification:** Persistent notification with quick bypass/enable action controls.

---

## 🏗️ Architecture

```
app/
 ├── src/main/cpp/                     # Native C++ DSP Engine
 │    ├── biquad.cpp / biquad.h        # RBJ Audio EQ Cookbook Biquad filter algorithms
 │    ├── dsp_engine.cpp / .h          # Stereo processor, limiter, crossfeed, bass boost
 │    └── jni_bridge.cpp               # JNI bridge between C++ and Kotlin
 └── src/main/java/com/timbre/dsp/     # Kotlin Android Frontend & Services
      ├── audio/                       # AudioEffectManager, AudioSessionTracker, AudioSessionReceiver
      ├── data/                        # PresetRepository, AutoEqRepository
      ├── model/                       # DSPModels, EQBand, EQPreset, AutoEqProfile, PermissionStatus
      ├── permission/                  # PermissionManager (Shizuku, Root, Notifications, Battery)
      ├── service/                     # DSPForegroundService, DSPTileService, MediaSessionListenerService
      └── ui/                          # Jetpack Compose UI
           ├── components/             # EQCurveVisualizer, AutoEqDialog, SavePresetDialog
           ├── dashboard/              # DashboardScreen (Equalizer + Curve + Presets)
           ├── effects/                # EffectsScreen (Dynamics, Bass, Crossfeed, Limiter)
           ├── magisk/                 # MagiskInstallerDialog (Root module generator)
           ├── sessions/               # SessionsScreen (Active Sessions + Test Player)
           └── setup/                  # PermissionSetupSheet (Diagnostics & Setup Wizard)
```

---

## 🚀 Getting Started

### 1. Build and Run
Open the project in Android Studio (Jellyfish / Koala or newer) and build normally:
```bash
./gradlew assembleDebug
```

### 2. Setting Up Permissions
Open the **Setup** tab in the app:
- **Shizuku (Recommended for non-root):** Tap **Authorize Shizuku** and **Grant DUMP Hook**.
- **Root Users:** Tap **Grant DUMP via SU** or **Magisk / KSU Module** to flash the system engine.
- **Manual ADB Command:**
  ```bash
  adb shell pm grant com.timbre.dsp android.permission.DUMP
  ```
- **Notification Access:** Tap **Enable Notification Access** to allow automatic media player detection.

---

## 📄 License
This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
