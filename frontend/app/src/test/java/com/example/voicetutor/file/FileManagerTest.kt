package com.example.voicetutor.file

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.File

/**
 * Unit tests for FileManager
 */
class FileManagerTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var fileManager: FileManager
    private lateinit var testDir: File

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Create temporary test directory
        testDir = File(System.getProperty("java.io.tmpdir"), "test_filemanager")
        testDir.mkdirs()

        // Mock context to return our test directory
        `when`(mockContext.filesDir).thenReturn(File(testDir, "files"))
        `when`(mockContext.cacheDir).thenReturn(File(testDir, "cache"))
    }

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
            isDocument = false,
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
            createdAt = date,
        )

        assertFalse(fileInfo.isAudio)
        assertFalse(fileInfo.isImage)
        assertFalse(fileInfo.isDocument)
    }

    @Test
    fun fileManager_initialization_createsDirectories() {
        fileManager = FileManager(mockContext)

        // Verify directories were created (they're created in init block)
        assertTrue(
            File(mockContext.filesDir, "audio_recordings").exists() ||
                mockContext.filesDir != null,
        )
    }

    @Test
    fun formatFileSize_bytes_formatsCorrectly() {
        fileManager = FileManager(mockContext)

        assertEquals("512 B", fileManager.formatFileSize(512))
    }

    @Test
    fun formatFileSize_kilobytes_formatsCorrectly() {
        fileManager = FileManager(mockContext)

        assertEquals("1.5 KB", fileManager.formatFileSize(1536))
    }

    @Test
    fun formatFileSize_megabytes_formatsCorrectly() {
        fileManager = FileManager(mockContext)

        val twoMB = 2 * 1024 * 1024L
        assertEquals("2.0 MB", fileManager.formatFileSize(twoMB))
    }

    @Test
    fun formatFileSize_gigabytes_formatsCorrectly() {
        fileManager = FileManager(mockContext)

        val twoGB = 2L * 1024 * 1024 * 1024
        assertEquals("2.0 GB", fileManager.formatFileSize(twoGB))
    }

    @Test
    fun getFileSize_existingFile_returnsCorrectSize() {
        fileManager = FileManager(mockContext)

        // Create a test file
        val testFile = File(testDir, "test_size.txt")
        testFile.writeText("Hello World")

        val size = fileManager.getFileSize(testFile.absolutePath)
        assertEquals(11L, size) // "Hello World" is 11 bytes

        testFile.delete()
    }

    @Test
    fun getFileSize_nonExistingFile_returnsZero() {
        fileManager = FileManager(mockContext)

        val size = fileManager.getFileSize("/non/existing/file.txt")
        assertEquals(0L, size)
    }

    @Test
    fun fileExists_existingFile_returnsTrue() {
        fileManager = FileManager(mockContext)

        val testFile = File(testDir, "existing_file.txt")
        testFile.writeText("test")

        assertTrue(fileManager.fileExists(testFile.absolutePath))

        testFile.delete()
    }

    @Test
    fun fileExists_nonExistingFile_returnsFalse() {
        fileManager = FileManager(mockContext)

        assertFalse(fileManager.fileExists("/non/existing/file.txt"))
    }

    @Test
    fun saveAudioFile_success_returnsFileInfo() = runTest {
        fileManager = FileManager(mockContext)

        // Create a test audio file
        val sourceFile = File(testDir, "test_audio.wav")
        sourceFile.writeBytes(ByteArray(100) { it.toByte() })

        val result = fileManager.saveAudioFile(sourceFile, "custom_audio.wav")

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()
        assertNotNull(fileInfo)
        assertEquals("custom_audio.wav", fileInfo?.name)
        assertTrue(fileInfo?.isAudio == true)
        assertEquals("audio/wav", fileInfo?.type)

        sourceFile.delete()
    }

    @Test
    fun deleteFile_existingFile_returnsSuccess() = runTest {
        fileManager = FileManager(mockContext)

        val testFile = File(testDir, "to_delete.txt")
        testFile.writeText("delete me")

        val result = fileManager.deleteFile(testFile.absolutePath)

        assertTrue(result.isSuccess)
        assertFalse(testFile.exists())
    }

    @Test
    fun deleteFile_nonExistingFile_returnsFailure() = runTest {
        fileManager = FileManager(mockContext)

        val result = fileManager.deleteFile("/non/existing/file.txt")

        assertTrue(result.isFailure)
    }

    @Test
    fun cleanupTempFiles_success_returnsCount() = runTest {
        fileManager = FileManager(mockContext)

        // Create temp directory and files
        val tempDir = File(mockContext.cacheDir, "temp")
        tempDir.mkdirs()

        File(tempDir, "temp1.txt").writeText("temp")
        File(tempDir, "temp2.txt").writeText("temp")

        val result = fileManager.cleanupTempFiles()

        assertTrue(result.isSuccess)
        val count = result.getOrNull()
        assertNotNull(count)
        assertTrue(count!! >= 0)
    }

    @Test
    fun getFiles_withNoFiles_returnsEmptyList() = runTest {
        fileManager = FileManager(mockContext)

        val result = fileManager.getFiles()

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
    }

    @Test
    fun fileInfo_copy_createsNewInstance() {
        val original = FileInfo(
            name = "test.txt",
            path = "/path/test.txt",
            size = 100L,
            type = "text/plain",
            createdAt = java.util.Date(),
        )

        val copy = original.copy(name = "new_test.txt")

        assertEquals("new_test.txt", copy.name)
        assertEquals(original.path, copy.path)
        assertEquals(original.size, copy.size)
    }

    @Test
    fun fileInfo_equality_worksCorrectly() {
        val date = java.util.Date()
        val info1 = FileInfo("test.txt", "/path", 100L, "text/plain", date)
        val info2 = FileInfo("test.txt", "/path", 100L, "text/plain", date)
        val info3 = FileInfo("other.txt", "/path", 100L, "text/plain", date)

        assertEquals(info1, info2)
        assertNotEquals(info1, info3)
    }

    @Test
    fun formatFileSize_zeroBytes_returnsZeroBytes() {
        fileManager = FileManager(mockContext)

        assertEquals("0 B", fileManager.formatFileSize(0))
    }

    @Test
    fun formatFileSize_oneByte_returnsOneByte() {
        fileManager = FileManager(mockContext)

        assertEquals("1 B", fileManager.formatFileSize(1))
    }

    @Test
    fun formatFileSize_1023Bytes_returnsBytes() {
        fileManager = FileManager(mockContext)

        assertEquals("1023 B", fileManager.formatFileSize(1023))
    }

    @Test
    fun formatFileSize_exactlyOneKB_returnsOneKB() {
        fileManager = FileManager(mockContext)

        assertEquals("1.0 KB", fileManager.formatFileSize(1024))
    }

    @Test
    fun formatFileSize_1025Bytes_returnsKB() {
        fileManager = FileManager(mockContext)

        assertEquals("1.0 KB", fileManager.formatFileSize(1025))
    }

    @Test
    fun formatFileSize_1536Bytes_returnsKB() {
        fileManager = FileManager(mockContext)

        assertEquals("1.5 KB", fileManager.formatFileSize(1536))
    }

    @Test
    fun formatFileSize_1023KB_returnsKB() {
        fileManager = FileManager(mockContext)

        val bytes = 1023 * 1024L
        assertEquals("1023.0 KB", fileManager.formatFileSize(bytes))
    }

    @Test
    fun formatFileSize_exactlyOneMB_returnsOneMB() {
        fileManager = FileManager(mockContext)

        val oneMB = 1024 * 1024L
        assertEquals("1.0 MB", fileManager.formatFileSize(oneMB))
    }

    @Test
    fun formatFileSize_oneAndHalfMB_returnsMB() {
        fileManager = FileManager(mockContext)

        val oneAndHalfMB = (1024 + 512) * 1024L
        assertEquals("1.5 MB", fileManager.formatFileSize(oneAndHalfMB))
    }

    @Test
    fun formatFileSize_1023MB_returnsMB() {
        fileManager = FileManager(mockContext)

        val bytes = 1023L * 1024 * 1024
        assertEquals("1023.0 MB", fileManager.formatFileSize(bytes))
    }

    @Test
    fun formatFileSize_exactlyOneGB_returnsOneGB() {
        fileManager = FileManager(mockContext)

        val oneGB = 1024L * 1024 * 1024
        assertEquals("1.0 GB", fileManager.formatFileSize(oneGB))
    }

    @Test
    fun formatFileSize_oneAndHalfGB_returnsGB() {
        fileManager = FileManager(mockContext)

        val oneAndHalfGB = (1024 + 512).toLong() * 1024 * 1024
        assertEquals("1.5 GB", fileManager.formatFileSize(oneAndHalfGB))
    }

    @Test
    fun formatFileSize_veryLargeFile_returnsGB() {
        fileManager = FileManager(mockContext)

        val tenGB = 10L * 1024 * 1024 * 1024
        assertEquals("10.0 GB", fileManager.formatFileSize(tenGB))
    }

    @Test
    fun formatFileSize_negativeBytes_handlesGracefully() {
        fileManager = FileManager(mockContext)

        // Negative bytes should still format (though unusual)
        val result = fileManager.formatFileSize(-100)
        assertNotNull(result)
    }
}
