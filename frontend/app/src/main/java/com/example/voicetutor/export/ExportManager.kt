package com.example.voicetutor.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ExportData(
    val title: String,
    val content: String,
    val type: ExportType,
    val metadata: Map<String, String> = emptyMap(),
)

enum class ExportType {
    PDF,
    TEXT,
    CSV,
    JSON,
}

data class ExportResult(
    val success: Boolean,
    val filePath: String? = null,
    val error: String? = null,
)

class ExportManager(private val context: Context) {

    private val exportDir = File(context.getExternalFilesDir(null), "exports")

    init {
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
    }

    /**
     * 과제 리포트 PDF 내보내기
     */
    suspend fun exportAssignmentReport(
        assignmentTitle: String,
        studentName: String,
        content: String,
        grade: String? = null,
        feedback: String? = null,
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "assignment_report_$timestamp.pdf"
            val file = File(exportDir, fileName)

            val pdfContent = generateAssignmentReportPDF(
                assignmentTitle = assignmentTitle,
                studentName = studentName,
                content = content,
                grade = grade,
                feedback = feedback,
            )

            FileOutputStream(file).use { fos ->
                fos.write(pdfContent.toByteArray())
            }

            ExportResult(success = true, filePath = file.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message)
        }
    }

    /**
     * 학생 성적표 CSV 내보내기
     */
    suspend fun exportGradeSheet(
        studentName: String,
        grades: List<GradeData>,
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "grade_sheet_${studentName}_$timestamp.csv"
            val file = File(exportDir, fileName)

            val csvContent = generateGradeSheetCSV(studentName, grades)

            FileOutputStream(file).use { fos ->
                fos.write(csvContent.toByteArray())
            }

            ExportResult(success = true, filePath = file.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message)
        }
    }

    /**
     * 학습 진도 리포트 내보내기
     */
    suspend fun exportProgressReport(
        studentName: String,
        progressData: ProgressData,
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "progress_report_${studentName}_$timestamp.pdf"
            val file = File(exportDir, fileName)

            val pdfContent = generateProgressReportPDF(studentName, progressData)

            FileOutputStream(file).use { fos ->
                fos.write(pdfContent.toByteArray())
            }

            ExportResult(success = true, filePath = file.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message)
        }
    }

    /**
     * 음성 녹음 파일 내보내기
     */
    suspend fun exportAudioFile(
        audioFilePath: String,
        customName: String? = null,
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(audioFilePath)
            if (!sourceFile.exists()) {
                return@withContext ExportResult(success = false, error = "오디오 파일을 찾을 수 없습니다")
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = customName ?: "voice_recording_$timestamp.wav"
            val targetFile = File(exportDir, fileName)

            sourceFile.copyTo(targetFile, overwrite = true)

            ExportResult(success = true, filePath = targetFile.absolutePath)
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message)
        }
    }

    /**
     * 파일 공유
     */
    fun shareFile(filePath: String, mimeType: String = "application/octet-stream") {
        val file = File(filePath)
        if (!file.exists()) return

        val uri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "파일 공유"))
    }

    /**
     * 과제 리포트 PDF 생성
     */
    private fun generateAssignmentReportPDF(
        assignmentTitle: String,
        studentName: String,
        content: String,
        grade: String?,
        feedback: String?,
    ): String {
        // 간단한 HTML 기반 PDF 생성 (실제로는 iText나 다른 PDF 라이브러리 사용 권장)
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>과제 리포트</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .title { font-size: 24px; font-weight: bold; color: #333; }
                    .subtitle { font-size: 16px; color: #666; margin-top: 10px; }
                    .section { margin: 20px 0; }
                    .section-title { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 10px; }
                    .content { line-height: 1.6; color: #444; }
                    .grade { font-size: 20px; font-weight: bold; color: #4CAF50; }
                    .footer { margin-top: 40px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">과제 리포트</div>
                    <div class="subtitle">VoiceTutor</div>
                </div>
                
                <div class="section">
                    <div class="section-title">과제 정보</div>
                    <div class="content">
                        <strong>과제명:</strong> $assignmentTitle<br>
                        <strong>학생명:</strong> $studentName<br>
                        <strong>제출일:</strong> ${SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault()).format(Date())}
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">과제 내용</div>
                    <div class="content">$content</div>
                </div>
                
                ${grade?.let {
            """
                <div class="section">
                    <div class="section-title">성적</div>
                    <div class="grade">$it</div>
                </div>
                """
        } ?: ""}
                
                ${feedback?.let {
            """
                <div class="section">
                    <div class="section-title">피드백</div>
                    <div class="content">$it</div>
                </div>
                """
        } ?: ""}
                
                <div class="footer">
                    이 리포트는 VoiceTutor 앱에서 생성되었습니다.
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 성적표 CSV 생성
     */
    private fun generateGradeSheetCSV(studentName: String, grades: List<GradeData>): String {
        val csv = StringBuilder()
        csv.appendLine("과제명,과목,점수,만점,백분율,제출일")

        grades.forEach { grade ->
            csv.appendLine("${grade.assignmentName},${grade.subject},${grade.score},${grade.maxScore},${grade.percentage}%,${grade.submitDate}")
        }

        return csv.toString()
    }

    /**
     * 진도 리포트 PDF 생성
     */
    private fun generateProgressReportPDF(studentName: String, progressData: ProgressData): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>학습 진도 리포트</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .title { font-size: 24px; font-weight: bold; color: #333; }
                    .stats { display: flex; justify-content: space-around; margin: 30px 0; }
                    .stat-item { text-align: center; }
                    .stat-value { font-size: 24px; font-weight: bold; color: #4CAF50; }
                    .stat-label { font-size: 14px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">학습 진도 리포트</div>
                    <div class="subtitle">$studentName</div>
                </div>
                
                <div class="stats">
                    <div class="stat-item">
                        <div class="stat-value">${progressData.completedAssignments}</div>
                        <div class="stat-label">완료한 과제</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">${progressData.averageScore}%</div>
                        <div class="stat-label">평균 점수</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">${progressData.studyHours}시간</div>
                        <div class="stat-label">학습 시간</div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 내보내기 파일 목록 조회
     */
    fun getExportFiles(): List<File> {
        return exportDir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * 내보내기 파일 삭제
     */
    fun deleteExportFile(fileName: String): Boolean {
        val file = File(exportDir, fileName)
        return file.delete()
    }

    /**
     * 모든 내보내기 파일 삭제
     */
    fun clearAllExports(): Int {
        var deletedCount = 0
        exportDir.listFiles()?.forEach { file ->
            if (file.delete()) {
                deletedCount++
            }
        }
        return deletedCount
    }
}

// 데이터 클래스들
data class GradeData(
    val assignmentName: String,
    val subject: String,
    val score: Int,
    val maxScore: Int,
    val percentage: Int,
    val submitDate: String,
)

data class ProgressData(
    val completedAssignments: Int,
    val totalAssignments: Int,
    val averageScore: Int,
    val studyHours: Int,
    val subjects: List<String>,
)
