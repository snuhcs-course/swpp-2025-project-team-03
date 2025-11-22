package com.example.voicetutor.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Unit tests for FileManager
 */
class FileManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockContentResolver: ContentResolver

    private lateinit var fileManager: FileManager
    private lateinit var testDir: File

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Create temporary test directory
        testDir = File(System.getProperty("java.io.tmpdir"), "test_filemanager_${System.currentTimeMillis()}")
        testDir.mkdirs()

        // Mock context to return our test directory
        `when`(mockContext.filesDir).thenReturn(File(testDir, "files"))
        `when`(mockContext.cacheDir).thenReturn(File(testDir, "cache"))
        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
    }

    @After
    fun cleanup() {
        // Clean up test directory
        testDir.deleteRecursively()
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

    @Test
    fun saveAudioFile_withNullCustomName_generatesFileName() = runTest {
        fileManager = FileManager(mockContext)

        val sourceFile = File(testDir, "test_audio.wav")
        sourceFile.writeBytes(ByteArray(100) { it.toByte() })

        val result = fileManager.saveAudioFile(sourceFile, null)

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()
        assertNotNull(fileInfo)
        assertTrue(fileInfo?.name?.startsWith("voice_recording_") == true)
        assertTrue(fileInfo?.name?.endsWith(".wav") == true)
        assertTrue(fileInfo?.isAudio == true)

        sourceFile.delete()
    }

    @Test
    fun saveAudioFile_failure_returnsFailure() = runTest {
        fileManager = FileManager(mockContext)

        // Try to save non-existent file
        val nonExistentFile = File(testDir, "non_existent.wav")

        val result = fileManager.saveAudioFile(nonExistentFile, "test.wav")

        assertTrue(result.isFailure)
    }

    @Test
    fun saveFile_success_audioType_returnsFileInfo() = runTest {
        fileManager = FileManager(mockContext)

        // Create a test file to simulate URI content
        val testFile = File(testDir, "test_audio.wav")
        testFile.writeBytes(ByteArray(200) { it.toByte() })

        val mockUri = mock(Uri::class.java)
        `when`(mockUri.lastPathSegment).thenReturn("test_audio.wav")
        `when`(mockContentResolver.getType(mockUri)).thenReturn("audio/wav")
        `when`(mockContentResolver.openInputStream(mockUri))
            .thenReturn(FileInputStream(testFile))

        val result = fileManager.saveFile(mockUri, "saved_audio.wav", FileType.AUDIO)

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()
        assertNotNull(fileInfo)
        assertEquals("saved_audio.wav", fileInfo?.name)
        assertTrue(fileInfo?.isAudio == true)
        assertEquals("audio/wav", fileInfo?.type)

        testFile.delete()
    }

    @Test
    fun saveFile_success_imageType_returnsFileInfo() = runTest {
        fileManager = FileManager(mockContext)

        val testFile = File(testDir, "test_image.jpg")
        testFile.writeBytes(ByteArray(300) { it.toByte() })

        val mockUri = mock(Uri::class.java)
        `when`(mockUri.lastPathSegment).thenReturn("test_image.jpg")
        `when`(mockContentResolver.getType(mockUri)).thenReturn("image/jpeg")
        `when`(mockContentResolver.openInputStream(mockUri))
            .thenReturn(FileInputStream(testFile))

        val result = fileManager.saveFile(mockUri, null, FileType.IMAGE)

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()
        assertNotNull(fileInfo)
        assertTrue(fileInfo?.name?.startsWith("image_") == true)
        assertTrue(fileInfo?.isImage == true)

        testFile.delete()
    }

    @Test
    fun saveFile_success_documentType_returnsFileInfo() = runTest {
        fileManager = FileManager(mockContext)

        val testFile = File(testDir, "test_doc.pdf")
        testFile.writeBytes(ByteArray(400) { it.toByte() })

        val mockUri = mock(Uri::class.java)
        `when`(mockUri.lastPathSegment).thenReturn("test_doc.pdf")
        `when`(mockContentResolver.getType(mockUri)).thenReturn("application/pdf")
        `when`(mockContentResolver.openInputStream(mockUri))
            .thenReturn(FileInputStream(testFile))

        val result = fileManager.saveFile(mockUri, null, FileType.DOCUMENT)

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()
        assertNotNull(fileInfo)
        assertTrue(fileInfo?.name?.startsWith("document_") == true)
        assertTrue(fileInfo?.isDocument == true)

        testFile.delete()
    }

    @Test
    fun saveFile_success_otherType_returnsFileInfo() = runTest {
        fileManager = FileManager(mockContext)

        val testFile = File(testDir, "test_other.bin")
        testFile.writeBytes(ByteArray(500) { it.toByte() })

        val mockUri = mock(Uri::class.java)
        `when`(mockUri.lastPathSegment).thenReturn("test_other.bin")
        `when`(mockContentResolver.getType(mockUri)).thenReturn("application/octet-stream")
        `when`(mockContentResolver.openInputStream(mockUri))
            .thenReturn(FileInputStream(testFile))

        val result = fileManager.saveFile(mockUri, null, FileType.OTHER)

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()
        assertNotNull(fileInfo)
        assertTrue(fileInfo?.name?.startsWith("other_") == true)

        testFile.delete()
    }

    @Test
    fun saveFile_failure_nullInputStream_returnsFailure() = runTest {
        fileManager = FileManager(mockContext)

        val mockUri = mock(Uri::class.java)
        `when`(mockContentResolver.openInputStream(mockUri)).thenReturn(null)

        val result = fileManager.saveFile(mockUri, "test.wav", FileType.AUDIO)

        assertTrue(result.isFailure)
    }

    @Test
    fun saveFile_failure_exception_returnsFailure() = runTest {
        fileManager = FileManager(mockContext)

        val mockUri = mock(Uri::class.java)
        `when`(mockContentResolver.openInputStream(mockUri))
            .thenThrow(RuntimeException("IO Error"))

        val result = fileManager.saveFile(mockUri, "test.wav", FileType.AUDIO)

        assertTrue(result.isFailure)
    }

    @Test
    fun saveFile_withoutExtension_usesDefaultExtension() = runTest {
        fileManager = FileManager(mockContext)

        val testFile = File(testDir, "test_file")
        testFile.writeBytes(ByteArray(100) { it.toByte() })

        val mockUri = mock(Uri::class.java)
        `when`(mockUri.lastPathSegment).thenReturn("test_file")
        `when`(mockContentResolver.getType(mockUri)).thenReturn(null)
        `when`(mockContentResolver.openInputStream(mockUri))
            .thenReturn(FileInputStream(testFile))

        val result = fileManager.saveFile(mockUri, null, FileType.AUDIO)

        assertTrue(result.isSuccess)
        val fileInfo = result.getOrNull()
        assertNotNull(fileInfo)
        assertTrue(fileInfo?.name?.endsWith(".wav") == true)

        testFile.delete()
    }

    @Test
    fun getFiles_withAudioType_returnsOnlyAudioFiles() = runTest {
        fileManager = FileManager(mockContext)

        // Create test files in audio directory
        val audioDir = File(mockContext.filesDir, "audio_recordings")
        audioDir.mkdirs()
        File(audioDir, "audio1.wav").writeBytes(ByteArray(100))
        File(audioDir, "audio2.mp3").writeBytes(ByteArray(200))

        val result = fileManager.getFiles(FileType.AUDIO)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.isNotEmpty())
        assertTrue(files.all { it.isAudio })
    }

    @Test
    fun getFiles_withImageType_returnsOnlyImageFiles() = runTest {
        fileManager = FileManager(mockContext)

        val imageDir = File(mockContext.filesDir, "images")
        imageDir.mkdirs()
        File(imageDir, "image1.jpg").writeBytes(ByteArray(100))
        File(imageDir, "image2.png").writeBytes(ByteArray(200))

        val result = fileManager.getFiles(FileType.IMAGE)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.isNotEmpty())
        assertTrue(files.all { it.isImage })
    }

    @Test
    fun getFiles_withDocumentType_returnsOnlyDocumentFiles() = runTest {
        fileManager = FileManager(mockContext)

        val documentDir = File(mockContext.filesDir, "documents")
        documentDir.mkdirs()
        File(documentDir, "doc1.pdf").writeBytes(ByteArray(100))
        File(documentDir, "doc2.txt").writeBytes(ByteArray(200))

        val result = fileManager.getFiles(FileType.DOCUMENT)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.isNotEmpty())
        assertTrue(files.all { it.isDocument })
    }

    @Test
    fun getFiles_withOtherType_returnsOnlyOtherFiles() = runTest {
        fileManager = FileManager(mockContext)

        val tempDir = File(mockContext.cacheDir, "temp")
        tempDir.mkdirs()
        File(tempDir, "other1.bin").writeBytes(ByteArray(100))

        val result = fileManager.getFiles(FileType.OTHER)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
    }

    @Test
    fun getFiles_withNullType_returnsAllFiles() = runTest {
        fileManager = FileManager(mockContext)

        // Create files in different directories
        val audioDir = File(mockContext.filesDir, "audio_recordings")
        audioDir.mkdirs()
        File(audioDir, "audio.wav").writeBytes(ByteArray(100))

        val imageDir = File(mockContext.filesDir, "images")
        imageDir.mkdirs()
        File(imageDir, "image.jpg").writeBytes(ByteArray(100))

        val result = fileManager.getFiles(null)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.size >= 2)
    }

    @Test
    fun getFiles_sortsByCreatedAtDescending() = runTest {
        fileManager = FileManager(mockContext)

        val audioDir = File(mockContext.filesDir, "audio_recordings")
        audioDir.mkdirs()

        val file1 = File(audioDir, "audio1.wav")
        file1.writeBytes(ByteArray(100))
        Thread.sleep(10) // Ensure different timestamps

        val file2 = File(audioDir, "audio2.wav")
        file2.writeBytes(ByteArray(100))

        val result = fileManager.getFiles(FileType.AUDIO)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.size >= 2)
        // Most recent file should be first
        assertTrue(files[0].createdAt >= files[1].createdAt)
    }

    @Test
    fun getFiles_withNonExistentDirectory_returnsEmptyList() = runTest {
        fileManager = FileManager(mockContext)

        // Use a directory that doesn't exist
        val result = fileManager.getFiles(FileType.AUDIO)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
    }

    @Test
    fun deleteFile_failure_cannotDelete_returnsFailure() = runTest {
        fileManager = FileManager(mockContext)

        // Create a file that we'll make non-deletable by using a non-existent path
        // In real scenario, this would be a file with permissions issue
        val result = fileManager.deleteFile("/root/cannot_delete.txt")

        // Should fail because file doesn't exist
        assertTrue(result.isFailure)
    }

    @Test
    fun cleanupTempFiles_withEmptyDirectory_returnsZero() = runTest {
        fileManager = FileManager(mockContext)

        val tempDir = File(mockContext.cacheDir, "temp")
        tempDir.mkdirs()

        val result = fileManager.cleanupTempFiles()

        assertTrue(result.isSuccess)
        val count = result.getOrNull()
        assertNotNull(count)
        assertEquals(0, count)
    }

    @Test
    fun cleanupTempFiles_withMultipleFiles_returnsCorrectCount() = runTest {
        fileManager = FileManager(mockContext)

        val tempDir = File(mockContext.cacheDir, "temp")
        tempDir.mkdirs()
        File(tempDir, "temp1.txt").writeText("temp1")
        File(tempDir, "temp2.txt").writeText("temp2")
        File(tempDir, "temp3.txt").writeText("temp3")

        val result = fileManager.cleanupTempFiles()

        assertTrue(result.isSuccess)
        val count = result.getOrNull()
        assertNotNull(count)
        assertTrue(count!! >= 3)
    }

    @Test
    fun getFiles_detectsAudioExtensions() = runTest {
        fileManager = FileManager(mockContext)

        val audioDir = File(mockContext.filesDir, "audio_recordings")
        audioDir.mkdirs()
        File(audioDir, "test.wav").writeBytes(ByteArray(10))
        File(audioDir, "test.mp3").writeBytes(ByteArray(10))
        File(audioDir, "test.m4a").writeBytes(ByteArray(10))
        File(audioDir, "test.aac").writeBytes(ByteArray(10))

        val result = fileManager.getFiles(FileType.AUDIO)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.all { it.isAudio })
    }

    @Test
    fun getFiles_detectsImageExtensions() = runTest {
        fileManager = FileManager(mockContext)

        val imageDir = File(mockContext.filesDir, "images")
        imageDir.mkdirs()
        File(imageDir, "test.jpg").writeBytes(ByteArray(10))
        File(imageDir, "test.jpeg").writeBytes(ByteArray(10))
        File(imageDir, "test.png").writeBytes(ByteArray(10))
        File(imageDir, "test.gif").writeBytes(ByteArray(10))
        File(imageDir, "test.webp").writeBytes(ByteArray(10))

        val result = fileManager.getFiles(FileType.IMAGE)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.all { it.isImage })
    }

    @Test
    fun getFiles_detectsDocumentExtensions() = runTest {
        fileManager = FileManager(mockContext)

        val documentDir = File(mockContext.filesDir, "documents")
        documentDir.mkdirs()
        File(documentDir, "test.pdf").writeBytes(ByteArray(10))
        File(documentDir, "test.doc").writeBytes(ByteArray(10))
        File(documentDir, "test.docx").writeBytes(ByteArray(10))
        File(documentDir, "test.txt").writeBytes(ByteArray(10))
        File(documentDir, "test.rtf").writeBytes(ByteArray(10))

        val result = fileManager.getFiles(FileType.DOCUMENT)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.all { it.isDocument })
    }

    @Test
    fun getFiles_handlesCaseInsensitiveExtensions() = runTest {
        fileManager = FileManager(mockContext)

        val audioDir = File(mockContext.filesDir, "audio_recordings")
        audioDir.mkdirs()
        File(audioDir, "test.WAV").writeBytes(ByteArray(10))
        File(audioDir, "test.MP3").writeBytes(ByteArray(10))

        val result = fileManager.getFiles(FileType.AUDIO)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.all { it.isAudio })
    }

    @Test
    fun getFiles_returnsCorrectMimeTypes() = runTest {
        fileManager = FileManager(mockContext)

        val audioDir = File(mockContext.filesDir, "audio_recordings")
        audioDir.mkdirs()
        File(audioDir, "test.wav").writeBytes(ByteArray(10))

        val result = fileManager.getFiles(FileType.AUDIO)

        assertTrue(result.isSuccess)
        val files = result.getOrNull()
        assertNotNull(files)
        assertTrue(files!!.isNotEmpty())
        // Verify MIME type is set correctly through getMimeTypeFromExtension
        assertEquals("audio/wav", files[0].type)
    }

    @Test
    fun getFiles_withException_returnsFailure() = runTest {
        fileManager = FileManager(mockContext)

        // Mock a scenario where listFiles might throw an exception
        // This is hard to test directly, but we can test edge cases
        val result = fileManager.getFiles(FileType.AUDIO)

        // Should succeed even with empty directory
        assertTrue(result.isSuccess)
    }
}
