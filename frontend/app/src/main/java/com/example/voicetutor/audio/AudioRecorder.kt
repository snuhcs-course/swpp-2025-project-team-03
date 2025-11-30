package com.example.voicetutor.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
    val recordingTime: Int = 0,
    val audioFilePath: String? = null,
    val isRecordingComplete: Boolean = false,
    val error: String? = null,
)

data class AudioConfig(
    val sampleRate: Int = 16000,
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val audioSource: Int = MediaRecorder.AudioSource.MIC,
    val maxRecordingDurationSeconds: Int = 60,
)

class AudioRecorder(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var timerJob: Job? = null
    private var startTime: Long = 0

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val audioConfig = AudioConfig()

    fun startRecording(): Boolean {
        return try {
            if (_recordingState.value.isRecording) {
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

            if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                _recordingState.value = _recordingState.value.copy(
                    error = "마이크 권한이 필요합니다",
                )
                return false
            }

            audioRecord = AudioRecord(
                audioConfig.audioSource,
                audioConfig.sampleRate,
                audioConfig.channelConfig,
                audioConfig.audioFormat,
                bufferSize * 2,
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
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

            startTimer()
            startRecordingJob(audioFile, bufferSize)

            true
        } catch (e: Exception) {
            _recordingState.value = _recordingState.value.copy(
                error = "녹음 시작 중 오류: ${e.message}",
            )
            false
        }
    }

    fun stopRecording() {
        try {
            _recordingState.value = _recordingState.value.copy(
                isRecording = false,
                isRecordingComplete = true,
            )

            recordingJob?.cancel()
            timerJob?.cancel()

            audioRecord?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    stop()
                }
                release()
            }
            audioRecord = null

            val currentFilePath = _recordingState.value.audioFilePath
            if (currentFilePath != null && currentFilePath.endsWith(".pcm")) {
                val wavFilePath = convertPcmToWav(currentFilePath)
                if (wavFilePath != null) {
                    _recordingState.value = _recordingState.value.copy(
                        audioFilePath = wavFilePath,
                    )
                } else {
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

    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (_recordingState.value.isRecording) {
                delay(1000)
                val elapsedTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                _recordingState.value = _recordingState.value.copy(recordingTime = elapsedTime)

                if (elapsedTime >= audioConfig.maxRecordingDurationSeconds) {
                    stopRecording()
                    break
                }
            }
        }
    }

    private fun startRecordingJob(audioFile: File, bufferSize: Int) {
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                audioRecord?.startRecording()

                val buffer = ShortArray(bufferSize)
                val outputStream = FileOutputStream(audioFile)

                while (_recordingState.value.isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0

                    if (readSize > 0) {
                        val byteBuffer = ShortArray(readSize)
                        System.arraycopy(buffer, 0, byteBuffer, 0, readSize)

                        val bytes = ShortArray(readSize)
                        for (i in 0 until readSize) {
                            bytes[i] = buffer[i]
                        }

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

    private fun createAudioFile(): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "voice_recording_$timestamp.pcm"

            val audioDir = File(context.filesDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val audioFile = File(audioDir, fileName)
            audioFile
        } catch (e: Exception) {
            null
        }
    }

    fun convertPcmToWav(pcmFilePath: String): String? {
        return try {
            val pcmFile = File(pcmFilePath)
            if (!pcmFile.exists()) {
                return null
            }

            val wavFilePath = pcmFilePath.replace(".pcm", ".wav")
            val wavFile = File(wavFilePath)

            val pcmData = pcmFile.readBytes()
            val wavData = createWavFile(pcmData, audioConfig.sampleRate)

            wavFile.writeBytes(wavData)
            pcmFile.delete()

            wavFilePath
        } catch (e: Exception) {
            null
        }
    }

    private fun createWavHeader(dataSize: Int): ByteArray {
        val wavHeader = ByteArray(44)
        val fileSize = dataSize + 36

        wavHeader[0] = 'R'.code.toByte()
        wavHeader[1] = 'I'.code.toByte()
        wavHeader[2] = 'F'.code.toByte()
        wavHeader[3] = 'F'.code.toByte()

        wavHeader[4] = (fileSize and 0xFF).toByte()
        wavHeader[5] = ((fileSize shr 8) and 0xFF).toByte()
        wavHeader[6] = ((fileSize shr 16) and 0xFF).toByte()
        wavHeader[7] = ((fileSize shr 24) and 0xFF).toByte()

        wavHeader[8] = 'W'.code.toByte()
        wavHeader[9] = 'A'.code.toByte()
        wavHeader[10] = 'V'.code.toByte()
        wavHeader[11] = 'E'.code.toByte()

        wavHeader[12] = 'f'.code.toByte()
        wavHeader[13] = 'm'.code.toByte()
        wavHeader[14] = 't'.code.toByte()
        wavHeader[15] = ' '.code.toByte()

        wavHeader[16] = 16
        wavHeader[17] = 0
        wavHeader[18] = 0
        wavHeader[19] = 0

        wavHeader[20] = 1
        wavHeader[21] = 0

        wavHeader[22] = 1
        wavHeader[23] = 0

        val sampleRate = 16000
        wavHeader[24] = (sampleRate and 0xFF).toByte()
        wavHeader[25] = ((sampleRate shr 8) and 0xFF).toByte()
        wavHeader[26] = 0
        wavHeader[27] = 0

        val byteRate = sampleRate * 2
        wavHeader[28] = 0
        wavHeader[29] = ((byteRate shr 8) and 0xFF).toByte()
        wavHeader[30] = 0
        wavHeader[31] = 0

        wavHeader[32] = 2
        wavHeader[33] = 0

        wavHeader[34] = 16
        wavHeader[35] = 0

        wavHeader[36] = 'd'.code.toByte()
        wavHeader[37] = 'a'.code.toByte()
        wavHeader[38] = 't'.code.toByte()
        wavHeader[39] = 'a'.code.toByte()

        wavHeader[40] = (dataSize and 0xFF).toByte()
        wavHeader[41] = ((dataSize shr 8) and 0xFF).toByte()
        wavHeader[42] = ((dataSize shr 16) and 0xFF).toByte()
        wavHeader[43] = ((dataSize shr 24) and 0xFF).toByte()

        return wavHeader
    }

    private fun createWavFile(pcmData: ByteArray, sampleRate: Int): ByteArray {
        val wavHeader = ByteArray(44)
        val dataSize = pcmData.size
        val fileSize = dataSize + 36

        wavHeader[0] = 'R'.code.toByte()
        wavHeader[1] = 'I'.code.toByte()
        wavHeader[2] = 'F'.code.toByte()
        wavHeader[3] = 'F'.code.toByte()

        wavHeader[4] = (fileSize and 0xFF).toByte()
        wavHeader[5] = ((fileSize shr 8) and 0xFF).toByte()
        wavHeader[6] = ((fileSize shr 16) and 0xFF).toByte()
        wavHeader[7] = ((fileSize shr 24) and 0xFF).toByte()

        wavHeader[8] = 'W'.code.toByte()
        wavHeader[9] = 'A'.code.toByte()
        wavHeader[10] = 'V'.code.toByte()
        wavHeader[11] = 'E'.code.toByte()

        wavHeader[12] = 'f'.code.toByte()
        wavHeader[13] = 'm'.code.toByte()
        wavHeader[14] = 't'.code.toByte()
        wavHeader[15] = ' '.code.toByte()

        wavHeader[16] = 16
        wavHeader[17] = 0
        wavHeader[18] = 0
        wavHeader[19] = 0

        wavHeader[20] = 1
        wavHeader[21] = 0

        wavHeader[22] = 1
        wavHeader[23] = 0

        wavHeader[24] = (sampleRate and 0xFF).toByte()
        wavHeader[25] = ((sampleRate shr 8) and 0xFF).toByte()
        wavHeader[26] = 0
        wavHeader[27] = 0

        val byteRate = sampleRate * 2
        wavHeader[28] = 0
        wavHeader[29] = ((byteRate shr 8) and 0xFF).toByte()
        wavHeader[30] = 0
        wavHeader[31] = 0

        wavHeader[32] = 2
        wavHeader[33] = 0

        wavHeader[34] = 16
        wavHeader[35] = 0

        wavHeader[36] = 'd'.code.toByte()
        wavHeader[37] = 'a'.code.toByte()
        wavHeader[38] = 't'.code.toByte()
        wavHeader[39] = 'a'.code.toByte()

        wavHeader[40] = (dataSize and 0xFF).toByte()
        wavHeader[41] = ((dataSize shr 8) and 0xFF).toByte()
        wavHeader[42] = ((dataSize shr 16) and 0xFF).toByte()
        wavHeader[43] = ((dataSize shr 24) and 0xFF).toByte()

        return wavHeader + pcmData
    }

    fun createEmptyWavFile(): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "voice_skip_$timestamp.wav"

            val audioDir = File(context.filesDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val wavFile = File(audioDir, fileName)

            val sampleRate = 16000
            val numSamples = sampleRate
            val silentData = ByteArray(numSamples * 2)

            val wavHeader = createWavHeader(silentData.size)

            FileOutputStream(wavFile).use { outputStream ->
                outputStream.write(wavHeader)
                outputStream.write(silentData)
            }

            wavFile
        } catch (e: Exception) {
            null
        }
    }

    @ExcludeFromJacocoGeneratedReport
    fun cleanup() {
        stopRecording()
        recordingJob?.cancel()
        timerJob?.cancel()
    }
}
