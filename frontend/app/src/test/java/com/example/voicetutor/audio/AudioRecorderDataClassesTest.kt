package com.example.voicetutor.audio

import android.media.AudioFormat
import android.media.MediaRecorder
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AudioRecorder data classes
 */
class AudioRecorderDataClassesTest {
    
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
    fun recordingState_creation_withAllFields_createsCorrectInstance() {
        val state = RecordingState(
            isRecording = true,
            recordingTime = 60,
            audioFilePath = "/path/to/audio.wav",
            isRecordingComplete = false,
            error = null
        )
        
        assertTrue(state.isRecording)
        assertEquals(60, state.recordingTime)
        assertEquals("/path/to/audio.wav", state.audioFilePath)
        assertFalse(state.isRecordingComplete)
        assertNull(state.error)
    }
    
    @Test
    fun recordingState_withError_storesError() {
        val state = RecordingState(
            isRecording = false,
            error = "Recording failed"
        )
        
        assertFalse(state.isRecording)
        assertEquals("Recording failed", state.error)
    }
    
    @Test
    fun recordingState_copy_createsNewInstance() {
        val original = RecordingState(isRecording = false, recordingTime = 0)
        val copy = original.copy(isRecording = true, recordingTime = 30)
        
        assertTrue(copy.isRecording)
        assertEquals(30, copy.recordingTime)
        assertFalse(original.isRecording)
    }
    
    @Test
    fun recordingState_equality_worksCorrectly() {
        val state1 = RecordingState(isRecording = true, recordingTime = 60)
        val state2 = RecordingState(isRecording = true, recordingTime = 60)
        val state3 = RecordingState(isRecording = false, recordingTime = 60)
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }
    
    @Test
    fun recordingState_toString_containsFields() {
        val state = RecordingState(isRecording = true, recordingTime = 60)
        val string = state.toString()
        
        assertTrue(string.contains("true") || string.contains("isRecording"))
    }
    
    @Test
    fun audioConfig_creation_withDefaults_usesCorrectDefaults() {
        val config = AudioConfig()
        
        assertEquals(16000, config.sampleRate)
        assertEquals(AudioFormat.CHANNEL_IN_MONO, config.channelConfig)
        assertEquals(AudioFormat.ENCODING_PCM_16BIT, config.audioFormat)
        assertEquals(MediaRecorder.AudioSource.MIC, config.audioSource)
    }
    
    @Test
    fun audioConfig_creation_withCustomValues_storesCorrectly() {
        val config = AudioConfig(
            sampleRate = 44100,
            channelConfig = AudioFormat.CHANNEL_IN_STEREO,
            audioFormat = AudioFormat.ENCODING_PCM_16BIT,
            audioSource = MediaRecorder.AudioSource.MIC
        )
        
        assertEquals(44100, config.sampleRate)
        assertEquals(AudioFormat.CHANNEL_IN_STEREO, config.channelConfig)
    }
    
    @Test
    fun audioConfig_copy_createsNewInstance() {
        val original = AudioConfig(sampleRate = 16000)
        val copy = original.copy(sampleRate = 44100)
        
        assertEquals(44100, copy.sampleRate)
        assertEquals(16000, original.sampleRate)
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
    fun audioConfig_toString_containsSampleRate() {
        val config = AudioConfig(sampleRate = 16000)
        val string = config.toString()
        
        assertTrue(string.contains("16000") || string.contains("sampleRate"))
    }
    
    @Test
    fun audioConfig_hashCode_isConsistent() {
        val config1 = AudioConfig(sampleRate = 16000)
        val config2 = AudioConfig(sampleRate = 16000)
        
        assertEquals(config1.hashCode(), config2.hashCode())
    }
    
    @Test
    fun recordingState_componentAccess_worksCorrectly() {
        val state = RecordingState(
            isRecording = true,
            recordingTime = 60,
            audioFilePath = "/path",
            isRecordingComplete = false,
            error = "error"
        )
        
        val (isRec, time, path, complete, err) = state
        
        assertTrue(isRec)
        assertEquals(60, time)
        assertEquals("/path", path)
        assertFalse(complete)
        assertEquals("error", err)
    }
    
    @Test
    fun audioConfig_componentAccess_worksCorrectly() {
        val config = AudioConfig(
            sampleRate = 16000,
            channelConfig = AudioFormat.CHANNEL_IN_MONO,
            audioFormat = AudioFormat.ENCODING_PCM_16BIT,
            audioSource = MediaRecorder.AudioSource.MIC
        )
        
        val (rate, channel, format, source) = config
        
        assertEquals(16000, rate)
        assertEquals(AudioFormat.CHANNEL_IN_MONO, channel)
        assertEquals(AudioFormat.ENCODING_PCM_16BIT, format)
        assertEquals(MediaRecorder.AudioSource.MIC, source)
    }
    
    @Test
    fun recordingState_withLongRecordingTime_worksCorrectly() {
        val state = RecordingState(recordingTime = 3600) // 1 hour
        
        assertEquals(3600, state.recordingTime)
    }
    
    @Test
    fun recordingState_withVeryLongPath_worksCorrectly() {
        val longPath = "/very/long/path/" + "segment/".repeat(100) + "audio.wav"
        val state = RecordingState(audioFilePath = longPath)
        
        assertEquals(longPath, state.audioFilePath)
    }
}

