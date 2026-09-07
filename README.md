# AdioEVO - Android Music Production Studio

AdioEVO is an original Android music production application featuring multi-track audio recording, MIDI keyboard support, timeline editing, and professional DAW workflows—all optimized for mobile and tablet devices.

## Core Features

- **Multi-track Audio Recording & Editing**: Record and manage multiple audio tracks
- **MIDI Support**: 
  - USB MIDI device detection and connection
  - Bluetooth MIDI support
  - Virtual on-screen MIDI keyboard
  - MIDI IN/OUT device selection
  - Channel, velocity, and sustain pedal (CC64) support
- **Timeline Editor**: Multi-track timeline with clip trimming, splitting, moving, and deletion
- **Audio Features**:
  - WAV/MP3 import
  - Waveform display
  - Track muting, solo, volume, and panning
  - Low-latency audio monitoring
- **MIDI Piano Roll Editor**: Edit MIDI notes with quantization support
- **Playback & Recording**: Play/pause/stop, loop playback, metronome, BPM/time-signature controls
- **Project Management**: Save, load, and export projects to WAV
- **Professional UI**: Original dark-themed DAW interface optimized for mobile and tablets

## Architecture

The project uses a modular architecture with the following structure:

```
AdioEVO/
├── app/                    # Main application module
├── audio-engine/           # Audio engine abstraction
├── midi-engine/            # MIDI handling and device management
├── project/                # Project data and management
├── timeline/               # Timeline and clip editing
├── editor/                 # MIDI and audio editors
└── ui/                     # Jetpack Compose UI components
```

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Coroutines
- **State Management**: StateFlow
- **Audio API**: Android MediaCodec, AudioTrack, AudioRecord
- **MIDI API**: Android MIDI API
- **Database**: Room
- **Build System**: Gradle

## Getting Started

### Requirements

- Android Studio Hedgehog or later
- Android SDK 24 (API level 24) or higher
- Kotlin 1.9+

### Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on a physical device or emulator

**Note**: MIDI functionality requires a physical Android device with USB OTG support or Bluetooth MIDI capability. Emulators have limited MIDI support.

## MIDI Setup

1. Connect a USB MIDI keyboard via USB OTG cable, or pair a Bluetooth MIDI device
2. Navigate to Settings → MIDI Devices
3. Select your MIDI IN device for input
4. Select your MIDI OUT device for output (if needed)
5. Choose the MIDI channel
6. Return to the main screen and open the MIDI Keyboard to begin playing

## Permissions

The application requires the following Android permissions:

- `RECORD_AUDIO` - Audio recording
- `MIDI_ACCESS` - MIDI device access (API 29+)
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_AUDIO` - Import audio files
- `WRITE_EXTERNAL_STORAGE` - Save project files (API < 30)
- `BLUETOOTH` / `BLUETOOTH_CONNECT` - Bluetooth MIDI (API 31+)

## Documentation

See the `docs/` directory for detailed documentation on:

- **MIDI Engine**: MIDI device detection, message handling, sustain pedal logic
- **Audio Engine**: Recording, playback, mixing, low-latency configuration
- **Project Format**: Project file structure and metadata storage
- **UI Architecture**: Compose component structure and state management

## License

This project is developed as an original application. It does not include or derive from proprietary source code of any third-party software.

## Contributing

See CONTRIBUTING.md for guidelines.

---

**Status**: In Active Development

For issues, feature requests, or contributions, please open a GitHub issue or pull request.
