package com.example.voicetutor.audio

import org.junit.Assert.*
import org.junit.Test

class RecordingStateTest {

    @Test
    fun recordingState_defaultValues_areCorrect() {
        val state = RecordingState()

        assertFalse(state.isRecording)
        assertEquals(0, state.recordingTime)
        assertNull(state.audioFilePath)
        assertFalse(state.isRecordingComplete)
        assertNull(state.error)
    }

    @Test
    fun recordingState_withRecordingTime_updatesTime() {
        val state = RecordingState(
            isRecording = true,
            recordingTime = 10,
        )

        assertTrue(state.isRecording)
        assertEquals(10, state.recordingTime)
    }

    @Test
    fun recordingState_withAudioFilePath_setsPath() {
        val filePath = "/path/to/audio.wav"
        val state = RecordingState(
            audioFilePath = filePath,
        )

        assertEquals(filePath, state.audioFilePath)
    }

    @Test
    fun recordingState_withError_setsError() {
        val errorMessage = "Test error"
        val state = RecordingState(
            error = errorMessage,
        )

        assertEquals(errorMessage, state.error)
    }

    @Test
    fun recordingState_isRecordingComplete_setsComplete() {
        val state = RecordingState(
            isRecordingComplete = true,
        )

        assertTrue(state.isRecordingComplete)
    }

    @Test
    fun recordingState_copy_updatesValues() {
        val original = RecordingState()
        val updated = original.copy(
            isRecording = true,
            recordingTime = 5,
            audioFilePath = "/test/path.wav",
        )

        assertTrue(updated.isRecording)
        assertEquals(5, updated.recordingTime)
        assertEquals("/test/path.wav", updated.audioFilePath)

        // Original remains unchanged
        assertFalse(original.isRecording)
        assertEquals(0, original.recordingTime)
        assertNull(original.audioFilePath)
    }

    @Test
    fun recordingState_allProperties_set() {
        val state = RecordingState(
            isRecording = true,
            recordingTime = 30,
            audioFilePath = "/test/recording.wav",
            isRecordingComplete = true,
            error = null,
        )

        assertTrue(state.isRecording)
        assertEquals(30, state.recordingTime)
        assertEquals("/test/recording.wav", state.audioFilePath)
        assertTrue(state.isRecordingComplete)
        assertNull(state.error)
    }
}
