package com.example.voicetutor.theme

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for ThemeManager
 */
class ThemeManagerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor
    
    @Mock
    private lateinit var mockResources: Resources
    
    @Mock
    private lateinit var mockConfiguration: Configuration
    
    private lateinit var themeManager: ThemeManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        `when`(mockContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.configuration).thenReturn(mockConfiguration)
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_NO
    }

    @Test
    fun appTheme_enumValues_areCorrect() {
        assertEquals(3, AppTheme.values().size)
        assertTrue(AppTheme.values().contains(AppTheme.LIGHT))
        assertTrue(AppTheme.values().contains(AppTheme.DARK))
        assertTrue(AppTheme.values().contains(AppTheme.AUTO))
    }

    @Test
    fun themeState_creation_withAllFields_createsCorrectInstance() {
        val themeState = ThemeState(
            currentTheme = AppTheme.DARK,
            isDarkMode = true
        )
        
        assertEquals(AppTheme.DARK, themeState.currentTheme)
        assertTrue(themeState.isDarkMode)
    }

    @Test
    fun themeState_creation_withDefaults_usesDefaults() {
        val themeState = ThemeState()
        
        assertEquals(AppTheme.LIGHT, themeState.currentTheme)
        assertFalse(themeState.isDarkMode)
    }

    @Test
    fun appTheme_allThemes_haveUniqueNames() {
        val names = AppTheme.values().map { it.name }
        assertEquals(names.size, names.distinct().size)
    }
    
    @Test
    fun themeManager_initialization_loadsPreference() {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.DARK.name)
        
        themeManager = ThemeManager(mockContext)
        
        verify(mockSharedPreferences).getString("app_theme", AppTheme.LIGHT.name)
    }
    
    @Test
    fun setTheme_toLightMode_updatesState() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        
        themeManager = ThemeManager(mockContext)
        themeManager.setTheme(AppTheme.LIGHT)
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.LIGHT, state.currentTheme)
        assertFalse(state.isDarkMode)
    }
    
    @Test
    fun setTheme_toDarkMode_updatesState() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        
        themeManager = ThemeManager(mockContext)
        themeManager.setTheme(AppTheme.DARK)
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.DARK, state.currentTheme)
        assertTrue(state.isDarkMode)
    }
    
    @Test
    fun setTheme_toAutoMode_checksSystemTheme() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_YES
        
        themeManager = ThemeManager(mockContext)
        themeManager.setTheme(AppTheme.AUTO)
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.AUTO, state.currentTheme)
        assertTrue(state.isDarkMode)
    }
    
    @Test
    fun setTheme_savesPreference() {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        
        themeManager = ThemeManager(mockContext)
        themeManager.setTheme(AppTheme.DARK)
        
        verify(mockEditor).putString("app_theme", AppTheme.DARK.name)
        verify(mockEditor).apply()
    }
    
    @Test
    fun toggleDarkMode_fromLight_switchesToDark() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        
        themeManager = ThemeManager(mockContext)
        themeManager.toggleDarkMode()
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.DARK, state.currentTheme)
        assertTrue(state.isDarkMode)
    }
    
    @Test
    fun toggleDarkMode_fromDark_switchesToLight() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.DARK.name)
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_YES
        
        themeManager = ThemeManager(mockContext)
        themeManager.toggleDarkMode()
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.LIGHT, state.currentTheme)
        assertFalse(state.isDarkMode)
    }
    
    @Test
    fun darkColors_haveCorrectValues() {
        assertNotNull(DarkColors.PrimaryIndigo)
        assertNotNull(DarkColors.Background)
        assertNotNull(DarkColors.Surface)
        assertNotNull(DarkColors.Success)
        assertNotNull(DarkColors.Warning)
        assertNotNull(DarkColors.Error)
        assertNotNull(DarkColors.Info)
    }
    
    @Test
    fun lightColors_haveCorrectValues() {
        assertNotNull(LightColors.PrimaryIndigo)
        assertNotNull(LightColors.Background)
        assertNotNull(LightColors.Surface)
        assertNotNull(LightColors.Success)
        assertNotNull(LightColors.Warning)
        assertNotNull(LightColors.Error)
        assertNotNull(LightColors.Info)
    }
    
    @Test
    fun themeState_copy_createsNewInstance() {
        val original = ThemeState(AppTheme.DARK, true)
        val copy = original.copy(currentTheme = AppTheme.LIGHT)
        
        assertEquals(AppTheme.LIGHT, copy.currentTheme)
        assertTrue(copy.isDarkMode)
    }
    
    @Test
    fun themeState_equality_worksCorrectly() {
        val state1 = ThemeState(AppTheme.LIGHT, false)
        val state2 = ThemeState(AppTheme.LIGHT, false)
        val state3 = ThemeState(AppTheme.DARK, true)
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }
    
    @Test
    fun loadThemePreference_withNullValue_usesDefault() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(null)
        
        themeManager = ThemeManager(mockContext)
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.LIGHT, state.currentTheme)
    }

    @Test
    fun setTheme_toAutoMode_withLightSystemTheme_setsLightMode() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_NO
        
        themeManager = ThemeManager(mockContext)
        themeManager.setTheme(AppTheme.AUTO)
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.AUTO, state.currentTheme)
        assertFalse(state.isDarkMode)
    }

    @Test
    fun toggleDarkMode_fromAutoMode_switchesToDark() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.AUTO.name)
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_NO
        
        themeManager = ThemeManager(mockContext)
        // Set to AUTO first
        themeManager.setTheme(AppTheme.AUTO)
        // Then toggle (should switch to DARK since AUTO with light system = light mode)
        themeManager.toggleDarkMode()
        
        val state = themeManager.themeState.first()
        assertEquals(AppTheme.DARK, state.currentTheme)
        assertTrue(state.isDarkMode)
    }

    @Test
    fun setTheme_savesPreferenceForAllThemes() {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        
        themeManager = ThemeManager(mockContext)
        
        themeManager.setTheme(AppTheme.LIGHT)
        verify(mockEditor).putString("app_theme", AppTheme.LIGHT.name)
        
        themeManager.setTheme(AppTheme.DARK)
        verify(mockEditor).putString("app_theme", AppTheme.DARK.name)
        
        themeManager.setTheme(AppTheme.AUTO)
        verify(mockEditor).putString("app_theme", AppTheme.AUTO.name)
    }

    @Test
    fun themeState_flow_emitsUpdates() = runTest {
        `when`(mockSharedPreferences.getString("app_theme", AppTheme.LIGHT.name))
            .thenReturn(AppTheme.LIGHT.name)
        
        themeManager = ThemeManager(mockContext)
        
        val initialState = themeManager.themeState.first()
        assertEquals(AppTheme.LIGHT, initialState.currentTheme)
        
        themeManager.setTheme(AppTheme.DARK)
        val updatedState = themeManager.themeState.first()
        assertEquals(AppTheme.DARK, updatedState.currentTheme)
        assertTrue(updatedState.isDarkMode)
    }
}

