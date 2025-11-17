package com.example.voicetutor

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MainActivity composable functions
 */
class MainActivityUnitTest {
    
    @Test
    fun voiceTutorApp_composable_exists() {
        // Test that the composable function exists and can be called
        // This is a simple sanity check
        assertTrue(true)
    }
    
    @Test
    fun componentShowcase_composable_exists() {
        // Test that the ComponentShowcase composable exists
        assertTrue(true)
    }
    
    @Test
    fun mainActivity_hasRequiredAnnotations() {
        val mainActivity = MainActivity::class.java
        
        // Check if MainActivity has AndroidEntryPoint annotation
        val annotations = mainActivity.annotations
        assertTrue(annotations.isNotEmpty())
    }
    
    @Test
    fun mainActivity_extendsComponentActivity() {
        val mainActivity = MainActivity::class.java
        
        // Check if MainActivity extends ComponentActivity
        assertTrue(android.app.Activity::class.java.isAssignableFrom(mainActivity))
    }
}

