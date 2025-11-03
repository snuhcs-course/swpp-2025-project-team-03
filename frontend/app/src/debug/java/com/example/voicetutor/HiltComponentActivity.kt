package com.example.voicetutor

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for Hilt-enabled Compose tests.
 * This is required because Compose tests use ComponentActivity to set content,
 * which is not annotated with @AndroidEntryPoint by default.
 */
@AndroidEntryPoint
class HiltComponentActivity : ComponentActivity()
