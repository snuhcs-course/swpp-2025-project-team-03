package com.example.voicetutor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.voicetutor.MainActivity

data class NotificationData(
    val id: Int,
    val title: String,
    val message: String,
    val type: NotificationType,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val autoCancel: Boolean = true,
    val actions: List<NotificationAction> = emptyList()
)

enum class NotificationType {
    ASSIGNMENT_DUE,
    NEW_MESSAGE,
    GRADE_UPDATE,
    SYSTEM_UPDATE,
    REMINDER
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

data class NotificationAction(
    val title: String,
    val action: String,
    val icon: Int = android.R.drawable.ic_dialog_info
)

class VoiceTutorNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID_ASSIGNMENTS = "assignments"
        private const val CHANNEL_ID_MESSAGES = "messages"
        private const val CHANNEL_ID_GRADES = "grades"
        private const val CHANNEL_ID_SYSTEM = "system"
        private const val CHANNEL_ID_REMINDERS = "reminders"
        
        private const val NOTIFICATION_ID_ASSIGNMENT = 1000
        private const val NOTIFICATION_ID_MESSAGE = 2000
        private const val NOTIFICATION_ID_GRADE = 3000
        private const val NOTIFICATION_ID_SYSTEM = 4000
        private const val NOTIFICATION_ID_REMINDER = 5000
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * 알림 채널 생성
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_ASSIGNMENTS,
                    "과제 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "과제 마감일 및 새 과제 알림"
                    enableVibration(true)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_MESSAGES,
                    "메시지 알림",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "새 메시지 및 채팅 알림"
                    enableVibration(true)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_GRADES,
                    "성적 알림",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "성적 업데이트 및 피드백 알림"
                    enableVibration(false)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_SYSTEM,
                    "시스템 알림",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "앱 업데이트 및 시스템 알림"
                    enableVibration(false)
                    setShowBadge(false)
                },
                
                NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "리마인더",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "학습 리마인더 및 일정 알림"
                    enableVibration(true)
                    setShowBadge(true)
                }
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    /**
     * 알림 표시
     */
    fun showNotification(notificationData: NotificationData) {
        val channelId = getChannelId(notificationData.type)
        val notificationId = getNotificationId(notificationData.type, notificationData.id)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", notificationData.type.name)
            putExtra("notification_id", notificationData.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationData.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationData.title)
            .setContentText(notificationData.message)
            .setPriority(getNotificationPriority(notificationData.priority))
            .setAutoCancel(notificationData.autoCancel)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationData.message))
        
        // 액션 버튼 추가
        notificationData.actions.forEach { action ->
            val actionIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("action", action.action)
                putExtra("notification_id", notificationData.id)
            }
            
            val actionPendingIntent = PendingIntent.getActivity(
                context,
                action.hashCode(),
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.addAction(
                NotificationCompat.Action.Builder(
                    action.icon,
                    action.title,
                    actionPendingIntent
                ).build()
            )
        }
        
        // 우선순위에 따른 추가 설정
        when (notificationData.priority) {
            NotificationPriority.URGENT -> {
                builder.setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVibrate(longArrayOf(0, 1000, 500, 1000))
            }
            NotificationPriority.HIGH -> {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            }
            else -> {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            }
        }
        
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
    
    /**
     * 과제 마감일 알림
     */
    fun showAssignmentDueNotification(
        assignmentTitle: String,
        dueDate: String,
        assignmentId: Int
    ) {
        val notificationData = NotificationData(
            id = assignmentId,
            title = "📝 과제 마감 임박",
            message = "'$assignmentTitle' 과제가 $dueDate 에 마감됩니다.",
            type = NotificationType.ASSIGNMENT_DUE,
            priority = NotificationPriority.HIGH,
            actions = listOf(
                NotificationAction("과제 보기", "view_assignment"),
                NotificationAction("나중에", "snooze")
            )
        )
        
        showNotification(notificationData)
    }
    
    /**
     * 새 메시지 알림
     */
    fun showNewMessageNotification(
        senderName: String,
        messagePreview: String,
        messageId: Int
    ) {
        val notificationData = NotificationData(
            id = messageId,
            title = "💬 새 메시지",
            message = "$senderName: $messagePreview",
            type = NotificationType.NEW_MESSAGE,
            priority = NotificationPriority.NORMAL,
            actions = listOf(
                NotificationAction("답장", "reply"),
                NotificationAction("읽음", "mark_read")
            )
        )
        
        showNotification(notificationData)
    }
    
    /**
     * 성적 업데이트 알림
     */
    fun showGradeUpdateNotification(
        assignmentTitle: String,
        grade: String,
        assignmentId: Int
    ) {
        val notificationData = NotificationData(
            id = assignmentId,
            title = "📊 성적 업데이트",
            message = "'$assignmentTitle' 과제 성적이 $grade 로 업데이트되었습니다.",
            type = NotificationType.GRADE_UPDATE,
            priority = NotificationPriority.NORMAL,
            actions = listOf(
                NotificationAction("성적 보기", "view_grade"),
                NotificationAction("피드백 보기", "view_feedback")
            )
        )
        
        showNotification(notificationData)
    }
    
    /**
     * 학습 리마인더 알림
     */
    fun showStudyReminderNotification(
        subject: String,
        reminderId: Int
    ) {
        val notificationData = NotificationData(
            id = reminderId,
            title = "⏰ 학습 시간",
            message = "$subject 공부 시간입니다!",
            type = NotificationType.REMINDER,
            priority = NotificationPriority.NORMAL,
            actions = listOf(
                NotificationAction("공부 시작", "start_study"),
                NotificationAction("나중에", "snooze")
            )
        )
        
        showNotification(notificationData)
    }
    
    /**
     * 알림 취소
     */
    fun cancelNotification(notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }
    
    /**
     * 모든 알림 취소
     */
    fun cancelAllNotifications() {
        with(NotificationManagerCompat.from(context)) {
            cancelAll()
        }
    }
    
    /**
     * 알림 타입별 채널 ID 반환
     */
    private fun getChannelId(type: NotificationType): String {
        return when (type) {
            NotificationType.ASSIGNMENT_DUE -> CHANNEL_ID_ASSIGNMENTS
            NotificationType.NEW_MESSAGE -> CHANNEL_ID_MESSAGES
            NotificationType.GRADE_UPDATE -> CHANNEL_ID_GRADES
            NotificationType.SYSTEM_UPDATE -> CHANNEL_ID_SYSTEM
            NotificationType.REMINDER -> CHANNEL_ID_REMINDERS
        }
    }
    
    /**
     * 알림 ID 생성
     */
    private fun getNotificationId(type: NotificationType, id: Int): Int {
        return when (type) {
            NotificationType.ASSIGNMENT_DUE -> NOTIFICATION_ID_ASSIGNMENT + id
            NotificationType.NEW_MESSAGE -> NOTIFICATION_ID_MESSAGE + id
            NotificationType.GRADE_UPDATE -> NOTIFICATION_ID_GRADE + id
            NotificationType.SYSTEM_UPDATE -> NOTIFICATION_ID_SYSTEM + id
            NotificationType.REMINDER -> NOTIFICATION_ID_REMINDER + id
        }
    }
    
    /**
     * 알림 우선순위 변환
     */
    private fun getNotificationPriority(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.URGENT -> NotificationCompat.PRIORITY_MAX
        }
    }
}
