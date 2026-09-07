package com.adioevo

import org.junit.Test
import org.junit.Before
import com.adioevo.midi.*
import com.adioevo.audio.*
import com.adioevo.timeline.*
import com.adioevo.project.*

/**
 * Compilation and Integration Tests
 * Verifies all components compile and basic functionality works
 */
class CompilationTest {

    private lateinit var midiEngine: MidiEngine
    private lateinit var sustainPedalHandler: SustainPedalHandler
    private lateinit var midiKeyboardController: MidiKeyboardController
    private lateinit var audioBuffer: AudioBuffer
    private lateinit var audioMixer: AudioMixer
    private lateinit var timelineManager: TimelineManager
    private lateinit var trackManager: TrackManager
    private lateinit var midiNoteEditor: MidiNoteEditor

    @Before
    fun setUp() {
        // All components should instantiate without errors
        sustainPedalHandler = SustainPedalHandler()
        midiKeyboardController = MidiKeyboardController()
        audioBuffer = AudioBuffer(44100, 512)
        audioMixer = AudioMixer(8)
        timelineManager = TimelineManager(44100, 120f, 4, 4)
        trackManager = TrackManager()
        midiNoteEditor = MidiNoteEditor()
    }

    @Test
    fun testSustainPedalHandler() {
        // Test sustain on
        sustainPedalHandler.handleSustainPedal(100)
        assert(sustainPedalHandler.getSustainStatus())

        // Test sustain off
        sustainPedalHandler.handleSustainPedal(50)
        assert(!sustainPedalHandler.getSustainStatus())
    }

    @Test
    fun testMidiKeyboardController() {
        // Test octave change
        midiKeyboardController.setOctave(5)
        assert(midiKeyboardController.currentOctave.value == 5)

        // Test velocity change
        midiKeyboardController.setVelocity(80)
        assert(midiKeyboardController.velocity.value == 80)
    }

    @Test
    fun testAudioBuffer() {
        audioBuffer.startRecording()
        assert(audioBuffer.isRecordingNow())

        // Write samples
        val testData = FloatArray(256) { 0.5f }
        val written = audioBuffer.write(testData)
        assert(written > 0)

        audioBuffer.stop()
        assert(!audioBuffer.isRecordingNow())
    }

    @Test
    fun testAudioMixer() {
        audioMixer.registerTrack(1)
        audioMixer.setTrackVolume(1, 0.8f)
        audioMixer.setTrackPan(1, 0.5f)

        val mixLevel = audioMixer.getMixLevel()
        assert(mixLevel > 0f)
    }

    @Test
    fun testTimelineManager() {
        val samplesPerBeat = timelineManager.getSamplesPerBeat()
        assert(samplesPerBeat > 0)

        val gridInterval = timelineManager.getGridIntervalSamples()
        assert(gridInterval > 0)

        val timeCode = timelineManager.getTimeCodeString(0)
        assert(timeCode.isNotEmpty())
    }

    @Test
    fun testTrackManager() {
        val track = trackManager.createTrack("Test Track", 1)
        assert(track.name == "Test Track")

        trackManager.setTrackVolume(track.id, 0.8f)
        trackManager.toggleMute(track.id)
        assert(trackManager.getTrack(track.id)?.isMuted == true)
    }

    @Test
    fun testMidiNoteEditor() {
        val note = midiNoteEditor.addNote(60, 100, 0, 1000)
        assert(note.pitch == 60)
        assert(note.velocity == 100)

        val notes = midiNoteEditor.getNotes()
        assert(notes.size == 1)

        midiNoteEditor.deleteNote(note)
        assert(midiNoteEditor.getNotes().isEmpty())
    }
}
