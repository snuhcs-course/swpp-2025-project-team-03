package com.example.voicetutor.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PermissionUtilsTest {

    @Mock
    lateinit var context: Context

    @Test
    fun getRequiredPermissions_returnsCorrectPermissions() {
        // Act
        val permissions = PermissionUtils.getRequiredPermissions()

        // Assert
        assert(permissions.size == 3)
        assert(permissions.contains(Manifest.permission.RECORD_AUDIO))
        assert(permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        assert(permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    // Note: Android framework-dependent tests are skipped because ContextCompat.checkSelfPermission
    // requires Robolectric or Android instrumentation tests. Pure unit tests cannot mock static Android methods.
    // These tests should be moved to androidTest if needed.

    @Test
    fun isPermissionGranted_allGranted_returnsTrue() {
        // Arrange
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
        )

        // Act
        val result = PermissionUtils.isPermissionGranted(grantResults)

        // Assert
        assert(result == true)
    }

    @Test
    fun isPermissionGranted_oneDenied_returnsFalse() {
        // Arrange
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_GRANTED,
        )

        // Act
        val result = PermissionUtils.isPermissionGranted(grantResults)

        // Assert
        assert(result == false)
    }

    @Test
    fun isPermissionGranted_emptyArray_returnsFalse() {
        // Arrange
        val grantResults = intArrayOf()

        // Act
        val result = PermissionUtils.isPermissionGranted(grantResults)

        // Assert
        assert(result == false)
    }

    @Test
    fun isPermissionGranted_allDenied_returnsFalse() {
        // Arrange
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_DENIED,
        )

        // Act
        val result = PermissionUtils.isPermissionGranted(grantResults)

        // Assert
        assert(result == false)
    }

    @Test
    fun RECORD_AUDIO_PERMISSION_REQUEST_CODE_hasCorrectValue() {
        // Assert
        assert(PermissionUtils.RECORD_AUDIO_PERMISSION_REQUEST_CODE == 1001)
    }
}
