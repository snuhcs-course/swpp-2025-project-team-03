package com.example.voicetutor.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
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

    @Test
    fun hasAudioPermission_whenGranted_returnsTrue() {
        // Arrange
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            // Act
            val result = PermissionUtils.hasAudioPermission(context)

            // Assert
            assert(result == true)
        }
    }

    @Test
    fun hasAudioPermission_whenDenied_returnsFalse() {
        // Arrange
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                )
            }.thenReturn(PackageManager.PERMISSION_DENIED)

            // Act
            val result = PermissionUtils.hasAudioPermission(context)

            // Assert
            assert(result == false)
        }
    }

    @Test
    fun hasAllPermissions_whenAllGranted_returnsTrue() {
        // Arrange
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            // Act
            val result = PermissionUtils.hasAllPermissions(context)

            // Assert
            assert(result == true)
        }
    }

    @Test
    fun hasAllPermissions_whenOneDenied_returnsFalse() {
        // Arrange
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }.thenReturn(PackageManager.PERMISSION_DENIED)

            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            // Act
            val result = PermissionUtils.hasAllPermissions(context)

            // Assert
            assert(result == false)
        }
    }

    @Test
    fun hasAllPermissions_whenAllDenied_returnsFalse() {
        // Arrange
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    Mockito.any(Context::class.java),
                    Mockito.anyString(),
                )
            }.thenReturn(PackageManager.PERMISSION_DENIED)

            // Act
            val result = PermissionUtils.hasAllPermissions(context)

            // Assert
            assert(result == false)
        }
    }
}
