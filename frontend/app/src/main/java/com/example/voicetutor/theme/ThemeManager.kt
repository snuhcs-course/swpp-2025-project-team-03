package com.example.voicetutor.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.voicetutor.annotations.ExcludeFromJacocoGeneratedReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    LIGHT,
    DARK,
    AUTO,
}

data class ThemeState(
    val currentTheme: AppTheme = AppTheme.LIGHT,
    val isDarkMode: Boolean = false,
)

class ThemeManager(private val context: Context) {

    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    init {
        loadThemePreference()
    }

    /**
     * 테마 변경
     */
    fun setTheme(theme: AppTheme) {
        _themeState.value = _themeState.value.copy(
            currentTheme = theme,
            isDarkMode = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.AUTO -> isSystemDarkMode()
            },
        )
        saveThemePreference(theme)
    }

    /**
     * 다크 모드 토글
     */
    fun toggleDarkMode() {
        val newTheme = if (_themeState.value.isDarkMode) AppTheme.LIGHT else AppTheme.DARK
        setTheme(newTheme)
    }

    /**
     * 시스템 다크 모드 확인
     */
    private fun isSystemDarkMode(): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * 테마 설정 저장
     */
    private fun saveThemePreference(theme: AppTheme) {
        val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("app_theme", theme.name).apply()
    }

    /**
     * 테마 설정 로드
     */
    private fun loadThemePreference() {
        val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val savedTheme = sharedPrefs.getString("app_theme", AppTheme.LIGHT.name)
        val theme = AppTheme.valueOf(savedTheme ?: AppTheme.LIGHT.name)

        _themeState.value = _themeState.value.copy(
            currentTheme = theme,
            isDarkMode = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.AUTO -> isSystemDarkMode()
            },
        )
    }
}

// 다크 테마 색상 정의
object DarkColors {
    val PrimaryIndigo = Color(0xFF6366F1)
    val LightIndigo = Color(0xFF818CF8)
    val DarkIndigo = Color(0xFF4F46E5)

    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2C2C2C)

    val OnBackground = Color(0xFFE1E1E1)
    val OnSurface = Color(0xFFE1E1E1)
    val OnSurfaceVariant = Color(0xFFB3B3B3)

    val Gray50 = Color(0xFF2C2C2C)
    val Gray100 = Color(0xFF3C3C3C)
    val Gray200 = Color(0xFF4C4C4C)
    val Gray300 = Color(0xFF5C5C5C)
    val Gray400 = Color(0xFF6C6C6C)
    val Gray500 = Color(0xFF7C7C7C)
    val Gray600 = Color(0xFF8C8C8C)
    val Gray700 = Color(0xFF9C9C9C)
    val Gray800 = Color(0xFFACACAC)
    val Gray900 = Color(0xFFBCBCBC)

    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
}

// 라이트 테마 색상 정의
object LightColors {
    val PrimaryIndigo = Color(0xFF6366F1)
    val LightIndigo = Color(0xFF818CF8)
    val DarkIndigo = Color(0xFF4F46E5)

    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF8F9FA)

    val OnBackground = Color(0xFF1F2937)
    val OnSurface = Color(0xFF1F2937)
    val OnSurfaceVariant = Color(0xFF6B7280)

    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)

    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
}

@Composable
fun VoiceTutorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = DarkColors.PrimaryIndigo,
            primaryContainer = DarkColors.LightIndigo,
            secondary = DarkColors.LightIndigo,
            background = DarkColors.Background,
            surface = DarkColors.Surface,
            surfaceVariant = DarkColors.SurfaceVariant,
            onBackground = DarkColors.OnBackground,
            onSurface = DarkColors.OnSurface,
            onSurfaceVariant = DarkColors.OnSurfaceVariant,
            error = DarkColors.Error,
        )
    } else {
        lightColorScheme(
            primary = LightColors.PrimaryIndigo,
            primaryContainer = LightColors.LightIndigo,
            secondary = LightColors.LightIndigo,
            background = LightColors.Background,
            surface = LightColors.Surface,
            surfaceVariant = LightColors.SurfaceVariant,
            onBackground = LightColors.OnBackground,
            onSurface = LightColors.OnSurface,
            onSurfaceVariant = LightColors.OnSurfaceVariant,
            error = LightColors.Error,
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}

// 테마 관련 확장 함수들
val ColorScheme.success: Color
    get() = DarkColors.Success

val ColorScheme.warning: Color
    get() = DarkColors.Warning

val ColorScheme.info: Color
    get() = DarkColors.Info

// Composable 함수로 변경
@Composable
fun ColorScheme.gray50(): Color = if (isSystemInDarkTheme()) DarkColors.Gray50 else LightColors.Gray50

@Composable
fun ColorScheme.gray100(): Color = if (isSystemInDarkTheme()) DarkColors.Gray100 else LightColors.Gray100

@Composable
fun ColorScheme.gray200(): Color = if (isSystemInDarkTheme()) DarkColors.Gray200 else LightColors.Gray200

@Composable
fun ColorScheme.gray300(): Color = if (isSystemInDarkTheme()) DarkColors.Gray300 else LightColors.Gray300

@Composable
fun ColorScheme.gray400(): Color = if (isSystemInDarkTheme()) DarkColors.Gray400 else LightColors.Gray400

@Composable
fun ColorScheme.gray500(): Color = if (isSystemInDarkTheme()) DarkColors.Gray500 else LightColors.Gray500

@Composable
fun ColorScheme.gray600(): Color = if (isSystemInDarkTheme()) DarkColors.Gray600 else LightColors.Gray600

@Composable
fun ColorScheme.gray700(): Color = if (isSystemInDarkTheme()) DarkColors.Gray700 else LightColors.Gray700

@Composable
fun ColorScheme.gray800(): Color = if (isSystemInDarkTheme()) DarkColors.Gray800 else LightColors.Gray800

@Composable
fun ColorScheme.gray900(): Color = if (isSystemInDarkTheme()) DarkColors.Gray900 else LightColors.Gray900
