package com.example.voicetutor.di

import android.content.Context
import com.example.voicetutor.data.network.ApiConfig
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Retrofit

/**
 * Unit tests for NetworkModule providers
 */
class NetworkModuleTest {
    
    @Mock
    private lateinit var mockContext: Context
    
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
        assertTrue(gson is Gson)
        
        // Test that Gson can serialize/deserialize
        val testData = mapOf("key" to "value")
        val json = gson.toJson(testData)
        assertEquals("{\"key\":\"value\"}", json)
    }
    
    @Test
    fun provideLoggingInterceptor_returnsConfiguredInterceptor() {
        val interceptor = networkModule.provideLoggingInterceptor()
        
        assertNotNull(interceptor)
        assertTrue(interceptor is HttpLoggingInterceptor)
        assertEquals(HttpLoggingInterceptor.Level.BODY, interceptor.level)
    }
    
    @Test
    fun provideOkHttpClient_returnsConfiguredClient() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val client = networkModule.provideOkHttpClient(loggingInterceptor)
        
        assertNotNull(client)
        assertTrue(client is OkHttpClient)
        
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
        val client = networkModule.provideOkHttpClient(loggingInterceptor)
        val gson = networkModule.provideGson()
        
        val retrofit = networkModule.provideRetrofit(client, gson, mockApiConfig)
        
        assertNotNull(retrofit)
        assertTrue(retrofit is Retrofit)
        assertEquals("http://test.com/api/", retrofit.baseUrl().toString())
    }
    
    @Test
    fun provideApiService_returnsApiServiceInstance() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val client = networkModule.provideOkHttpClient(loggingInterceptor)
        val gson = networkModule.provideGson()
        val retrofit = networkModule.provideRetrofit(client, gson, mockApiConfig)
        
        val apiService = networkModule.provideApiService(retrofit)
        
        assertNotNull(apiService)
    }
    
    @Test
    fun okHttpClient_hasCorrectInterceptorConfiguration() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val client = networkModule.provideOkHttpClient(loggingInterceptor)
        
        // Verify interceptors count (should have at least the logging interceptor)
        assertTrue(client.interceptors.size >= 1)
        assertTrue(client.interceptors.contains(loggingInterceptor))
    }
    
    @Test
    fun gson_isLenient() {
        val gson = networkModule.provideGson()
        
        // Test that Gson is lenient (can parse malformed JSON)
        val result = try {
            gson.fromJson("{key: 'value'}", Map::class.java) // Single quotes are not standard JSON
            true
        } catch (e: Exception) {
            false
        }
        
        // Lenient Gson should be able to parse this
        assertTrue(result)
    }
    
    @Test
    fun retrofit_usesGsonConverter() {
        val loggingInterceptor = networkModule.provideLoggingInterceptor()
        val client = networkModule.provideOkHttpClient(loggingInterceptor)
        val gson = networkModule.provideGson()
        val retrofit = networkModule.provideRetrofit(client, gson, mockApiConfig)
        
        // Verify converters
        assertTrue(retrofit.converterFactories().size > 1) // At least BuiltInConverters + GsonConverter
    }
}

