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
    ) {
        val json = objectMapper.readTree(response.contentAsString)
        response.status shouldBe ResponseStatus.OK.httpStatus.value()
        json["code"].asInt() shouldBe ResponseStatus.OK.httpStatus.value()
        json["message"].asText() shouldBe ResponseStatus.OK.message
        json["success"].asBoolean() shouldBe true
    }

    fun assertFailure(
        response: MockHttpServletResponse,
        status: ResponseStatus
    ) {
        val json = objectMapper.readTree(response.contentAsString)
        response.status shouldBe status.httpStatus.value()
        json["code"].asInt() shouldBe status.httpStatus.value()
        json["message"].asText() shouldBe status.message
        json["success"].asBoolean() shouldBe false
    }

}