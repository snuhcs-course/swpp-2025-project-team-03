package com.example.voicetutor

import org.junit.Assert.*
import org.junit.Test

class VoiceTutorApplicationUnitTest {

    @Test
    fun voiceTutorApplication_hasHiltAndroidAppAnnotation() {
        val application = VoiceTutorApplication::class.java
        val annotations = application.annotations
        assertTrue(annotations.isNotEmpty())
    }

    @Test
    fun voiceTutorApplication_extendsApplication() {
        val application = VoiceTutorApplication::class.java
        assertTrue(android.app.Application::class.java.isAssignableFrom(application))
    }

    @Test
    fun apiServiceEntryPoint_interfaceExists() {
        val entryPoint = ApiServiceEntryPoint::class.java
        assertTrue(entryPoint.isInterface)
    }

    @Test
    fun apiServiceEntryPoint_hasApiServiceMethod() {
        val entryPoint = ApiServiceEntryPoint::class.java
        val methods = entryPoint.declaredMethods
        val hasApiServiceMethod = methods.any { it.name == "apiService" }
        assertTrue(hasApiServiceMethod)
    }
}
