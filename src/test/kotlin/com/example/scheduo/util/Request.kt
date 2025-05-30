package com.example.scheduo.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class Request(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    fun get(
        url: String,
        token: String? = null
    ): MockHttpServletResponse {
        val requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
        if (!token.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer $token")
        return mockMvc.perform(requestBuilder).andReturn().response
    }

    fun post(
        url: String,
        body: Any? = null,
        token: String? = null
    ): MockHttpServletResponse {
        val requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON)
        if (body != null) requestBuilder.content(objectMapper.writeValueAsString(body))
        if (!token.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer $token")
        return mockMvc.perform(requestBuilder).andReturn().response
    }

    fun patch(
        url: String,
        body: Any? = null,
        token: String? = null
    ): MockHttpServletResponse {
        val requestBuilder = MockMvcRequestBuilders.patch(url).contentType(MediaType.APPLICATION_JSON)
        if (body != null) requestBuilder.content(objectMapper.writeValueAsString(body))
        if (!token.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer $token")
        return mockMvc.perform(requestBuilder).andReturn().response
    }

    fun delete(
        url: String,
        token: String? = null
    ): MockHttpServletResponse {
        val requestBuilder = MockMvcRequestBuilders.delete(url).contentType(MediaType.APPLICATION_JSON)
        if (!token.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer $token")
        return mockMvc.perform(requestBuilder).andReturn().response
    }
}