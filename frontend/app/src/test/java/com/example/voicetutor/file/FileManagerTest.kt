package com.example.voicetutor.file

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FileManager enums and data classes.
 */
class FileManagerTest {

    @Test
    fun fileType_enumValues_areCorrect() {
        assertEquals(4, FileType.values().size)
        assertTrue(FileType.values().contains(FileType.AUDIO))
        assertTrue(FileType.values().contains(FileType.IMAGE))
        assertTrue(FileType.values().contains(FileType.DOCUMENT))
        assertTrue(FileType.values().contains(FileType.OTHER))
    }

    @Test
    fun fileInfo_creation_withAllFields_createsCorrectInstance() {
        val date = java.util.Date()
        val fileInfo = FileInfo(
            name = "test.wav",
            path = "/path/to/test.wav",
            size = 1024L,
            type = "audio/wav",
            createdAt = date,
            isAudio = true,
            isImage = false,
            isDocument = false
        )
        
        assertEquals("test.wav", fileInfo.name)
        assertEquals("/path/to/test.wav", fileInfo.path)
        assertEquals(1024L, fileInfo.size)
        assertEquals("audio/wav", fileInfo.type)
        assertEquals(date, fileInfo.createdAt)
        assertTrue(fileInfo.isAudio)
        assertFalse(fileInfo.isImage)
        assertFalse(fileInfo.isDocument)
    }

    @Test
    fun fileInfo_creation_withDefaults_usesDefaults() {
        val date = java.util.Date()
        val fileInfo = FileInfo(
            name = "test.jpg",
            path = "/path/to/test.jpg",
            size = 2048L,
            type = "image/jpeg",
            createdAt = date
        )
        
        assertFalse(fileInfo.isAudio)
        assertFalse(fileInfo.isImage)
        assertFalse(fileInfo.isDocument)
    }
}

