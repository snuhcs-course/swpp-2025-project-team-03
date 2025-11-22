package com.example.voicetutor.file

import android.content.Context
import android.net.Uri
import com.example.voicetutor.annotations.ExcludeFromJacocoGeneratedReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val type: String,
    val createdAt: Date,
    val isAudio: Boolean = false,
    val isImage: Boolean = false,
    val isDocument: Boolean = false,
)

enum class FileType {
    AUDIO,
    IMAGE,
    DOCUMENT,
    OTHER,
}

class FileManager(private val context: Context) {

    private val audioDir = File(context.filesDir, "audio_recordings")
    private val documentDir = File(context.filesDir, "documents")
    private val imageDir = File(context.filesDir, "images")
    private val tempDir = File(context.cacheDir, "temp")

    init {
        // 디렉토리 생성
        listOf(audioDir, documentDir, imageDir, tempDir).forEach { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }

    /**
     * 파일 저장
     */
    suspend fun saveFile(
        uri: Uri,
        fileName: String? = null,
        fileType: FileType = FileType.OTHER,
    ): Result<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("파일을 읽을 수 없습니다"))

            val targetDir = when (fileType) {
                FileType.AUDIO -> audioDir
                FileType.IMAGE -> imageDir
                FileType.DOCUMENT -> documentDir
                FileType.OTHER -> tempDir
            }

            val finalFileName = fileName ?: generateFileName(uri, fileType)
            val targetFile = File(targetDir, finalFileName)

            // 파일 복사
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            val fileInfo = FileInfo(
                name = finalFileName,
                path = targetFile.absolutePath,
                size = targetFile.length(),
                type = getMimeType(uri),
                createdAt = Date(),
                isAudio = fileType == FileType.AUDIO,
                isImage = fileType == FileType.IMAGE,
                isDocument = fileType == FileType.DOCUMENT,
            )

            Result.success(fileInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 오디오 파일 저장
     */
    suspend fun saveAudioFile(
        sourceFile: File,
        customName: String? = null,
    ): Result<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val fileName = customName ?: generateAudioFileName()
            val targetFile = File(audioDir, fileName)

            sourceFile.copyTo(targetFile, overwrite = true)

            val fileInfo = FileInfo(
                name = fileName,
                path = targetFile.absolutePath,
                size = targetFile.length(),
                type = "audio/wav",
                createdAt = Date(),
                isAudio = true,
            )

            Result.success(fileInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 파일 목록 조회
     */
    suspend fun getFiles(fileType: FileType? = null): Result<List<FileInfo>> = withContext(Dispatchers.IO) {
        try {
            val directories = when (fileType) {
                FileType.AUDIO -> listOf(audioDir)
                FileType.IMAGE -> listOf(imageDir)
                FileType.DOCUMENT -> listOf(documentDir)
                FileType.OTHER -> listOf(tempDir)
                null -> listOf(audioDir, documentDir, imageDir, tempDir)
            }

            val files = directories.flatMap { dir ->
                if (dir.exists()) {
                    dir.listFiles()?.map { file ->
                        FileInfo(
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            type = getMimeTypeFromExtension(file.extension),
                            createdAt = Date(file.lastModified()),
                            isAudio = file.extension.lowercase() in listOf("wav", "mp3", "m4a", "aac"),
                            isImage = file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp"),
                            isDocument = file.extension.lowercase() in listOf("pdf", "doc", "docx", "txt", "rtf"),
                        )
                    } ?: emptyList()
                } else {
                    emptyList()
                }
            }.sortedByDescending { it.createdAt }

            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 파일 삭제
     */
    suspend fun deleteFile(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                if (file.delete()) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("파일 삭제에 실패했습니다"))
                }
            } else {
                Result.failure(Exception("파일을 찾을 수 없습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 파일 크기 조회
     */
    fun getFileSize(filePath: String): Long {
        return File(filePath).length()
    }

    /**
     * 파일 존재 여부 확인
     */
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    /**
     * 파일명 생성
     */
    private fun generateFileName(uri: Uri, fileType: FileType): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = getFileExtension(uri) ?: getDefaultExtension(fileType)
        return "${fileType.name.lowercase()}_$timestamp.$extension"
    }

    /**
     * 오디오 파일명 생성
     */
    private fun generateAudioFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "voice_recording_$timestamp.wav"
    }

    /**
     * URI에서 파일 확장자 추출
     */
    private fun getFileExtension(uri: Uri): String? {
        val fileName = uri.lastPathSegment
        val extension = fileName?.substringAfterLast('.', "")
        return if (extension.isNullOrBlank()) null else extension
    }

    /**
     * 파일 타입별 기본 확장자
     */
    private fun getDefaultExtension(fileType: FileType): String {
        return when (fileType) {
            FileType.AUDIO -> "wav"
            FileType.IMAGE -> "jpg"
            FileType.DOCUMENT -> "pdf" // PDF 파일의 기본 확장자를 pdf로 변경
            FileType.OTHER -> "bin"
        }
    }

    /**
     * MIME 타입 조회
     */
    private fun getMimeType(uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }

    /**
     * 확장자로부터 MIME 타입 조회
     */
    private fun getMimeTypeFromExtension(extension: String): String {
        return when (extension.lowercase()) {
            "wav" -> "audio/wav"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            "rtf" -> "application/rtf"
            else -> "application/octet-stream"
        }
    }

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 변환
     */
    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    /**
     * 임시 파일 정리
     */
    suspend fun cleanupTempFiles(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val tempFiles = tempDir.listFiles()
            var deletedCount = 0

            tempFiles?.forEach { file ->
                if (file.delete()) {
                    deletedCount++
                }
            }

            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
