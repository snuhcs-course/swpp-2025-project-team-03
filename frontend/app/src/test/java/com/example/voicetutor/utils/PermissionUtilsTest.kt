package com.example.voicetutor.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PermissionUtilsTest {

    @Mock
    lateinit var context: Context

    @Test
    fun getRequiredPermissions_returnsCorrectPermissions() {
        val permissions = PermissionUtils.getRequiredPermissions()

        assert(permissions.size == 3)
        assert(permissions.contains(Manifest.permission.RECORD_AUDIO))
        assert(permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        assert(permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    @Test
    fun isPermissionGranted_allGranted_returnsTrue() {
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
        )

        val result = PermissionUtils.isPermissionGranted(grantResults)

        assert(result)
    }

    @Test
    fun isPermissionGranted_oneDenied_returnsFalse() {
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_GRANTED,
        )

        val result = PermissionUtils.isPermissionGranted(grantResults)

        assert(!result)
    }

    @Test
    fun isPermissionGranted_emptyArray_returnsFalse() {
        val grantResults = intArrayOf()

        val result = PermissionUtils.isPermissionGranted(grantResults)

        assert(!result)
    }

    @Test
    fun isPermissionGranted_allDenied_returnsFalse() {
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_DENIED,
        )

        val result = PermissionUtils.isPermissionGranted(grantResults)

        assert(!result)
    }

    @Test
    fun recordAudioPermissionRequestCode_hasCorrectValue() {
        assert(PermissionUtils.RECORD_AUDIO_PERMISSION_REQUEST_CODE == 1001)
    }

    @Test
    fun hasAudioPermission_whenGranted_returnsTrue() {
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            val result = PermissionUtils.hasAudioPermission(context)

            assert(result)
        }
    }

    @Test
    fun hasAudioPermission_whenDenied_returnsFalse() {
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                )
            }.thenReturn(PackageManager.PERMISSION_DENIED)

            val result = PermissionUtils.hasAudioPermission(context)

            assert(!result)
        }
    }

    @Test
    fun hasAllPermissions_whenAllGranted_returnsTrue() {
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

            val result = PermissionUtils.hasAllPermissions(context)

            assert(result)
        }
    }

    @Test
    fun hasAllPermissions_whenOneDenied_returnsFalse() {
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

            val result = PermissionUtils.hasAllPermissions(context)

            assert(!result)
        }
    }

    @Test
    fun hasAllPermissions_whenAllDenied_returnsFalse() {
        Mockito.mockStatic(ContextCompat::class.java).use { mockedStatic ->
            mockedStatic.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    Mockito.any(Context::class.java),
                    Mockito.anyString(),
                )
            }.thenReturn(PackageManager.PERMISSION_DENIED)

            val result = PermissionUtils.hasAllPermissions(context)

            assert(!result)
        }
    }
}
