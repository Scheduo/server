package com.example.scheduo.response

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
@SpringBootTest
@AutoConfigureMockMvc
class UniformTFResponse(
        @Autowired private val mockMvc: MockMvc,
        @Autowired private val objectMapper: ObjectMapper
) : DescribeSpec({

    describe("성공/실패 응답 통일 테스트") {

        /**
         * {
         *     "code": 200,
         *     "success": true,
         *     "message": "성공 응답 테스트",
         *     "data": {}
         * }
         */
        context("성공 응답을 반환할 때") {
            it("code=200, success=true, message, data가 포함된 JSON을 반환한다") {
                val response = mockMvc.get("/test/response/success")
                        .andReturn().response

                response.status shouldBe 200

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "성공 응답 테스트"
            }
        }

        /**
         * {
         *     "code": 404,
         *     "status" : "COMMON_0001"
         *     "success": false,
         *     "message": "실패 응답 테스트."
         * }
         */
        context("실패 응답을 반환할 때") {
            it("code, success=false, status, message가 포함된 JSON을 반환한다") {
                val response = mockMvc.get("/test/response/fail")
                        .andReturn().response

                response.status shouldBe 404

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 404
                json["success"].asBoolean() shouldBe false
                json["status"].asText() shouldBe "COMMON_0001"
                json["message"].asText() shouldBe "실패 응답 테스트."
            }
        }
    }
})