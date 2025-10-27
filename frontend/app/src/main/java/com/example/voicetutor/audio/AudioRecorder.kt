package com.example.voicetutor.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

data class RecordingState(
    val isRecording: Boolean = false,
    val recordingTime: Int = 0, // seconds
    val audioFilePath: String? = null,
    val error: String? = null,
    val isRecordingComplete: Boolean = false // 녹음이 완전히 완료되었는지 여부
)

data class AudioConfig(
    val sampleRate: Int = 16000, // Google STT 표준
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val audioSource: Int = MediaRecorder.AudioSource.MIC
)

class AudioRecorder(private val context: Context) {
    
    private var audioRecord: android.media.AudioRecord? = null
    private var recordingJob: Job? = null
    private var timerJob: Job? = null
    private var startTime: Long = 0
    private var outputStream: FileOutputStream? = null
    private var currentAudioFile: File? = null
    private var totalBytesWritten: Long = 0
    
    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val audioConfig = AudioConfig()
    
    companion object {
        private const val TAG = "AudioRecorder"
    }
    
    /**
     * 녹음 시작
     */
    fun startRecording(): Boolean {
        Log.d(TAG, "🎤 녹음 시작 요청")
        return try {
            if (_recordingState.value.isRecording) {
                Log.w(TAG, "⚠️ 이미 녹음 중입니다")
                return false
            }
            
            val bufferSize = AudioRecord.getMinBufferSize(
                audioConfig.sampleRate,
                audioConfig.channelConfig,
                audioConfig.audioFormat
            )
            
            Log.d(TAG, "📊 기본 버퍼 크기: $bufferSize bytes")
            
            if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
                Log.e(TAG, "❌ 오디오 버퍼 크기를 가져올 수 없습니다")
                _recordingState.value = _recordingState.value.copy(
                    error = "오디오 버퍼 크기를 가져올 수 없습니다"
                )
                return false
            }
            
            // 버퍼 크기를 더 크게 설정 (21초 분량의 데이터를 담을 수 있도록)
            val largeBufferSize = bufferSize * 32 // 기본 버퍼의 32배로 설정 (16배에서 32배로 증가)
            Log.d(TAG, "📊 확장된 버퍼 크기: $largeBufferSize bytes (기본의 32배)")
            
            // Android 6.0 이상에서 권한 체크
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    _recordingState.value = _recordingState.value.copy(
                        error = "마이크 권한이 필요합니다"
                    )
                    return false
                }
            }
            
            audioRecord = android.media.AudioRecord(
                audioConfig.audioSource,
                audioConfig.sampleRate,
                audioConfig.channelConfig,
                audioConfig.audioFormat,
                largeBufferSize
            )
            
            Log.d(TAG, "🎙️ AudioRecord 생성 완료 - 상태: ${audioRecord?.state}")
            
            if (audioRecord?.state != android.media.AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "❌ 오디오 레코더 초기화 실패 - 상태: ${audioRecord?.state}")
                _recordingState.value = _recordingState.value.copy(
                    error = "오디오 레코더 초기화에 실패했습니다"
                )
                return false
            }
            
            val audioFile = createAudioFile()
            if (audioFile == null) {
                Log.e(TAG, "❌ 오디오 파일 생성 실패")
                _recordingState.value = _recordingState.value.copy(
                    error = "오디오 파일을 생성할 수 없습니다"
                )
                return false
            }
            
            Log.d(TAG, "📁 오디오 파일 생성 완료: ${audioFile.absolutePath}")
            
            // 녹음 관련 변수 초기화
            currentAudioFile = audioFile
            totalBytesWritten = 0
            Log.d(TAG, "🔄 녹음 변수 초기화 완료")
            
            _recordingState.value = _recordingState.value.copy(
                isRecording = true,
                recordingTime = 0,
                audioFilePath = audioFile.absolutePath,
                error = null,
                isRecordingComplete = false
            )
            
            startTime = System.currentTimeMillis()
            
            // 타이머 시작
            startTimer()
            
            // 녹음 시작
            startRecordingJob(audioFile, largeBufferSize)
            
            Log.i(TAG, "✅ 녹음 시작 성공!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ 녹음 시작 중 오류: ${e.message}", e)
            _recordingState.value = _recordingState.value.copy(
                error = "녹음 시작 중 오류: ${e.message}"
            )
            false
        }
    }
    
    /**
     * 녹음 중지 - 강화된 버퍼 처리
     */
    fun stopRecording() {
        Log.d(TAG, "🛑 녹음 중지 요청")
        try {
            _recordingState.value = _recordingState.value.copy(isRecording = false)
            Log.d(TAG, "📊 현재까지 기록된 바이트: $totalBytesWritten")
            
            // 녹음 작업이 완료될 때까지 대기
            recordingJob?.let { job ->
                Log.d(TAG, "⏳ 녹음 작업 완료 대기 중...")
                runBlocking {
                    job.join() // 녹음 루프가 완전히 종료될 때까지 대기
                }
                Log.d(TAG, "✅ 녹음 작업 완료")
            }
            
            // AudioRecord의 내부 버퍼 완전히 비우기
            audioRecord?.let { record ->
                if (record.state == android.media.AudioRecord.STATE_INITIALIZED) {
                    Log.d(TAG, "🧹 AudioRecord 내부 버퍼 비우기 시작")
                    // stop() 전에 추가로 읽어서 내부 버퍼 비우기
                    val bufferSize = AudioRecord.getMinBufferSize(
                        audioConfig.sampleRate,
                        audioConfig.channelConfig,
                        audioConfig.audioFormat
                    )
                    val buffer = ShortArray(bufferSize)
                    
                    var additionalBytesRead = 0L
                    // 더 많은 횟수로 읽어서 내부 버퍼 완전히 비우기 (21초 분량)
                    repeat(100) { // 50번에서 100번으로 증가
                        val readSize = record.read(buffer, 0, bufferSize)
                        if (readSize > 0) {
                            val byteArray = ByteArray(readSize * 2)
                            for (i in 0 until readSize) {
                                val sample = buffer[i]
                                byteArray[i * 2] = (sample.toInt() and 0xFF).toByte()
                                byteArray[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                            }
                            outputStream?.write(byteArray)
                            totalBytesWritten += byteArray.size
                            additionalBytesRead += byteArray.size
                            Log.d(TAG, "📥 추가 읽기 #${it + 1}: $readSize samples (${byteArray.size} bytes)")
                        } else {
                            // 더 이상 읽을 데이터가 없으면 중단
                            Log.d(TAG, "📥 추가 읽기 #${it + 1}: 더 이상 읽을 데이터 없음")
                            return@repeat
                        }
                    }
                    
                    Log.d(TAG, "📊 추가로 읽은 총 바이트: $additionalBytesRead")
                    Log.d(TAG, "📊 최종 총 바이트: $totalBytesWritten")
                    
                    record.stop()
                    Log.d(TAG, "🛑 AudioRecord.stop() 호출 완료")
                }
                record.release()
                Log.d(TAG, "🗑️ AudioRecord.release() 호출 완료")
            }
            audioRecord = null
            
            // 파일 스트림 정리
            Log.d(TAG, "💾 파일 스트림 정리 시작")
            outputStream?.apply {
                flush() // 버퍼에 남은 데이터 강제 쓰기
                Log.d(TAG, "💾 파일 스트림 flush() 완료")
                close()
                Log.d(TAG, "💾 파일 스트림 close() 완료")
            }
            outputStream = null
            
            // 추가 안전 지연 (21초 분량을 고려하여 더 길게)
            Log.d(TAG, "⏳ 안전 지연 시작 (3000ms)")
            Thread.sleep(3000) // 2000ms에서 3000ms로 증가
            Log.d(TAG, "✅ 안전 지연 완료")
            
            // 녹음 완료 상태 업데이트
            _recordingState.value = _recordingState.value.copy(
                isRecording = false,
                error = null,
                isRecordingComplete = true // 녹음이 완전히 완료됨
            )
            
            Log.i(TAG, "🎉 녹음 중지 완료! 총 바이트: $totalBytesWritten")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 녹음 중지 중 오류: ${e.message}", e)
            _recordingState.value = _recordingState.value.copy(
                error = "녹음 중지 중 오류: ${e.message}"
            )
        }
    }
    
    /**
     * 녹음 타이머 시작
     */
    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (_recordingState.value.isRecording) {
                delay(1000)
                val elapsedTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                _recordingState.value = _recordingState.value.copy(recordingTime = elapsedTime)
            }
        }
    }
    
    /**
     * 실제 녹음 작업
     */
    private fun startRecordingJob(audioFile: File, bufferSize: Int) {
        Log.d(TAG, "🎬 녹음 작업 시작 - 파일: ${audioFile.name}, 버퍼크기: $bufferSize")
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                audioRecord?.startRecording()
                Log.d(TAG, "🎙️ AudioRecord.startRecording() 호출 완료")
                
                val buffer = ShortArray(bufferSize)
                outputStream = FileOutputStream(audioFile)
                Log.d(TAG, "📁 FileOutputStream 생성 완료")
                
                var loopCount = 0
                while (_recordingState.value.isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    
                    if (readSize > 0) {
                        // PCM 데이터를 바이트 배열로 변환
                        val byteArray = ByteArray(readSize * 2)
                        for (i in 0 until readSize) {
                            val sample = buffer[i]
                            byteArray[i * 2] = (sample.toInt() and 0xFF).toByte()
                            byteArray[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }
                        
                        outputStream?.write(byteArray)
                        totalBytesWritten += byteArray.size
                        
                        loopCount++
                        if (loopCount % 100 == 0) { // 100번마다 로그 출력
                            Log.d(TAG, "🔄 녹음 루프 #$loopCount: $readSize samples (${byteArray.size} bytes), 총 바이트: $totalBytesWritten")
                        }
                    }
                }
                
                Log.d(TAG, "🛑 녹음 루프 종료 - 총 루프 수: $loopCount, 총 바이트: $totalBytesWritten")
                
                // 녹음 루프가 종료된 후 마지막 버퍼 처리 (21초 분량 고려)
                Log.d(TAG, "🧹 마지막 버퍼 처리 시작")
                var finalBytesRead = 0L
                repeat(50) { // 20번에서 50번으로 증가
                    val finalReadSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (finalReadSize > 0) {
                        val byteArray = ByteArray(finalReadSize * 2)
                        for (i in 0 until finalReadSize) {
                            val sample = buffer[i]
                            byteArray[i * 2] = (sample.toInt() and 0xFF).toByte()
                            byteArray[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }
                        outputStream?.write(byteArray)
                        totalBytesWritten += byteArray.size
                        finalBytesRead += byteArray.size
                        Log.d(TAG, "📥 마지막 읽기 #${it + 1}: $finalReadSize samples (${byteArray.size} bytes)")
                    } else {
                        // 더 이상 읽을 데이터가 없으면 중단
                        Log.d(TAG, "📥 마지막 읽기 #${it + 1}: 더 이상 읽을 데이터 없음")
                        return@repeat
                    }
                }
                
                Log.d(TAG, "📊 마지막 버퍼에서 읽은 총 바이트: $finalBytesRead")
                Log.d(TAG, "📊 최종 총 바이트: $totalBytesWritten")
                
                // 파일 쓰기 완료 보장
                outputStream?.flush()
                Log.d(TAG, "💾 파일 스트림 flush() 완료")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 녹음 중 오류: ${e.message}", e)
                _recordingState.value = _recordingState.value.copy(
                    error = "녹음 중 오류: ${e.message}"
                )
            } finally {
                // 파일 스트림은 stopRecording()에서 정리하므로 여기서는 닫지 않음
                // 단, 예외 발생 시에만 안전하게 닫기
                try {
                    outputStream?.close()
                    Log.d(TAG, "💾 예외 발생 시 파일 스트림 닫기 완료")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 파일 스트림 닫기 중 오류: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 오디오 파일 생성
     */
    private fun createAudioFile(): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "voice_recording_$timestamp.pcm"
            
            // 앱의 내부 저장소에 파일 생성
            val audioDir = File(context.filesDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            
            File(audioDir.absolutePath, fileName)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * PCM 파일을 WAV 파일로 변환 - 동적 헤더 업데이트
     */
    fun convertPcmToWav(pcmFilePath: String): String? {
        Log.d(TAG, "🔄 PCM to WAV 변환 시작: $pcmFilePath")
        return try {
            val pcmFile = File(pcmFilePath)
            if (!pcmFile.exists()) {
                Log.e(TAG, "❌ PCM 파일이 존재하지 않음: $pcmFilePath")
                return null
            }
            
            val pcmFileSize = pcmFile.length()
            Log.d(TAG, "📁 PCM 파일 크기: $pcmFileSize bytes")
            
            val wavFilePath = pcmFilePath.replace(".pcm", ".wav")
            val wavFile = File(wavFilePath)
            
            val pcmData = pcmFile.readBytes()
            if (pcmData.isEmpty()) {
                Log.e(TAG, "❌ PCM 데이터가 비어있음")
                return null
            }
            
            // 실제 기록된 바이트 수 사용
            val actualDataSize = if (totalBytesWritten > 0) totalBytesWritten.toInt() else pcmData.size
            Log.d(TAG, "📊 실제 데이터 크기: $actualDataSize bytes (총 기록: $totalBytesWritten)")
            
            val wavData = createWavFileWithActualSize(pcmData, audioConfig.sampleRate, actualDataSize)
            
            wavFile.writeBytes(wavData)
            Log.d(TAG, "💾 WAV 파일 쓰기 완료: ${wavFile.length()} bytes")
            
            // 파일이 제대로 쓰여졌는지 확인
            if (wavFile.exists() && wavFile.length() > 0) {
                pcmFile.delete() // PCM 파일 삭제
                Log.d(TAG, "🗑️ PCM 파일 삭제 완료")
                Log.i(TAG, "✅ PCM to WAV 변환 성공: $wavFilePath")
                wavFilePath
            } else {
                Log.e(TAG, "❌ WAV 파일 생성 실패")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ PCM to WAV 변환 중 오류: ${e.message}", e)
            null
        }
    }
    
    /**
     * 실제 데이터 크기로 WAV 파일 헤더 생성
     */
    private fun createWavFileWithActualSize(pcmData: ByteArray, sampleRate: Int, actualDataSize: Int): ByteArray {
        val wavHeader = ByteArray(44)
        val fileSize = actualDataSize + 36
        
        // RIFF 헤더
        wavHeader[0] = 'R'.toByte()
        wavHeader[1] = 'I'.toByte()
        wavHeader[2] = 'F'.toByte()
        wavHeader[3] = 'F'.toByte()
        
        // 파일 크기 (실제 데이터 크기 사용)
        wavHeader[4] = (fileSize and 0xFF).toByte()
        wavHeader[5] = ((fileSize shr 8) and 0xFF).toByte()
        wavHeader[6] = ((fileSize shr 16) and 0xFF).toByte()
        wavHeader[7] = ((fileSize shr 24) and 0xFF).toByte()
        
        // WAVE 형식
        wavHeader[8] = 'W'.toByte()
        wavHeader[9] = 'A'.toByte()
        wavHeader[10] = 'V'.toByte()
        wavHeader[11] = 'E'.toByte()
        
        // fmt 청크
        wavHeader[12] = 'f'.toByte()
        wavHeader[13] = 'm'.toByte()
        wavHeader[14] = 't'.toByte()
        wavHeader[15] = ' '.toByte()
        
        // fmt 청크 크기
        wavHeader[16] = 16
        wavHeader[17] = 0
        wavHeader[18] = 0
        wavHeader[19] = 0
        
        // 오디오 포맷 (PCM)
        wavHeader[20] = 1
        wavHeader[21] = 0
        
        // 채널 수 (모노)
        wavHeader[22] = 1
        wavHeader[23] = 0
        
        // 샘플 레이트
        wavHeader[24] = (sampleRate and 0xFF).toByte()
        wavHeader[25] = ((sampleRate shr 8) and 0xFF).toByte()
        wavHeader[26] = ((sampleRate shr 16) and 0xFF).toByte()
        wavHeader[27] = ((sampleRate shr 24) and 0xFF).toByte()
        
        // 바이트 레이트
        val byteRate = sampleRate * 2
        wavHeader[28] = (byteRate and 0xFF).toByte()
        wavHeader[29] = ((byteRate shr 8) and 0xFF).toByte()
        wavHeader[30] = ((byteRate shr 16) and 0xFF).toByte()
        wavHeader[31] = ((byteRate shr 24) and 0xFF).toByte()
        
        // 블록 정렬
        wavHeader[32] = 2
        wavHeader[33] = 0
        
        // 비트당 샘플
        wavHeader[34] = 16
        wavHeader[35] = 0
        
        // data 청크
        wavHeader[36] = 'd'.toByte()
        wavHeader[37] = 'a'.toByte()
        wavHeader[38] = 't'.toByte()
        wavHeader[39] = 'a'.toByte()
        
        // 데이터 크기 (실제 기록된 크기 사용)
        wavHeader[40] = (actualDataSize and 0xFF).toByte()
        wavHeader[41] = ((actualDataSize shr 8) and 0xFF).toByte()
        wavHeader[42] = ((actualDataSize shr 16) and 0xFF).toByte()
        wavHeader[43] = ((actualDataSize shr 24) and 0xFF).toByte()
        
        // 실제 데이터 크기만큼만 사용
        val actualPcmData = if (actualDataSize < pcmData.size) {
            pcmData.sliceArray(0 until actualDataSize)
        } else {
            pcmData
        }
        
        return wavHeader + actualPcmData
    }
    
    /**
     * 파일 쓰기 완료 보장 및 검증 (6초 분량 고려)
     */
    private fun ensureFileWriteComplete(file: File): Boolean {
        return try {
            // 파일이 존재하고 크기가 0보다 큰지 확인
            if (!file.exists() || file.length() == 0L) {
                return false
            }
            
            // 파일 핸들을 강제로 동기화
            val fileChannel = file.outputStream().channel
            fileChannel.force(true) // OS 버퍼를 디스크에 강제 쓰기
            fileChannel.close()
            
            // 더 긴 지연으로 파일 시스템 동기화 보장 (6초 분량 고려)
            Thread.sleep(500) // 100ms에서 500ms로 증가
            
            // 파일이 여전히 존재하고 크기가 유지되는지 확인
            val isValid = file.exists() && file.length() > 0
            
            // 추가 검증: 파일 크기가 예상 범위 내에 있는지 확인
            val expectedMinSize = totalBytesWritten * 0.8 // 최소 80%는 기록되어야 함
            val actualSize = file.length()
            
            isValid && actualSize >= expectedMinSize
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 녹음된 파일이 업로드 준비되었는지 확인
     */
    fun isRecordingReadyForUpload(): Boolean {
        return try {
            val audioFile = currentAudioFile
            if (audioFile == null || !audioFile.exists()) {
                return false
            }
            
            // 파일 쓰기 완료 보장
            ensureFileWriteComplete(audioFile)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * WAV 파일 헤더 생성
     */
    private fun createWavFile(pcmData: ByteArray, sampleRate: Int): ByteArray {
        val wavHeader = ByteArray(44)
        val dataSize = pcmData.size
        val fileSize = dataSize + 36
        
        // RIFF 헤더
        wavHeader[0] = 'R'.toByte()
        wavHeader[1] = 'I'.toByte()
        wavHeader[2] = 'F'.toByte()
        wavHeader[3] = 'F'.toByte()
        
        // 파일 크기
        wavHeader[4] = (fileSize and 0xFF).toByte()
        wavHeader[5] = ((fileSize shr 8) and 0xFF).toByte()
        wavHeader[6] = ((fileSize shr 16) and 0xFF).toByte()
        wavHeader[7] = ((fileSize shr 24) and 0xFF).toByte()
        
        // WAVE 형식
        wavHeader[8] = 'W'.toByte()
        wavHeader[9] = 'A'.toByte()
        wavHeader[10] = 'V'.toByte()
        wavHeader[11] = 'E'.toByte()
        
        // fmt 청크
        wavHeader[12] = 'f'.toByte()
        wavHeader[13] = 'm'.toByte()
        wavHeader[14] = 't'.toByte()
        wavHeader[15] = ' '.toByte()
        
        // fmt 청크 크기
        wavHeader[16] = 16
        wavHeader[17] = 0
        wavHeader[18] = 0
        wavHeader[19] = 0
        
        // 오디오 포맷 (PCM)
        wavHeader[20] = 1
        wavHeader[21] = 0
        
        // 채널 수 (모노)
        wavHeader[22] = 1
        wavHeader[23] = 0
        
        // 샘플 레이트
        wavHeader[24] = (sampleRate and 0xFF).toByte()
        wavHeader[25] = ((sampleRate shr 8) and 0xFF).toByte()
        wavHeader[26] = ((sampleRate shr 16) and 0xFF).toByte()
        wavHeader[27] = ((sampleRate shr 24) and 0xFF).toByte()
        
        // 바이트 레이트
        val byteRate = sampleRate * 2
        wavHeader[28] = (byteRate and 0xFF).toByte()
        wavHeader[29] = ((byteRate shr 8) and 0xFF).toByte()
        wavHeader[30] = ((byteRate shr 16) and 0xFF).toByte()
        wavHeader[31] = ((byteRate shr 24) and 0xFF).toByte()
        
        // 블록 정렬
        wavHeader[32] = 2
        wavHeader[33] = 0
        
        // 비트당 샘플
        wavHeader[34] = 16
        wavHeader[35] = 0
        
        // data 청크
        wavHeader[36] = 'd'.toByte()
        wavHeader[37] = 'a'.toByte()
        wavHeader[38] = 't'.toByte()
        wavHeader[39] = 'a'.toByte()
        
        // 데이터 크기
        wavHeader[40] = (dataSize and 0xFF).toByte()
        wavHeader[41] = ((dataSize shr 8) and 0xFF).toByte()
        wavHeader[42] = ((dataSize shr 16) and 0xFF).toByte()
        wavHeader[43] = ((dataSize shr 24) and 0xFF).toByte()
        
        return wavHeader + pcmData
    }
    
    /**
     * 리소스 정리
     */
    fun cleanup() {
        try {
            stopRecording()
            recordingJob?.cancel()
            timerJob?.cancel()
            
            // 추가 안전장치: 파일 스트림이 남아있다면 정리
            outputStream?.apply {
                try {
                    flush()
                    close()
                } catch (e: Exception) {
                    // 무시
                }
            }
            outputStream = null
            
            // 변수 초기화
            currentAudioFile = null
            totalBytesWritten = 0
            
        } catch (e: Exception) {
            // 정리 중 오류는 무시
        }
    }
}

