# Timbre DSP Magisk Module

This directory contains the template for building the Root-Mode (Mode B) Magisk module for Timbre DSP.

## How it works
To inject the native C++ DSP engine into the system audio pipeline, we must trick the Android OS into loading our compiled `libtimbre_dsp.so` as a global `AudioEffect`.

1. **The Library:** During the Gradle build, copy `app/build/intermediates/cxx/Debug/.../libtimbre_dsp.so` to the `system/lib/soundfx/` (or `lib64`) directory in this module.
2. **The Configuration:** A custom `audio_effects.xml` file needs to be placed in `system/vendor/etc/`. This file tells the Android AudioFlinger to load our `.so` file.
3. **Packaging:** Zip the contents of this folder (`module.prop`, `post-fs-data.sh`, `system/`) into a `.zip` file.
4. **Flashing:** The user installs the zip file via Magisk or KernelSU and reboots.

Once rebooted, the Timbre DSP engine will process all system audio globally!
