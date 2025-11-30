package com.example.voicetutor.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcherFactory: () -> TestDispatcher = {
        val scheduler = TestCoroutineScheduler()
        UnconfinedTestDispatcher(scheduler)
    },
) : TestWatcher() {

    lateinit var testDispatcher: TestDispatcher

    override fun starting(description: Description) {
        testDispatcher = testDispatcherFactory()
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
