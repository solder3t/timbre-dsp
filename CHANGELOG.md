# Changelog

All notable changes to Timbre DSP will be documented in this file.

## [1.2.6]
### Added & Improved
- 🌓 **Adaptive Display**: Added responsive 2x2 grid for Theme Mode selector on compact screens, preventing text wrapping on "System" and "AMOLED".
- 🪟 **Frosted Glass Blur**: Matched bottom navigation bar blur with `HazeMaterials.ultraThin()` and shimmer divider styling.
- 📐 **Layout Overflow Fixes**: Fixed power switch clipping on the master DSP card and ensured flexible weights across all header rows.
- 📜 **Full Scroll Clearance**: Configured 96dp bottom padding on all tabs so content never gets stuck behind the bottom bar.
- 🧹 **Shader Overhead Purge**: Removed card-level GPU blur shaders for faster, smoother rendering and lower battery consumption.
- 🔄 **Dynamic Versioning**: `BuildConfig.VERSION_NAME` dynamically binds into `UpdateChecker` and update dialogs.

## [1.2.5]
- 🔄 In-app updater and GitHub release packaging enhancements.
- ⚡ Audio routing and Shizuku dump permission integration improvements.

## [1.2.4]
- 🎛️ Parametric EQ and AutoEq profile integration updates.
- 🔊 Output device auto-binding and preset management fixes.
