package com.example.voicetutor.audio

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentPosition: Int = 0, // milliseconds
    val duration: Int = 0, // milliseconds
    val playbackSpeed: Float = 1.0f,
    val error: String? = null
)

class AudioPlayer(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null
    private var currentFilePath: String? = null
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    /**
     * 오디오 파일 재생
     */
    fun playAudio(filePath: String): Boolean {
        return try {
            // 기존 재생 중지
            stopPlayback()
            
            val audioFile = File(filePath)
            if (!audioFile.exists()) {
                _playbackState.value = _playbackState.value.copy(
                    error = "오디오 파일을 찾을 수 없습니다"
                )
                return false
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                
                setOnPreparedListener { mp ->
                    _playbackState.value = _playbackState.value.copy(
                        duration = mp.duration,
                        error = null
                    )
                    mp.start()
                }
                
                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isPaused = false,
                        currentPosition = 0
                    )
                }
                
                setOnErrorListener { _, what, extra ->
                    _playbackState.value = _playbackState.value.copy(
                        error = "재생 중 오류가 발생했습니다: $what, $extra",
                        isPlaying = false,
                        isPaused = false
                    )
                    true
                }
            }
            
            currentFilePath = filePath
            _playbackState.value = _playbackState.value.copy(
                isPlaying = true,
                isPaused = false,
                error = null
            )
            
            // 재생 위치 업데이트
            startPositionUpdate()
            
            true
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                error = "재생 시작 중 오류: ${e.message}",
                isPlaying = false,
                isPaused = false
            )
            false
        }
    }
    
    /**
     * 재생 일시정지/재개
     */
    fun pauseResume(): Boolean {
        return try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isPaused = true
                    )
                } else {
                    mp.start()
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = true,
                        isPaused = false
                    )
                }
                true
            } ?: false
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                error = "일시정지/재개 중 오류: ${e.message}"
            )
            false
        }
    }
    
    /**
     * 재생 중지
     */
    fun stopPlayback() {
        try {
            playbackJob?.cancel()
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            currentFilePath = null
            
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isPaused = false,
                currentPosition = 0
            )
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                error = "재생 중지 중 오류: ${e.message}"
            )
        }
    }
    
    /**
     * 재생 위치 이동
     */
    fun seekTo(position: Int): Boolean {
        return try {
            mediaPlayer?.let { mp ->
                val seekPosition = (position * mp.duration / 100).coerceIn(0, mp.duration)
                mp.seekTo(seekPosition)
                _playbackState.value = _playbackState.value.copy(
                    currentPosition = seekPosition
                )
                true
            } ?: false
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                error = "위치 이동 중 오류: ${e.message}"
            )
            false
        }
    }
    
    /**
     * 재생 속도 변경
     */
    fun setPlaybackSpeed(speed: Float): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mediaPlayer?.let { mp ->
                    mp.playbackParams = mp.playbackParams.setSpeed(speed)
                    _playbackState.value = _playbackState.value.copy(
                        playbackSpeed = speed
                    )
                    true
                } ?: false
            } else {
                _playbackState.value = _playbackState.value.copy(
                    error = "재생 속도 변경은 Android 6.0 이상에서 지원됩니다"
                )
                false
            }
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                error = "재생 속도 변경 중 오류: ${e.message}"
            )
            false
        }
    }
    
    /**
     * 재생 위치 업데이트
     */
    private fun startPositionUpdate() {
        playbackJob = CoroutineScope(Dispatchers.Main).launch {
            while (_playbackState.value.isPlaying || _playbackState.value.isPaused) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _playbackState.value = _playbackState.value.copy(
                            currentPosition = mp.currentPosition
                        )
                    }
                }
                delay(100) // 100ms마다 업데이트
            }
        }
    }
    
    /**
     * 현재 재생 중인 파일 경로
     */
    fun getCurrentFilePath(): String? = currentFilePath
    
    /**
     * 리소스 정리
     */
    fun cleanup() {
        stopPlayback()
        playbackJob?.cancel()
    }
}
