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
        val requestBuilder = MockMvcRequestBuilders.get(url)
            .contentType(MediaType.APPLICATION_JSON)
        return execute(requestBuilder, token)
    }

    fun post(
        url: String,
        body: Any? = null,
        token: String? = null
    ): MockHttpServletResponse {
        val requestBuilder = MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body))
        return execute(requestBuilder, token)
    }

    fun patch(
        url: String,
        body: Any? = null,
        token: String? = null
    ): MockHttpServletResponse {
        val requestBuilder = MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body))
        return execute(requestBuilder, token)
    }

    fun delete(
        url: String,
        token: String? = null
    ): MockHttpServletResponse {
        val requestBuilder = MockMvcRequestBuilders.delete(url)
            .contentType(MediaType.APPLICATION_JSON)
        return execute(requestBuilder, token)
    }

    private fun execute(
        requestBuilder: org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder,
        token: String?
    ): MockHttpServletResponse {
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        return mockMvc.perform(requestBuilder).andReturn().response
    }
}