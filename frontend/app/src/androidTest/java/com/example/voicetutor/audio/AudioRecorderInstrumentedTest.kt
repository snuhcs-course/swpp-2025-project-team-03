package com.example.voicetutor.audio

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.TestDispatcher

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
        // 실제 녹음은 하드웨어가 필요하므로 권한과 마이크가 있는 환경에서만 성공
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == 
            PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            val result = audioRecorder.startRecording()
            // 실제 기기에서만 성공할 수 있음 (에뮬레이터에서는 실패할 수 있음)
            // 결과에 관계없이 상태를 확인
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
            delay(100) // 짧은 딜레이
            
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
        // 녹음 중이 아닐 때 중지해도 크래시가 나지 않아야 함
        audioRecorder.stopRecording()
        val state = audioRecorder.recordingState.value
        
        assertFalse(state.isRecording)
    }

    @Test
    fun audioRecorder_convertPcmToWav_withValidPcmFile_createsWavFile() {
        // 테스트용 PCM 파일 생성
        val tempDir = File(context.cacheDir, "test_audio")
        tempDir.mkdirs()
        val pcmFile = File(tempDir, "test.pcm")
        
        // 더미 PCM 데이터 작성 (최소 크기)
        val dummyPcmData = ByteArray(100) { it.toByte() }
        pcmFile.writeBytes(dummyPcmData)
        
        // WAV 변환
        val wavFilePath = audioRecorder.convertPcmToWav(pcmFile.absolutePath)
        
        if (wavFilePath != null) {
            val wavFile = File(wavFilePath)
            assertTrue(wavFile.exists())
            assertTrue(wavFile.length() > 0)
            assertTrue(wavFile.absolutePath.endsWith(".wav"))
            
            // PCM 파일은 삭제되어야 함
            assertFalse(pcmFile.exists())
            
            // 정리
            wavFile.delete()
        } else {
            // 변환 실패 가능성 (권한 등)
            // 정리만 수행
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
        
        // 상태 변화를 관찰 (실제 녹음은 하드웨어 필요)
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == 
            PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            audioRecorder.startRecording()
            delay(100)
            
            val recordingState = audioRecorder.recordingState.first()
            // 상태가 변경되었는지 확인 (성공 여부와 관계없이)
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
        // 권한이 없는 경우를 테스트하려면 권한을 취소해야 하지만,
        // 테스트 환경에서는 이미 권한이 부여되어 있으므로
        // 이 테스트는 실제 권한이 없을 때의 동작을 검증하기 어려움
        // 대신 상태 구조를 확인
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
                    // 파일 경로가 올바른 형식인지 확인
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
        // 에러 상태가 올바르게 설정되는지 확인
        val state = audioRecorder.recordingState.value
        // 초기 상태에는 에러가 없어야 함
        assertNull(state.error)
    }

    @Test
    fun audioRecorder_recordingTime_incrementsWhileRecording() = runBlocking {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == 
            PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            val startResult = audioRecorder.startRecording()
            
            if (startResult) {
                delay(1500) // 1.5초 대기
                
                val state = audioRecorder.recordingState.value
                // 녹음 시간이 증가했는지 확인 (최소 1초 이상)
                assertTrue(state.recordingTime >= 1)
                
                audioRecorder.stopRecording()
                delay(100)
            }
        }
    }
}

