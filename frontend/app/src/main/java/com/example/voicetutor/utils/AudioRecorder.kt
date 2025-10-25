package com.example.voicetutor.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorder(private val context: Context) {
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private var audioData = mutableListOf<ByteArray>()
    
    // 녹음 설정
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    
    private var onRecordingStateChanged: ((Boolean) -> Unit)? = null
    private var onDurationChanged: ((Int) -> Unit)? = null
    
    fun setOnRecordingStateChanged(callback: (Boolean) -> Unit) {
        onRecordingStateChanged = callback
    }
    
    fun setOnDurationChanged(callback: (Int) -> Unit) {
        onDurationChanged = callback
    }
    
    fun startRecording(): Boolean {
        println("AudioRecorder - startRecording() called")
        
        if (isRecording) {
            println("AudioRecorder - Already recording")
            return false
        }
        
        if (!checkPermissions()) {
            println("AudioRecorder - Permission denied (RECORD_AUDIO)")
            return false
        }
        
        try {
            println("AudioRecorder - Creating AudioRecord instance")
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            
            println("AudioRecorder - AudioRecord state: ${audioRecord?.state}")
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                println("AudioRecorder - AudioRecord not initialized properly")
                return false
            }
            
            println("AudioRecorder - Starting recording")
            audioRecord?.startRecording()
            isRecording = true
            audioData.clear()
            
            onRecordingStateChanged?.invoke(true)
            
            println("AudioRecorder - Starting recording job")
            // 녹음 시작
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ByteArray(bufferSize)
                var duration = 0
                
                while (isRecording && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (bytesRead > 0) {
                        audioData.add(buffer.copyOf(bytesRead))
                        duration += (bytesRead * 1000) / (sampleRate * 2) // 대략적인 시간 계산
                        onDurationChanged?.invoke(duration / 1000) // 초 단위로 전달
                    }
                }
            }
            
            println("AudioRecorder - Recording started successfully")
            return true
        } catch (e: Exception) {
            println("AudioRecorder - Exception during recording: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    fun stopRecording(): String? {
        if (!isRecording) return null
        
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        
        recordingJob?.cancel()
        recordingJob = null
        
        onRecordingStateChanged?.invoke(false)
        
        return saveAudioToFile()
    }
    
    private fun saveAudioToFile(): String? {
        return try {
            val audioFile = createAudioFile()
            val outputStream = FileOutputStream(audioFile)
            
            // WAV 헤더 작성
            writeWavHeader(outputStream, audioData.size * bufferSize)
            
            // 오디오 데이터 작성
            audioData.forEach { data ->
                outputStream.write(data)
            }
            
            outputStream.close()
            audioFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "recording_$timeStamp.wav"
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "VoiceTutor")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, fileName)
    }
    
    private fun writeWavHeader(outputStream: FileOutputStream, dataSize: Int) {
        val totalSize = 36 + dataSize
        val sampleRate = this.sampleRate.toLong()
        
        // RIFF 헤더
        outputStream.write("RIFF".toByteArray())
        outputStream.write(intToByteArray(totalSize))
        outputStream.write("WAVE".toByteArray())
        
        // fmt 청크
        outputStream.write("fmt ".toByteArray())
        outputStream.write(intToByteArray(16)) // fmt 청크 크기
        outputStream.write(shortToByteArray(1)) // PCM 포맷
        outputStream.write(shortToByteArray(1)) // 채널 수
        outputStream.write(intToByteArray(sampleRate.toInt())) // 샘플 레이트
        outputStream.write(intToByteArray((sampleRate * 2).toInt())) // 바이트 레이트
        outputStream.write(shortToByteArray(2)) // 블록 정렬
        outputStream.write(shortToByteArray(16)) // 비트 깊이
        
        // data 청크
        outputStream.write("data".toByteArray())
        outputStream.write(intToByteArray(dataSize))
    }
    
    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }
    
    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
    
    private fun checkPermissions(): Boolean {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        println("AudioRecorder - Permission check: $hasPermission")
        return hasPermission
    }
    
    fun isRecording(): Boolean = isRecording
    
    fun cleanup() {
        if (isRecording) {
            stopRecording()
        }
        audioRecord?.release()
        audioRecord = null
    }
}
