package com.example.voicetutor

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt integration tests.
 * This ensures that tests use HiltTestApplication instead of the production Application class.
 *
 * Reference: https://developer.android.com/training/dependency-injection/hilt-testing#instrumented-tests
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
