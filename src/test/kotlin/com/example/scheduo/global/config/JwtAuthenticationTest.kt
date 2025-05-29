package com.example.scheduo.global.config

import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.fixture.JwtFixture
import com.example.scheduo.fixture.createMember
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class JwtAuthenticationTest(
        @Autowired val mockMvc: MockMvc,
        @Autowired val objectMapper: ObjectMapper,
        @Autowired val memberRepository: MemberRepository,
        @Autowired val jwtFixture: JwtFixture
) : DescribeSpec({

    var testMember: Member? = null

    beforeTest {
        memberRepository.deleteAll()
        testMember = memberRepository.save(createMember(email = "user@example.com", nickname = "홍길동"))
    }

    describe("인증이 필요한 API 요청 시") {
        context("유효한 토큰이 Authorization 헤더에 있을 경우") {
            val validToken = jwtFixture.createValidToken(testMember!!.id!!)

            it("200 OK와 정상 응답이 반환된다") {
                val response = mockMvc.get("/members/me?tempId=${testMember!!.id!!}") {
                    header("Authorization", "Bearer $validToken")
                }
                        .andReturn().response

                response.status shouldBe 200
                val json = objectMapper.readTree(response.contentAsString)
                json["success"].asBoolean() shouldBe true
            }
        }

        context("토큰이 없을 경우") {
            it("401 Unauthorized와 에러 메시지가 반환된다") {
                val response = mockMvc.get("/members/me")
                        .andReturn().response

                response.status shouldBe 401
                val json = objectMapper.readTree(response.contentAsString)
                json["status"].asInt() shouldBe 401
                json["error"].asText() shouldBe "Unauthorized"
                json["message"].asText() shouldBe "토큰이 없습니다."
            }
        }

        context("유효하지 않은 토큰이 Authorization 헤더에 있을 경우") {
            val invalidToken = jwtFixture.createInvalidToken()

            it("401 Unauthorized와 에러 메시지가 반환된다") {
                val response = mockMvc.get("/members/me?tempId=${testMember!!.id!!}") {
                    header("Authorization", "Bearer $invalidToken")
                }
                        .andReturn().response

                response.status shouldBe 401
                val json = objectMapper.readTree(response.contentAsString)
                json["status"].asInt() shouldBe 401
                json["error"].asText() shouldBe "Unauthorized"
                json["message"].asText() shouldBe "토큰이 유효하지 않습니다."
            }
        }
    }
})
