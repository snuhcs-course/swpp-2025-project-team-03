package com.example.voicetutor.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import com.example.voicetutor.annotations.ExcludeFromJacocoGeneratedReport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class RecordingState(
    val isRecording: Boolean = false,
    val recordingTime: Int = 0, // seconds
    val audioFilePath: String? = null,
    val isRecordingComplete: Boolean = false,
    val error: String? = null,
)

data class AudioConfig(
    val sampleRate: Int = 16000, // Google STT 표준
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val audioSource: Int = MediaRecorder.AudioSource.MIC,
    val maxRecordingDurationSeconds: Int = 60, // 최대 녹음 시간: 1분
)

class AudioRecorder(private val context: Context) {

    private var audioRecord: android.media.AudioRecord? = null
    private var recordingJob: Job? = null
    private var timerJob: Job? = null
    private var startTime: Long = 0

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val audioConfig = AudioConfig()

    /**
     * 녹음 시작
     */
    fun startRecording(): Boolean {
        return try {
            println("AudioRecorder - Starting recording...")
            if (_recordingState.value.isRecording) {
                println("AudioRecorder - Already recording, returning false")
                return false
            }

            val bufferSize = AudioRecord.getMinBufferSize(
                audioConfig.sampleRate,
                audioConfig.channelConfig,
                audioConfig.audioFormat,
            )

            if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
                _recordingState.value = _recordingState.value.copy(
                    error = "오디오 버퍼 크기를 가져올 수 없습니다",
                )
                return false
            }

            // Android 6.0 이상에서 권한 체크
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    _recordingState.value = _recordingState.value.copy(
                        error = "마이크 권한이 필요합니다",
                    )
                    return false
                }
            }

            audioRecord = android.media.AudioRecord(
                audioConfig.audioSource,
                audioConfig.sampleRate,
                audioConfig.channelConfig,
                audioConfig.audioFormat,
                bufferSize * 2,
            )

            if (audioRecord?.state != android.media.AudioRecord.STATE_INITIALIZED) {
                _recordingState.value = _recordingState.value.copy(
                    error = "오디오 레코더 초기화에 실패했습니다",
                )
                return false
            }

            val audioFile = createAudioFile()
            if (audioFile == null) {
                _recordingState.value = _recordingState.value.copy(
                    error = "오디오 파일을 생성할 수 없습니다",
                )
                return false
            }

            _recordingState.value = _recordingState.value.copy(
                isRecording = true,
                recordingTime = 0,
                audioFilePath = audioFile.absolutePath,
                isRecordingComplete = false,
                error = null,
            )

            startTime = System.currentTimeMillis()

            // 타이머 시작
            startTimer()

            // 녹음 시작
            startRecordingJob(audioFile, bufferSize)

            println("AudioRecorder - Recording started successfully")
            println("AudioRecorder - Audio file: ${audioFile.absolutePath}")
            println("AudioRecorder - Buffer size: $bufferSize")
            println("AudioRecorder - Sample rate: ${audioConfig.sampleRate}")

            true
        } catch (e: Exception) {
            _recordingState.value = _recordingState.value.copy(
                error = "녹음 시작 중 오류: ${e.message}",
            )
            false
        }
    }

    /**
     * 녹음 중지
     */
    fun stopRecording() {
        try {
            _recordingState.value = _recordingState.value.copy(
                isRecording = false,
                isRecordingComplete = true,
            )

            // 작업들 중지
            recordingJob?.cancel()
            timerJob?.cancel()

            // AudioRecord 정리
            audioRecord?.apply {
                if (state == android.media.AudioRecord.STATE_INITIALIZED) {
                    stop()
                }
                release()
            }
            audioRecord = null

            // PCM 파일을 WAV로 변환
            val currentFilePath = _recordingState.value.audioFilePath
            if (currentFilePath != null && currentFilePath.endsWith(".pcm")) {
                println("AudioRecorder - Converting PCM to WAV: $currentFilePath")
                val wavFilePath = convertPcmToWav(currentFilePath)
                if (wavFilePath != null) {
                    println("AudioRecorder - Successfully converted to WAV: $wavFilePath")
                    _recordingState.value = _recordingState.value.copy(
                        audioFilePath = wavFilePath,
                    )
                } else {
                    println("AudioRecorder - Failed to convert PCM to WAV")
                    _recordingState.value = _recordingState.value.copy(
                        error = "PCM to WAV 변환 실패",
                    )
                }
            }
        } catch (e: Exception) {
            _recordingState.value = _recordingState.value.copy(
                error = "녹음 중지 중 오류: ${e.message}",
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
                
                // 최대 녹음 시간 도달 시 자동 중지
                if (elapsedTime >= audioConfig.maxRecordingDurationSeconds) {
                    println("AudioRecorder - 최대 녹음 시간 도달, 자동 중지")
                    stopRecording()
                    break
                }
            }
        }
    }

    /**
     * 실제 녹음 작업
     */
    private fun startRecordingJob(audioFile: File, bufferSize: Int) {
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                audioRecord?.startRecording()

                val buffer = ShortArray(bufferSize)
                val outputStream = FileOutputStream(audioFile)

                while (_recordingState.value.isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0

                    if (readSize > 0) {
                        // PCM 데이터를 바이트 배열로 변환
                        val byteBuffer = ShortArray(readSize)
                        System.arraycopy(buffer, 0, byteBuffer, 0, readSize)

                        // 바이트 배열로 변환하여 파일에 쓰기
                        val bytes = ShortArray(readSize)
                        for (i in 0 until readSize) {
                            bytes[i] = buffer[i]
                        }

                        // 바이트로 변환
                        val byteArray = ByteArray(readSize * 2)
                        for (i in 0 until readSize) {
                            val sample = bytes[i]
                            byteArray[i * 2] = (sample.toInt() and 0xFF).toByte()
                            byteArray[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }

                        outputStream.write(byteArray)
                    }
                }

                outputStream.close()
            } catch (e: Exception) {
                _recordingState.value = _recordingState.value.copy(
                    error = "녹음 중 오류: ${e.message}",
                )
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

            println("AudioRecorder - Creating audio file: $fileName")

            // 앱의 내부 저장소에 파일 생성
            val audioDir = File(context.filesDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
                println("AudioRecorder - Created audio directory: ${audioDir.absolutePath}")
            }

            val audioFile = File(audioDir, fileName)
            println("AudioRecorder - Audio file path: ${audioFile.absolutePath}")

            audioFile
        } catch (e: Exception) {
            println("AudioRecorder - Error creating audio file: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * PCM 파일을 WAV 파일로 변환
     */
    fun convertPcmToWav(pcmFilePath: String): String? {
        return try {
            println("AudioRecorder - Starting PCM to WAV conversion")
            println("AudioRecorder - PCM file path: $pcmFilePath")

            val pcmFile = File(pcmFilePath)
            if (!pcmFile.exists()) {
                println("AudioRecorder - PCM file does not exist: $pcmFilePath")
                return null
            }

            println("AudioRecorder - PCM file size: ${pcmFile.length()} bytes")

            val wavFilePath = pcmFilePath.replace(".pcm", ".wav")
            val wavFile = File(wavFilePath)

            println("AudioRecorder - Target WAV file path: $wavFilePath")

            val pcmData = pcmFile.readBytes()
            println("AudioRecorder - Read ${pcmData.size} bytes from PCM file")

            val wavData = createWavFile(pcmData, audioConfig.sampleRate)
            println("AudioRecorder - Created WAV data: ${wavData.size} bytes")

            wavFile.writeBytes(wavData)
            println("AudioRecorder - WAV file written successfully")
            println("AudioRecorder - WAV file size: ${wavFile.length()} bytes")

            pcmFile.delete() // PCM 파일 삭제
            println("AudioRecorder - PCM file deleted")

            wavFilePath
        } catch (e: Exception) {
            println("AudioRecorder - Error converting PCM to WAV: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * WAV 파일 헤더만 생성 (44바이트)
     */
    private fun createWavHeader(dataSize: Int, sampleRate: Int): ByteArray {
        val wavHeader = ByteArray(44)
        val fileSize = dataSize + 36

        // RIFF 헤더
        wavHeader[0] = 'R'.code.toByte()
        wavHeader[1] = 'I'.code.toByte()
        wavHeader[2] = 'F'.code.toByte()
        wavHeader[3] = 'F'.code.toByte()

        // 파일 크기
        wavHeader[4] = (fileSize and 0xFF).toByte()
        wavHeader[5] = ((fileSize shr 8) and 0xFF).toByte()
        wavHeader[6] = ((fileSize shr 16) and 0xFF).toByte()
        wavHeader[7] = ((fileSize shr 24) and 0xFF).toByte()

        // WAVE 포맷
        wavHeader[8] = 'W'.code.toByte()
        wavHeader[9] = 'A'.code.toByte()
        wavHeader[10] = 'V'.code.toByte()
        wavHeader[11] = 'E'.code.toByte()

        // fmt 청크
        wavHeader[12] = 'f'.code.toByte()
        wavHeader[13] = 'm'.code.toByte()
        wavHeader[14] = 't'.code.toByte()
        wavHeader[15] = ' '.code.toByte()

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
        wavHeader[36] = 'd'.code.toByte()
        wavHeader[37] = 'a'.code.toByte()
        wavHeader[38] = 't'.code.toByte()
        wavHeader[39] = 'a'.code.toByte()

        // 데이터 크기
        wavHeader[40] = (dataSize and 0xFF).toByte()
        wavHeader[41] = ((dataSize shr 8) and 0xFF).toByte()
        wavHeader[42] = ((dataSize shr 16) and 0xFF).toByte()
        wavHeader[43] = ((dataSize shr 24) and 0xFF).toByte()

        return wavHeader
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
     * 1초짜리 빈 WAV 파일 생성 (건너뛰기용)
     */
    fun createEmptyWavFile(): File? {
        return try {
            println("AudioRecorder - Creating empty WAV file for skip")

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "voice_skip_$timestamp.wav"

            val audioDir = File(context.filesDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val wavFile = File(audioDir, fileName)

            // 1초 분량의 무음 데이터 생성 (16000 샘플 * 2바이트 = 32000 바이트)
            val sampleRate = 16000
            val numSamples = sampleRate // 1초
            val silentData = ByteArray(numSamples * 2) // 16-bit = 2 bytes per sample

            // WAV 헤더 생성
            val wavHeader = createWavHeader(silentData.size, sampleRate)

            // 파일에 쓰기
            FileOutputStream(wavFile).use { outputStream ->
                outputStream.write(wavHeader)
                outputStream.write(silentData)
            }

            println("AudioRecorder - Empty WAV file created: ${wavFile.absolutePath}")
            wavFile
        } catch (e: Exception) {
            println("AudioRecorder - Error creating empty WAV file: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * 리소스 정리
     */
    @ExcludeFromJacocoGeneratedReport
    fun cleanup() {
        stopRecording()
        recordingJob?.cancel()
        timerJob?.cancel()
    }
}
