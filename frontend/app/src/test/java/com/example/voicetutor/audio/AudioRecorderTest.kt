package com.example.voicetutor.audio

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AudioRecorder data classes.
 */
class AudioRecorderTest {

    @Test
    fun recordingState_creation_withAllFields_createsCorrectInstance() {
        val state = RecordingState(
            isRecording = true,
            recordingTime = 30,
            audioFilePath = "/path/to/audio.wav",
            isRecordingComplete = false,
            error = null
        )
        
        assertTrue(state.isRecording)
        assertEquals(30, state.recordingTime)
        assertEquals("/path/to/audio.wav", state.audioFilePath)
        assertFalse(state.isRecordingComplete)
        assertNull(state.error)
    }

    @Test
    fun recordingState_creation_withDefaults_usesDefaults() {
        val state = RecordingState()
        
        assertFalse(state.isRecording)
        assertEquals(0, state.recordingTime)
        assertNull(state.audioFilePath)
        assertFalse(state.isRecordingComplete)
        assertNull(state.error)
    }

    @Test
    fun recordingState_copy_createsNewInstance() {
        val original = RecordingState(
            isRecording = true,
            recordingTime = 10,
            audioFilePath = "/path/to/audio.wav"
        )
        
        val copy = original.copy(isRecording = false, recordingTime = 20)
        
        assertFalse(copy.isRecording)
        assertEquals(20, copy.recordingTime)
        assertEquals(original.audioFilePath, copy.audioFilePath)
    }

    @Test
    fun recordingState_equality_worksCorrectly() {
        val state1 = RecordingState(
            isRecording = true,
            recordingTime = 10,
            audioFilePath = "/path/to/audio.wav"
        )
        val state2 = RecordingState(
            isRecording = true,
            recordingTime = 10,
            audioFilePath = "/path/to/audio.wav"
        )
        val state3 = RecordingState(
            isRecording = false,
            recordingTime = 10,
            audioFilePath = "/path/to/audio.wav"
        )
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun recordingState_withError_handlesError() {
        val state = RecordingState(
            isRecording = false,
            error = "Recording failed"
        )
        
        assertFalse(state.isRecording)
        assertEquals("Recording failed", state.error)
    }

    @Test
    fun recordingState_withCompleteState_handlesComplete() {
        val state = RecordingState(
            isRecording = false,
            isRecordingComplete = true,
            audioFilePath = "/path/to/audio.wav"
        )
        
        assertFalse(state.isRecording)
        assertTrue(state.isRecordingComplete)
        assertEquals("/path/to/audio.wav", state.audioFilePath)
    }

    @Test
    fun audioConfig_creation_withAllFields_createsCorrectInstance() {
        val config = AudioConfig(
            sampleRate = 44100,
            channelConfig = android.media.AudioFormat.CHANNEL_IN_STEREO,
            audioFormat = android.media.AudioFormat.ENCODING_PCM_8BIT,
            audioSource = android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION
        )
        
        assertEquals(44100, config.sampleRate)
        assertEquals(android.media.AudioFormat.CHANNEL_IN_STEREO, config.channelConfig)
        assertEquals(android.media.AudioFormat.ENCODING_PCM_8BIT, config.audioFormat)
        assertEquals(android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION, config.audioSource)
    }

    @Test
    fun audioConfig_creation_withDefaults_usesDefaults() {
        val config = AudioConfig()
        
        assertEquals(16000, config.sampleRate)
        assertEquals(android.media.AudioFormat.CHANNEL_IN_MONO, config.channelConfig)
        assertEquals(android.media.AudioFormat.ENCODING_PCM_16BIT, config.audioFormat)
        assertEquals(android.media.MediaRecorder.AudioSource.MIC, config.audioSource)
    }

    @Test
    fun audioConfig_copy_createsNewInstance() {
        val original = AudioConfig(sampleRate = 16000)
        val copy = original.copy(sampleRate = 44100)
        
        assertEquals(44100, copy.sampleRate)
        assertEquals(original.channelConfig, copy.channelConfig)
    }

    @Test
    fun audioConfig_equality_worksCorrectly() {
        val config1 = AudioConfig(sampleRate = 16000)
        val config2 = AudioConfig(sampleRate = 16000)
        val config3 = AudioConfig(sampleRate = 44100)
        
        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }

    @Test
    fun recordingState_hashCode_worksCorrectly() {
        val state1 = RecordingState(isRecording = true, recordingTime = 10)
        val state2 = RecordingState(isRecording = true, recordingTime = 10)
        val state3 = RecordingState(isRecording = false, recordingTime = 10)
        
        assertEquals(state1.hashCode(), state2.hashCode())
        assertNotEquals(state1.hashCode(), state3.hashCode())
    }

    @Test
    fun audioConfig_hashCode_worksCorrectly() {
        val config1 = AudioConfig(sampleRate = 16000)
        val config2 = AudioConfig(sampleRate = 16000)
        val config3 = AudioConfig(sampleRate = 44100)
        
        assertEquals(config1.hashCode(), config2.hashCode())
        assertNotEquals(config1.hashCode(), config3.hashCode())
    }

    @Test
    fun recordingState_toString_containsFields() {
        val state = RecordingState(
            isRecording = true,
            recordingTime = 30,
            audioFilePath = "/path/to/audio.wav"
        )
        
        val toString = state.toString()
        assertTrue(toString.contains("isRecording=true"))
        assertTrue(toString.contains("recordingTime=30"))
        assertTrue(toString.contains("/path/to/audio.wav"))
    }

    @Test
    fun audioConfig_toString_containsFields() {
        val config = AudioConfig(sampleRate = 16000)
        
        val toString = config.toString()
        assertTrue(toString.contains("sampleRate=16000"))
    }

    @Test
    fun recordingState_withZeroTime_handlesCorrectly() {
        val state = RecordingState(recordingTime = 0)
        
        assertEquals(0, state.recordingTime)
        assertFalse(state.isRecording)
    }

    @Test
    fun recordingState_withLongTime_handlesCorrectly() {
        val state = RecordingState(recordingTime = 3600)
        
        assertEquals(3600, state.recordingTime)
    }

    @Test
    fun audioConfig_withDifferentSampleRates_handlesCorrectly() {
        val config1 = AudioConfig(sampleRate = 8000)
        val config2 = AudioConfig(sampleRate = 16000)
        val config3 = AudioConfig(sampleRate = 44100)
        val config4 = AudioConfig(sampleRate = 48000)
        
        assertEquals(8000, config1.sampleRate)
        assertEquals(16000, config2.sampleRate)
        assertEquals(44100, config3.sampleRate)
        assertEquals(48000, config4.sampleRate)
    }
}

