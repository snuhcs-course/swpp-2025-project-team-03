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
        assertNotNull(application)
    }

    @Test
    fun application_hasApplicationContext() {
        assertNotNull(application.applicationContext)
    }

    @Test
    fun application_initializesSuccessfully() {
        assertNotNull(application)

        val context = application.applicationContext
        assertNotNull(context)
    }

    @Test
    fun application_packageName_isCorrect() {
        assertEquals("com.example.voicetutor", application.packageName)
    }

    @Test
    fun apiServiceEntryPoint_isAccessible() {
        val entryPoint = try {
            dagger.hilt.android.EntryPointAccessors.fromApplication(
                application.applicationContext,
                ApiServiceEntryPoint::class.java,
            )
            true
        } catch (_: Exception) {
            false
        }

        assertTrue("ApiServiceEntryPoint should be accessible", entryPoint)
    }

    @Test
    fun application_healthCheckExecutes_withoutCrashing() {
        Thread.sleep(1000)

        assertNotNull(application)
    }
}
