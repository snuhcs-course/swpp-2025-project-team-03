package com.example.voicetutor

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for VoiceTutorApplication
 */
class VoiceTutorApplicationUnitTest {

    @Test
    fun voiceTutorApplication_hasHiltAndroidAppAnnotation() {
        val application = VoiceTutorApplication::class.java

        // Check if VoiceTutorApplication has HiltAndroidApp annotation
        val annotations = application.annotations
        assertTrue(annotations.isNotEmpty())
    }

    @Test
    fun voiceTutorApplication_extendsApplication() {
        val application = VoiceTutorApplication::class.java

        // Check if VoiceTutorApplication extends Application
        assertTrue(android.app.Application::class.java.isAssignableFrom(application))
    }

    @Test
    fun apiServiceEntryPoint_interfaceExists() {
        val entryPoint = ApiServiceEntryPoint::class.java

        // Check if ApiServiceEntryPoint is an interface
        assertTrue(entryPoint.isInterface)
    }

    @Test
    fun apiServiceEntryPoint_hasApiServiceMethod() {
        val entryPoint = ApiServiceEntryPoint::class.java

        // Check if the interface has the apiService method
        val methods = entryPoint.declaredMethods
        val hasApiServiceMethod = methods.any { it.name == "apiService" }
        assertTrue(hasApiServiceMethod)
    }
}
