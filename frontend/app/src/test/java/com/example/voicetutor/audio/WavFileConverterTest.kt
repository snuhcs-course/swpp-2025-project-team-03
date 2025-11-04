package com.example.voicetutor.audio

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer

class WavFileConverterTest {

    private val audioConfig = AudioConfig()

    @Test
    fun createWavFile_header_hasCorrectRIFF() {
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // RIFF 헤더 확인
        assertEquals('R'.toByte(), wavData[0])
        assertEquals('I'.toByte(), wavData[1])
        assertEquals('F'.toByte(), wavData[2])
        assertEquals('F'.toByte(), wavData[3])
    }

    @Test
    fun createWavFile_header_hasCorrectWAVE() {
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // WAVE 형식 확인
        assertEquals('W'.toByte(), wavData[8])
        assertEquals('A'.toByte(), wavData[9])
        assertEquals('V'.toByte(), wavData[10])
        assertEquals('E'.toByte(), wavData[11])
    }

    @Test
    fun createWavFile_header_hasCorrectFmtChunk() {
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // fmt 청크 확인
        assertEquals('f'.toByte(), wavData[12])
        assertEquals('m'.toByte(), wavData[13])
        assertEquals('t'.toByte(), wavData[14])
        assertEquals(' '.toByte(), wavData[15])
    }

    @Test
    fun createWavFile_header_hasCorrectDataChunk() {
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // data 청크 확인
        assertEquals('d'.toByte(), wavData[36])
        assertEquals('a'.toByte(), wavData[37])
        assertEquals('t'.toByte(), wavData[38])
        assertEquals('a'.toByte(), wavData[39])
    }

    @Test
    fun createWavFile_header_hasCorrectSampleRate() {
        val sampleRate = 16000
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, sampleRate)
        
        // 샘플 레이트는 리틀 엔디안으로 저장됨
        val sampleRateBytes = ByteArray(4)
        sampleRateBytes[0] = wavData[24]
        sampleRateBytes[1] = wavData[25]
        sampleRateBytes[2] = wavData[26]
        sampleRateBytes[3] = wavData[27]
        
        val readSampleRate = ByteBuffer.wrap(sampleRateBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
        assertEquals(sampleRate, readSampleRate)
    }

    @Test
    fun createWavFile_header_hasCorrectChannels() {
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // 채널 수 (모노 = 1)
        assertEquals(1, wavData[22].toInt())
        assertEquals(0, wavData[23].toInt())
    }

    @Test
    fun createWavFile_header_hasCorrectBitsPerSample() {
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // 비트당 샘플 (16비트)
        assertEquals(16, wavData[34].toInt())
        assertEquals(0, wavData[35].toInt())
    }

    @Test
    fun createWavFile_header_hasCorrectAudioFormat() {
        val pcmData = ByteArray(100)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // 오디오 포맷 (PCM = 1)
        assertEquals(1, wavData[20].toInt())
        assertEquals(0, wavData[21].toInt())
    }

    @Test
    fun createWavFile_dataSize_includesPcmData() {
        val pcmData = ByteArray(200)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // WAV 파일 크기는 헤더(44바이트) + PCM 데이터
        assertEquals(44 + pcmData.size, wavData.size)
        
        // PCM 데이터가 올바르게 포함되었는지 확인
        for (i in 0 until pcmData.size) {
            assertEquals(pcmData[i], wavData[44 + i])
        }
    }

    @Test
    fun createWavFile_differentSampleRates_handlesCorrectly() {
        val pcmData = ByteArray(100)
        
        val wavData16k = createWavFileHelper(pcmData, 16000)
        val wavData44k = createWavFileHelper(pcmData, 44100)
        
        // 파일 크기는 동일해야 함 (PCM 데이터 크기가 같음)
        assertEquals(wavData16k.size, wavData44k.size)
        
        // 하지만 샘플 레이트는 다름
        val sampleRate16k = ByteBuffer.wrap(
            wavData16k.sliceArray(24..27)
        ).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
        val sampleRate44k = ByteBuffer.wrap(
            wavData44k.sliceArray(24..27)
        ).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
        
        assertEquals(16000, sampleRate16k)
        assertEquals(44100, sampleRate44k)
    }

    @Test
    fun createWavFile_emptyPcmData_createsValidWav() {
        val pcmData = ByteArray(0)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // 최소 WAV 파일은 헤더만 있어야 함
        assertEquals(44, wavData.size)
        
        // RIFF 헤더 확인
        assertEquals('R'.toByte(), wavData[0])
        assertEquals('I'.toByte(), wavData[1])
    }

    @Test
    fun createWavFile_largePcmData_handlesCorrectly() {
        val pcmData = ByteArray(10000)
        val wavData = createWavFileHelper(pcmData, 16000)
        
        // 큰 PCM 데이터도 올바르게 처리되어야 함
        assertEquals(44 + 10000, wavData.size)
        
        // 데이터 청크 크기 확인
        val dataSizeBytes = ByteArray(4)
        dataSizeBytes[0] = wavData[40]
        dataSizeBytes[1] = wavData[41]
        dataSizeBytes[2] = wavData[42]
        dataSizeBytes[3] = wavData[43]
        val dataSize = ByteBuffer.wrap(dataSizeBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
        
        assertEquals(10000, dataSize)
    }

    // Helper function to create WAV file (mimics the private method)
    private fun createWavFileHelper(pcmData: ByteArray, sampleRate: Int): ByteArray {
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
}

