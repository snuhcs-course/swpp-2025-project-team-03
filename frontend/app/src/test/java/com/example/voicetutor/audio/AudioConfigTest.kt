package com.example.voicetutor.audio

import android.media.AudioFormat
import android.media.MediaRecorder
import org.junit.Assert.*
import org.junit.Test

class AudioConfigTest {

    @Test
    fun audioConfig_defaultValues_areCorrect() {
        val config = AudioConfig()

        assertEquals(16000, config.sampleRate) // Google STT ?��?
        assertEquals(AudioFormat.CHANNEL_IN_MONO, config.channelConfig)
        assertEquals(AudioFormat.ENCODING_PCM_16BIT, config.audioFormat)
        assertEquals(MediaRecorder.AudioSource.MIC, config.audioSource)
    }

    @Test
    fun audioConfig_customValues_setsValues() {
        val config = AudioConfig(
            sampleRate = 44100,
            channelConfig = AudioFormat.CHANNEL_IN_STEREO,
            audioFormat = AudioFormat.ENCODING_PCM_8BIT,
            audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION,
        )

        assertEquals(44100, config.sampleRate)
        assertEquals(AudioFormat.CHANNEL_IN_STEREO, config.channelConfig)
        assertEquals(AudioFormat.ENCODING_PCM_8BIT, config.audioFormat)
        assertEquals(MediaRecorder.AudioSource.VOICE_RECOGNITION, config.audioSource)
    }

    @Test
    fun audioConfig_googleSTTStandard_uses16kHz() {
        val config = AudioConfig()

        // Google STT ?��? ?�플 ?�이??
        assertEquals(16000, config.sampleRate)
    }

    @Test
    fun audioConfig_monoChannel_usesMono() {
        val config = AudioConfig()

        assertEquals(AudioFormat.CHANNEL_IN_MONO, config.channelConfig)
    }

    @Test
    fun audioConfig_pcm16Bit_uses16Bit() {
        val config = AudioConfig()

        assertEquals(AudioFormat.ENCODING_PCM_16BIT, config.audioFormat)
    }

    @Test
    fun audioConfig_micSource_usesMic() {
        val config = AudioConfig()

        assertEquals(MediaRecorder.AudioSource.MIC, config.audioSource)
    }
}
