package com.example.voicetutor.data.network

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for ApiConfig
 */
class ApiConfigTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    private lateinit var apiConfig: ApiConfig
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences("api_config", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
    }
    
    @Test
    fun getBaseUrl_whenNoPreference_returnsDefaultProdUrl() {
        `when`(mockSharedPreferences.getString("base_url", ApiConfig.PROD_URL))
            .thenReturn(ApiConfig.PROD_URL)
        
        apiConfig = ApiConfig(mockContext)
        val baseUrl = apiConfig.getBaseUrl()
        
        assertEquals(ApiConfig.PROD_URL, baseUrl)
    }
    
    @Test
    fun getBaseUrl_whenPreferenceSet_returnsStoredUrl() {
        val customUrl = "http://custom.url/api/"
        `when`(mockSharedPreferences.getString("base_url", ApiConfig.PROD_URL))
            .thenReturn(customUrl)
        
        apiConfig = ApiConfig(mockContext)
        val baseUrl = apiConfig.getBaseUrl()
        
        assertEquals(customUrl, baseUrl)
    }
    
    @Test
    fun getBaseUrl_whenPreferenceNull_returnsDefaultProdUrl() {
        `when`(mockSharedPreferences.getString("base_url", ApiConfig.PROD_URL))
            .thenReturn(null)
        
        apiConfig = ApiConfig(mockContext)
        val baseUrl = apiConfig.getBaseUrl()
        
        assertEquals(ApiConfig.PROD_URL, baseUrl)
    }
    
    @Test
    fun constants_haveCorrectValues() {
        assertEquals("http://10.0.2.2:8000/api/", ApiConfig.LOCALHOST_URL)
        assertEquals("http://192.168.35.202:8000/api/", ApiConfig.LOCALHOST_URL_DEVICE)
        assertEquals("http://147.46.78.61:8003/api/", ApiConfig.PROD_URL)
        assertEquals("localhost", ApiConfig.SERVER_TYPE_LOCALHOST)
        assertEquals("prod", ApiConfig.SERVER_TYPE_PROD)
    }
    
    @Test
    fun serverOption_dataClass_createsCorrectInstance() {
        val serverOption = ServerOption(
            name = "Production",
            url = "http://prod.server.com/api/",
            type = "prod"
        )
        
        assertEquals("Production", serverOption.name)
        assertEquals("http://prod.server.com/api/", serverOption.url)
        assertEquals("prod", serverOption.type)
    }
    
    @Test
    fun serverOption_dataClass_equality() {
        val option1 = ServerOption("Test", "http://test.com", "test")
        val option2 = ServerOption("Test", "http://test.com", "test")
        val option3 = ServerOption("Test", "http://other.com", "test")
        
        assertEquals(option1, option2)
        assertNotEquals(option1, option3)
    }
    
    @Test
    fun serverOption_dataClass_copy() {
        val original = ServerOption("Test", "http://test.com", "test")
        val copy = original.copy(url = "http://new.com")
        
        assertEquals("Test", copy.name)
        assertEquals("http://new.com", copy.url)
        assertEquals("test", copy.type)
    }
}

