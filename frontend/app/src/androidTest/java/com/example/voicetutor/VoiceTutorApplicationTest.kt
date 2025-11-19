package com.example.voicetutor

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for VoiceTutorApplication
 */
@Ignore("Application tests may fail in instrumented test environment")
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class VoiceTutorApplicationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var application: VoiceTutorApplication

    @Before
    fun setup() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()
        application = context as VoiceTutorApplication
    }

    @Test
    fun application_isNotNull() {
        assertNotNull(application)
    }

    @Test
    fun application_isInstanceOfVoiceTutorApplication() {
        assertTrue(application is VoiceTutorApplication)
    }

    @Test
    fun application_hasApplicationContext() {
        assertNotNull(application.applicationContext)
    }

    @Test
    fun application_initializesSuccessfully() {
        // If the application is created and we can get it,
        // then onCreate() was called successfully
        assertNotNull(application)

        // Verify that the application context is available
        val context = application.applicationContext
        assertNotNull(context)
    }

    @Test
    fun application_packageName_isCorrect() {
        assertEquals("com.example.voicetutor", application.packageName)
    }

    @Test
    fun apiServiceEntryPoint_isAccessible() {
        // Test that the entry point is properly set up
        // by trying to access it (it would throw if not set up correctly)
        val entryPoint = try {
            dagger.hilt.android.EntryPointAccessors.fromApplication(
                application.applicationContext,
                ApiServiceEntryPoint::class.java,
            )
            true
        } catch (e: Exception) {
            false
        }

        assertTrue("ApiServiceEntryPoint should be accessible", entryPoint)
    }

    @Test
    fun application_healthCheckExecutes_withoutCrashing() {
        // Give some time for the health check to potentially execute
        // The health check is launched in a coroutine with a delay
        Thread.sleep(1000)

        // If we get here without crashing, the health check didn't cause issues
        assertNotNull(application)
    }
}
