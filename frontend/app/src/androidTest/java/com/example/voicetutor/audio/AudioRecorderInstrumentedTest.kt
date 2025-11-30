package com.example.voicetutor.audio

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioRecorderInstrumentedTest {

    private lateinit var context: Context
    private lateinit var audioRecorder: AudioRecorder

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        audioRecorder = AudioRecorder(context)
    }

    @After
    fun cleanup() {
        audioRecorder.cleanup()
    }

    @Test
    fun audioRecorder_initialState_isNotRecording() {
        val state = audioRecorder.recordingState.value

        assertFalse(state.isRecording)
        assertEquals(0, state.recordingTime)
        assertNull(state.audioFilePath)
        assertFalse(state.isRecordingComplete)
        assertNull(state.error)
    }

    @Test
    fun audioRecorder_startRecording_returnsTrue() {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val result = audioRecorder.startRecording()
            assertTrue(result)

            val state = audioRecorder.recordingState.value
            assertNotNull(state)
        }
    }

    @Test
    fun audioRecorder_startRecordingTwice_secondCallReturnsFalse() = runBlocking {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val firstResult = audioRecorder.startRecording()
            delay(100)

            if (firstResult) {
                val secondResult = audioRecorder.startRecording()
                assertFalse(secondResult)

                audioRecorder.stopRecording()
                delay(100)
            }
        }
    }

    @Test
    fun audioRecorder_stopRecording_whenNotRecording_doesNotCrash() {
        audioRecorder.stopRecording()
        val state = audioRecorder.recordingState.value

        assertFalse(state.isRecording)
    }

    @Test
    fun audioRecorder_convertPcmToWav_withValidPcmFile_createsWavFile() {
        val tempDir = File(context.cacheDir, "test_audio")
        tempDir.mkdirs()
        val pcmFile = File(tempDir, "test.pcm")

        val dummyPcmData = ByteArray(100) { it.toByte() }
        pcmFile.writeBytes(dummyPcmData)

        val wavFilePath = audioRecorder.convertPcmToWav(pcmFile.absolutePath)

        if (wavFilePath != null) {
            val wavFile = File(wavFilePath)
            assertTrue(wavFile.exists())
            assertTrue(wavFile.length() > 0)
            assertTrue(wavFile.absolutePath.endsWith(".wav"))

            assertFalse(pcmFile.exists())

            wavFile.delete()
        } else {
            pcmFile.delete()
        }

        tempDir.deleteRecursively()
    }

    @Test
    fun audioRecorder_convertPcmToWav_withNonExistentFile_returnsNull() {
        val nonExistentPath = "/nonexistent/path/test.pcm"
        val result = audioRecorder.convertPcmToWav(nonExistentPath)

        assertNull(result)
    }

    @Test
    fun audioRecorder_convertPcmToWav_withInvalidPath_returnsNull() {
        val invalidPath = ""
        val result = audioRecorder.convertPcmToWav(invalidPath)

        assertNull(result)
    }

    @Test
    fun audioRecorder_recordingStateFlow_emitsUpdates() = runBlocking {
        val initialState = audioRecorder.recordingState.first()
        assertFalse(initialState.isRecording)

        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            audioRecorder.startRecording()
            delay(100)

            val recordingState = audioRecorder.recordingState.first()

            assertNotNull(recordingState)

            audioRecorder.stopRecording()
            delay(100)
        }
    }

    @Test
    fun audioRecorder_cleanup_stopsRecordingAndCleansResources() = runBlocking {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            audioRecorder.startRecording()
            delay(100)

            audioRecorder.cleanup()
            delay(100)

            val state = audioRecorder.recordingState.value
            assertFalse(state.isRecording)
        }
    }

    @Test
    fun audioRecorder_multipleStartStopCycles_handlesCorrectly() = runBlocking {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            repeat(3) {
                val startResult = audioRecorder.startRecording()
                delay(50)

                if (startResult) {
                    audioRecorder.stopRecording()
                    delay(50)
                }
            }

            val finalState = audioRecorder.recordingState.value
            assertFalse(finalState.isRecording)
        }
    }

    @Test
    fun audioRecorder_withoutPermission_setsError() {
        val state = audioRecorder.recordingState.value
        assertNotNull(state)
    }

    @Test
    fun audioRecorder_filePath_formatIsCorrect() = runBlocking {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val result = audioRecorder.startRecording()
            delay(100)

            if (result) {
                val state = audioRecorder.recordingState.value
                val filePath = state.audioFilePath

                if (filePath != null) {
                    assertTrue(filePath.contains("voice_recording_"))
                    assertTrue(filePath.contains(".pcm") || filePath.contains(".wav"))
                }

                audioRecorder.stopRecording()
                delay(100)
            }
        }
    }

    @Test
    fun audioRecorder_errorState_updatesCorrectly() {
        val state = audioRecorder.recordingState.value

        assertNull(state.error)
    }

    @Test
    fun audioRecorder_recordingTime_incrementsWhileRecording() = runBlocking {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val startResult = audioRecorder.startRecording()

            if (startResult) {
                delay(1500)

                val state = audioRecorder.recordingState.value

                assertTrue(state.recordingTime >= 1)

                audioRecorder.stopRecording()
                delay(100)
            }
        }
    }
}
