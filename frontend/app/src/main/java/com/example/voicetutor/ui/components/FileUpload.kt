package com.example.voicetutor.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.file.FileManager
import com.example.voicetutor.file.FileType
import com.example.voicetutor.ui.theme.*
import kotlinx.coroutines.launch

enum class FileUploadState {
    Idle,
    Uploading,
    Success,
    Error
}

data class UploadedFile(
    val name: String,
    val size: String,
    val type: String
)

@Composable
fun VTFileUpload(
    modifier: Modifier = Modifier,
    state: FileUploadState = FileUploadState.Idle,
    uploadProgress: Float = 0f,
    uploadedFiles: List<UploadedFile> = emptyList(),
    acceptedTypes: String = "PDF, DOC, TXT 파일",
    maxSizeMB: Int = 10,
    onFileSelect: () -> Unit = {},
    onFileRemove: (UploadedFile) -> Unit = {},
    fileType: FileType = FileType.DOCUMENT
) {
    val context = LocalContext.current
    val fileManager = remember { FileManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var selectedFiles by remember { mutableStateOf<List<com.example.voicetutor.file.FileInfo>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 파일 선택 런처
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                isUploading = true
                errorMessage = null
                
                try {
                    val newFiles = mutableListOf<com.example.voicetutor.file.FileInfo>()
                    
                    uris.forEach { uri ->
                        fileManager.saveFile(uri, fileType = fileType)
                            .onSuccess { fileInfo ->
                                newFiles.add(fileInfo)
                            }
                            .onFailure { exception ->
                                errorMessage = "파일 저장 실패: ${exception.message}"
                            }
                    }
                    
                    selectedFiles = newFiles
                    onFileSelect()
                } catch (e: Exception) {
                    errorMessage = "파일 업로드 중 오류: ${e.message}"
                } finally {
                    isUploading = false
                }
            }
        }
    }
    
    val borderColor = when (state) {
        FileUploadState.Idle -> Gray300
        FileUploadState.Uploading -> PrimaryIndigo
        FileUploadState.Success -> Success
        FileUploadState.Error -> Error
    }
    
    val backgroundColor = when (state) {
        FileUploadState.Idle -> Gray50
        FileUploadState.Uploading -> PrimaryIndigo.copy(alpha = 0.05f)
        FileUploadState.Success -> Success.copy(alpha = 0.05f)
        FileUploadState.Error -> Error.copy(alpha = 0.05f)
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upload area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(enabled = state != FileUploadState.Uploading) {
                    onFileSelect()
                }
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                FileUploadState.Idle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "파일을 업로드하세요",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "클릭하여 파일 선택 또는 드래그 앤 드롭",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$acceptedTypes (최대 ${maxSizeMB}MB)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                FileUploadState.Uploading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier.size(48.dp),
                            color = PrimaryIndigo,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "업로드 중...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(uploadProgress * 100).toInt()}% 완료",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                    }
                }
                
                FileUploadState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "업로드 완료!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Success
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "파일이 성공적으로 업로드되었습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                FileUploadState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "업로드 실패",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "파일 업로드 중 오류가 발생했습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        VTButton(
                            text = "다시 시도",
                            onClick = onFileSelect,
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Small
                        )
                    }
                }
            }
        }
        
        // Uploaded files list
        if (uploadedFiles.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "업로드된 파일 (${uploadedFiles.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                uploadedFiles.forEach { file ->
                    FileItem(
                        file = file,
                        onRemove = { onFileRemove(file) }
                    )
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: UploadedFile,
    onRemove: () -> Unit
) {
    VTCard(
        variant = CardVariant.Outlined,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (file.type.lowercase()) {
                            "pdf" -> Icons.Filled.PictureAsPdf
                            "doc", "docx" -> Icons.Filled.Description
                            "txt" -> Icons.Filled.TextSnippet
                            else -> Icons.Filled.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                    Text(
                        text = "${file.type.uppercase()} • ${file.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "파일 제거",
                    tint = Gray500,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileUploadPreview() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            VTFileUpload(
                state = FileUploadState.Idle
            )
            
            VTFileUpload(
                state = FileUploadState.Uploading,
                uploadProgress = 0.65f
            )
            
            VTFileUpload(
                state = FileUploadState.Success,
                uploadedFiles = listOf(
                    UploadedFile("assignment.pdf", "2.3MB", "PDF"),
                    UploadedFile("notes.docx", "1.1MB", "DOCX")
                )
            )
            
            VTFileUpload(
                state = FileUploadState.Error
            )
        }
    }
}
