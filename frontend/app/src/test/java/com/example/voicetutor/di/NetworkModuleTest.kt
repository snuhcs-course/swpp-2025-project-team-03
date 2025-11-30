package com.example.voicetutor.di

import com.example.voicetutor.data.network.ApiConfig
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for NetworkModule providers
 */
class NetworkModuleTest {

    @Mock
    private lateinit var mockApiConfig: ApiConfig

    private lateinit var networkModule: NetworkModule

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        networkModule = NetworkModule
        `when`(mockApiConfig.getBaseUrl()).thenReturn("http://test.com/api/")
    }

    @Test
    fun provideGson_returnsValidGsonInstance() {
        val gson = networkModule.provideGson()

        assertNotNull(gson)

        // Test that Gson can serialize/deserialize
        val testData = mapOf("key" to "value")
        val json = gson.toJson(testData)
        assertEquals("{\"key\":\"value\"}", json)
    }

    @Test
    fun provideLoggingInterceptor_returnsConfiguredInterceptor() {
        val interceptor = networkModule.provideLoggingInterceptor()

        assertNotNull(interceptor)
        assertEquals(HttpLoggingInterceptor.Level.BODY, interceptor.level)
    }

    @Test
    fun provideOkHttpClient_returnsConfiguredClient() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val cookieJar = networkModule.provideCookieJar()
        val client = networkModule.provideOkHttpClient(loggingInterceptor, cookieJar)

        assertNotNull(client)

        // Verify timeouts are set correctly
        assertEquals(60_000, client.connectTimeoutMillis)
        assertEquals(120_000, client.readTimeoutMillis)
        assertEquals(60_000, client.writeTimeoutMillis)

        // Verify interceptor is added
        assertTrue(client.interceptors.contains(loggingInterceptor))
    }

    @Test
    fun provideRetrofit_returnsConfiguredRetrofitInstance() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val cookieJar = networkModule.provideCookieJar()
        val client = networkModule.provideOkHttpClient(loggingInterceptor, cookieJar)
        val gson = networkModule.provideGson()

        val retrofit = networkModule.provideRetrofit(client, gson, mockApiConfig)

        assertNotNull(retrofit)
        assertEquals("http://test.com/api/", retrofit.baseUrl().toString())
    }

    @Test
    fun provideApiService_returnsApiServiceInstance() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val cookieJar = networkModule.provideCookieJar()
        val client = networkModule.provideOkHttpClient(loggingInterceptor, cookieJar)
        val gson = networkModule.provideGson()
        val retrofit = networkModule.provideRetrofit(client, gson, mockApiConfig)

        val apiService = networkModule.provideApiService(retrofit)

        assertNotNull(apiService)
    }

    @Test
    fun okHttpClient_hasCorrectInterceptorConfiguration() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val cookieJar = networkModule.provideCookieJar()
        val client = networkModule.provideOkHttpClient(loggingInterceptor, cookieJar)

        // Verify interceptors count (should have at least the logging interceptor)
        assertTrue(client.interceptors.isNotEmpty())
        assertTrue(client.interceptors.contains(loggingInterceptor))
    }

    @Test
    fun gson_isLenient() {
        val gson = networkModule.provideGson()

        // Test that Gson is lenient (can parse malformed JSON)
        val result = try {
            gson.fromJson("{key: 'value'}", Map::class.java) // Single quotes are not standard JSON
            true
        } catch (_: Exception) {
            false
        }

        // Lenient Gson should be able to parse this
        assertTrue(result)
    }

    @Test
    fun retrofit_usesGsonConverter() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val cookieJar = networkModule.provideCookieJar()
        val client = networkModule.provideOkHttpClient(loggingInterceptor, cookieJar)
        val gson = networkModule.provideGson()
        val retrofit = networkModule.provideRetrofit(client, gson, mockApiConfig)

        // Verify converters
        assertTrue(retrofit.converterFactories().size > 1) // At least BuiltInConverters + GsonConverter
    }
}
