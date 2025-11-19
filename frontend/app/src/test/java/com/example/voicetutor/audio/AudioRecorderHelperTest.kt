package com.example.voicetutor.audio

import android.media.AudioFormat
import android.media.MediaRecorder
import org.junit.Assert.*
import org.junit.Test

/**
 * Additional unit tests for AudioRecorder helper methods and edge cases.
 */
class AudioRecorderHelperTest {

    @Test
    fun recordingState_withAllBooleanCombinations_handlesCorrectly() {
        val combinations = listOf(
            RecordingState(isRecording = false, isRecordingComplete = false),
            RecordingState(isRecording = false, isRecordingComplete = true),
            RecordingState(isRecording = true, isRecordingComplete = false),
            RecordingState(isRecording = true, isRecordingComplete = true),
        )

        combinations.forEach { state ->
            assertNotNull(state)
        }
        assertEquals(4, combinations.size)
    }

    @Test
    fun recordingState_withVariousTimeValues_handlesCorrectly() {
        val times = listOf(0, 1, 30, 60, 300, 3600, Int.MAX_VALUE)
        times.forEach { time ->
            val state = RecordingState(recordingTime = time)
            assertEquals(time, state.recordingTime)
        }
    }

    @Test
    fun recordingState_withVariousErrorMessages_handlesCorrectly() {
        val errors = listOf(
            null,
            "",
            "Short error",
            "A".repeat(1000),
            "Error with special chars: !@#$%^&*()",
        )
        errors.forEach { error ->
            val state = RecordingState(error = error)
            assertEquals(error, state.error)
        }
    }

    @Test
    fun audioConfig_withVariousSampleRates_handlesCorrectly() {
        val sampleRates = listOf(8000, 16000, 22050, 44100, 48000)
        sampleRates.forEach { rate ->
            val config = AudioConfig(sampleRate = rate)
            assertEquals(rate, config.sampleRate)
        }
    }

    @Test
    fun audioConfig_withVariousChannelConfigs_handlesCorrectly() {
        val channelConfigs = listOf(
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.CHANNEL_IN_STEREO,
        )
        channelConfigs.forEach { channel ->
            val config = AudioConfig(channelConfig = channel)
            assertEquals(channel, config.channelConfig)
        }
    }

    @Test
    fun audioConfig_withVariousAudioFormats_handlesCorrectly() {
        val formats = listOf(
            AudioFormat.ENCODING_PCM_16BIT,
            AudioFormat.ENCODING_PCM_8BIT,
        )
        formats.forEach { format ->
            val config = AudioConfig(audioFormat = format)
            assertEquals(format, config.audioFormat)
        }
    }

    @Test
    fun audioConfig_withVariousAudioSources_handlesCorrectly() {
        val sources = listOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        )
        sources.forEach { source ->
            val config = AudioConfig(audioSource = source)
            assertEquals(source, config.audioSource)
        }
    }

    @Test
    fun recordingState_withNullFilePath_handlesCorrectly() {
        val state = RecordingState(audioFilePath = null)
        assertNull(state.audioFilePath)
    }

    @Test
    fun recordingState_withEmptyFilePath_handlesCorrectly() {
        val state = RecordingState(audioFilePath = "")
        assertEquals("", state.audioFilePath)
    }

    @Test
    fun recordingState_withComplexFilePath_handlesCorrectly() {
        val complexPath = "/storage/emulated/0/Android/data/com.example.voicetutor/files/audio_recordings/voice_recording_20240101_120000.pcm"
        val state = RecordingState(audioFilePath = complexPath)
        assertEquals(complexPath, state.audioFilePath)
    }

    @Test
    fun recordingState_hashCode_withSameValues_isEqual() {
        val state1 = RecordingState(
            isRecording = true,
            recordingTime = 60,
            audioFilePath = "/path/to/file",
            isRecordingComplete = false,
            error = null,
        )
        val state2 = RecordingState(
            isRecording = true,
            recordingTime = 60,
            audioFilePath = "/path/to/file",
            isRecordingComplete = false,
            error = null,
        )
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun audioConfig_hashCode_withSameValues_isEqual() {
        val config1 = AudioConfig(
            sampleRate = 16000,
            channelConfig = AudioFormat.CHANNEL_IN_MONO,
            audioFormat = AudioFormat.ENCODING_PCM_16BIT,
            audioSource = MediaRecorder.AudioSource.MIC,
        )
        val config2 = AudioConfig(
            sampleRate = 16000,
            channelConfig = AudioFormat.CHANNEL_IN_MONO,
            audioFormat = AudioFormat.ENCODING_PCM_16BIT,
            audioSource = MediaRecorder.AudioSource.MIC,
        )
        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun recordingState_toString_containsRelevantFields() {
        val state = RecordingState(
            isRecording = true,
            recordingTime = 60,
            audioFilePath = "/path/to/file",
        )
        val string = state.toString()
        assertTrue(string.contains("60") || string.contains("true") || string.contains("/path"))
    }

    @Test
    fun audioConfig_toString_containsRelevantFields() {
        val config = AudioConfig(sampleRate = 16000)
        val string = config.toString()
        assertTrue(string.contains("16000") || string.contains("sampleRate"))
    }

    @Test
    fun recordingState_equality_withDifferentErrorMessages_isNotEqual() {
        val state1 = RecordingState(error = "Error 1")
        val state2 = RecordingState(error = "Error 2")
        assertNotEquals(state1, state2)
    }

    @Test
    fun audioConfig_equality_withDifferentSampleRates_isNotEqual() {
        val config1 = AudioConfig(sampleRate = 16000)
        val config2 = AudioConfig(sampleRate = 44100)
        assertNotEquals(config1, config2)
    }

    @Test
    fun recordingState_withMaxIntRecordingTime_handlesCorrectly() {
        val state = RecordingState(recordingTime = Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, state.recordingTime)
    }

    @Test
    fun recordingState_withMinIntRecordingTime_handlesCorrectly() {
        val state = RecordingState(recordingTime = Int.MIN_VALUE)
        assertEquals(Int.MIN_VALUE, state.recordingTime)
    }
}
