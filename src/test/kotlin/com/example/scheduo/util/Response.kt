package com.example.scheduo.util

import com.example.scheduo.global.response.status.ResponseStatus
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import org.springframework.mock.web.MockHttpServletResponse

class Response(
    private val objectMapper: ObjectMapper
) {
    fun assertSuccess(
        response: MockHttpServletResponse,
        status: ResponseStatus = ResponseStatus.OK
    ) {
        assertCommon(response, status, expectedSuccess = true)
    }

    fun assertFailure(
        response: MockHttpServletResponse,
        status: ResponseStatus,
    ) {
        assertCommon(response, status, expectedSuccess = false)
    }

    fun assertValidationFailure(
        response: MockHttpServletResponse,
        status: ResponseStatus,
        message: String
    ) {
        val json = objectMapper.readTree(response.contentAsString)
        response.status shouldBe status.httpStatus.value()
        json["code"].asInt() shouldBe status.httpStatus.value()
        json["message"].asText() shouldBe message
        json["success"].asBoolean() shouldBe false
    }

    private fun assertCommon(
        response: MockHttpServletResponse,
        status: ResponseStatus,
        expectedSuccess: Boolean
    ) {
        val json = objectMapper.readTree(response.contentAsString)
        response.status shouldBe status.httpStatus.value()
        json["code"].asInt() shouldBe status.httpStatus.value()
        json["message"].asText() shouldBe status.message
        json["success"].asBoolean() shouldBe expectedSuccess
    }
}