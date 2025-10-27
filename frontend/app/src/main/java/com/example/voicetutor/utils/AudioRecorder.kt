package com.example.voicetutor.utils

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.provider.MediaStore
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
    
    // 추가 변수들
    private var outputStream: FileOutputStream? = null
    private var currentAudioFile: File? = null
    private var totalBytesWritten: Long = 0
    
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
            // 버퍼 크기를 더 크게 설정 (21초 분량의 데이터를 담을 수 있도록)
            val largeBufferSize = bufferSize * 32 // 기본 버퍼의 32배로 설정
            println("AudioRecorder - 확장된 버퍼 크기: $largeBufferSize bytes (기본의 32배)")
            
            println("AudioRecorder - Creating AudioRecord instance")
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                largeBufferSize
            )
            
            println("AudioRecorder - AudioRecord state: ${audioRecord?.state}")
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                println("AudioRecorder - AudioRecord not initialized properly, trying MIC source")
                audioRecord?.release()
                
                // 대체 오디오 소스 시도
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    largeBufferSize
                )
                
                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    println("AudioRecorder - Failed to initialize AudioRecord")
                    return false
                }
            }
            
            // 오디오 파일 생성
            val audioFile = createAudioFile()
            if (audioFile == null) {
                println("AudioRecorder - Failed to create audio file")
                return false
            }
            
            println("AudioRecorder - 오디오 파일 생성 완료: ${audioFile.absolutePath}")
            
            // 녹음 관련 변수 초기화
            currentAudioFile = audioFile
            totalBytesWritten = 0
            audioData.clear()
            
            println("AudioRecorder - Starting recording")
            audioRecord?.startRecording()
            isRecording = true
            
            onRecordingStateChanged?.invoke(true)
            
            println("AudioRecorder - Starting recording job")
            // 녹음 시작
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ShortArray(bufferSize)
                outputStream = FileOutputStream(audioFile.absolutePath)
                var duration = 0
                var loopCount = 0
                
                while (isRecording && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
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
                        audioData.add(byteArray)
                        
                        duration += (readSize * 1000) / (sampleRate * 2)
                        onDurationChanged?.invoke(duration / 1000)
                        
                        loopCount++
                        if (loopCount % 100 == 0) {
                            val currentDurationSeconds = totalBytesWritten / (sampleRate * 2)
                            println("AudioRecorder - 녹음 루프 #$loopCount: $readSize samples (${byteArray.size} bytes), 총 바이트: $totalBytesWritten, 현재 길이: ${currentDurationSeconds}초")
                        }
                    }
                }
                
                println("AudioRecorder - 녹음 루프 종료 - 총 루프 수: $loopCount, 총 바이트: $totalBytesWritten")
                
                // 녹음 루프가 종료된 후 마지막 버퍼 처리 (ANR 방지)
                println("AudioRecorder - 마지막 버퍼 처리 시작")
                var finalBytesRead = 0L
                repeat(10) { // 50번에서 10번으로 감소
                    val finalReadSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
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
                        println("AudioRecorder - 마지막 읽기 #${it + 1}: $finalReadSize samples (${byteArray.size} bytes)")
                    } else {
                        println("AudioRecorder - 마지막 읽기 #${it + 1}: 더 이상 읽을 데이터 없음")
                        return@repeat
                    }
                }
                
                val finalDurationSeconds = totalBytesWritten / (sampleRate * 2)
                println("AudioRecorder - 마지막 버퍼에서 읽은 총 바이트: $finalBytesRead")
                println("AudioRecorder - 최종 총 바이트: $totalBytesWritten, 현재 길이: ${finalDurationSeconds}초")
                
                // 파일 쓰기 완료 보장
                outputStream?.flush()
                println("AudioRecorder - 파일 스트림 flush() 완료")
                
                println("AudioRecorder - Recording finished. Total audio data chunks: ${audioData.size}, Total bytes: $totalBytesWritten")
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
        
        println("AudioRecorder - stopRecording() called")
        println("AudioRecorder - 현재까지 기록된 바이트: $totalBytesWritten")
        
        isRecording = false
        
        // 녹음 작업 완료 대기
        recordingJob?.let { job ->
            println("AudioRecorder - 녹음 작업 완료 대기 중...")
            runBlocking {
                job.join()
            }
            println("AudioRecorder - 녹음 작업 완료")
        }
        
        // 백그라운드에서 내부 버퍼 비우기 (ANR 방지)
        val stopJob = CoroutineScope(Dispatchers.IO).launch {
            // AudioRecord의 내부 버퍼 완전히 비우기
            audioRecord?.let { record ->
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    println("AudioRecorder - AudioRecord 내부 버퍼 비우기 시작")
                    val buffer = ShortArray(bufferSize)
                    
                    var additionalBytesRead = 0L
                    // 더 적은 횟수로 읽어서 내부 버퍼 비우기 (ANR 방지)
                    repeat(20) { // 100번에서 20번으로 감소
                        val readSize = record.read(buffer, 0, buffer.size)
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
                            println("AudioRecorder - 추가 읽기 #${it + 1}: $readSize samples (${byteArray.size} bytes)")
                        } else {
                            println("AudioRecorder - 추가 읽기 #${it + 1}: 더 이상 읽을 데이터 없음")
                            return@repeat
                        }
                    }
                    
                    println("AudioRecorder - 추가로 읽은 총 바이트: $additionalBytesRead")
                    val additionalDurationSeconds = totalBytesWritten / (sampleRate * 2)
                    println("AudioRecorder - 최종 총 바이트: $totalBytesWritten, 현재 길이: ${additionalDurationSeconds}초")
                    
                    record.stop()
                    println("AudioRecorder - AudioRecord.stop() 호출 완료")
                }
                record.release()
                println("AudioRecorder - AudioRecord.release() 호출 완료")
            }
            audioRecord = null
            
            // 파일 스트림 정리
            try {
                outputStream?.flush()
                println("AudioRecorder - 파일 스트림 flush() 완료")
                outputStream?.close()
                println("AudioRecorder - 파일 스트림 close() 완료")
            } catch (e: Exception) {
                println("AudioRecorder - 파일 스트림 닫기 중 오류: ${e.message}")
            }
            outputStream = null
            
        }
        
        // 백그라운드 작업 완료 대기 (짧은 시간만)
        runBlocking {
            withTimeout(2000) { // 2초 타임아웃
                stopJob.join()
            }
        }
        
        onRecordingStateChanged?.invoke(false)
        
        // WAV 파일로 변환
        val result = convertToWavFile()
        
        // 녹음 길이 계산 및 로그
        val durationSeconds = totalBytesWritten / (sampleRate * 2) // 2 bytes per sample (16-bit)
        val durationMinutes = durationSeconds / 60
        val remainingSeconds = durationSeconds % 60
        
        println("AudioRecorder - 녹음 길이: ${durationMinutes}분 ${remainingSeconds}초 (총 ${durationSeconds}초)")
        println("AudioRecorder - 녹음 중지 완료! 총 바이트: $totalBytesWritten, 파일: $result")
        
        return result
    }
    
    private fun convertToWavFile(): String? {
        return try {
            val audioFile = currentAudioFile ?: return null
            val wavFile = File(audioFile.parent, audioFile.nameWithoutExtension + ".wav")
            
            val outputStream = FileOutputStream(wavFile.absolutePath)
            
            // WAV 헤더 작성
            writeWavHeader(outputStream, totalBytesWritten.toInt())
            
            // PCM 데이터를 WAV 파일에 쓰기
            val inputStream = audioFile.inputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            
            outputStream.close()
            
            // 원본 PCM 파일 삭제
            audioFile.delete()
            
            // MediaStore에 추가
            addToMediaStore(wavFile)
            
            println("AudioRecorder - WAV 파일 변환 완료: ${wavFile.absolutePath}")
            wavFile.absolutePath
        } catch (e: Exception) {
            println("AudioRecorder - WAV 파일 변환 중 오류: ${e.message}")
            e.printStackTrace()
            null
        }
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
    
    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "recording_$timeStamp.pcm"
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "VoiceTutor")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, fileName)
    }
    
    private fun addToMediaStore(audioFile: File) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, audioFile.name)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/VoiceTutor")
        }
        
        context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                audioFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
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
        
        // 추가 변수들 정리
        currentAudioFile = null
        totalBytesWritten = 0
        outputStream = null
    }
}
