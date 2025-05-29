package com.example.scheduo.domain.member.controller

import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.member.repository.NotificationRepository
import com.example.scheduo.fixture.createEditInfoRequest
import com.example.scheduo.fixture.createMember
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MemberControllerTest(
        @Autowired val mockMvc: MockMvc,
        @Autowired val objectMapper: ObjectMapper,
        @Autowired val memberRepository: MemberRepository,
        @Autowired val notificationRepository: NotificationRepository
) : DescribeSpec({
    // TODO: authentication 연동에 따른 테스트 코드 수정 필요 (현재는 id 값에 의존적)
    var testId: Long? = null

    beforeTest {
        // db 데이터 삭제 주의
        notificationRepository.deleteAll()
        memberRepository.deleteAll()

        val savedMember = memberRepository.save(
                createMember(
                        email = "user@example.com",
                        nickname = "홍길동"
                )
        )
        testId = savedMember.id
        memberRepository.save(
                createMember(
                        email = "search@example1.com",
                        nickname = "임꺽정"
                )
        )
        memberRepository.save(
                createMember(
                        email = "search@example2.com",
                        nickname = "장길산"
                )
        )
    }

    describe("GET /members/me 요청 시") {
        context("인증된 사용자가 요청하면") {
            it("200 OK와 프로필 정보가 반환된다") {
                val response = mockMvc.get("/members/me?tempId=$testId")
                        .andReturn().response

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["email"].asText() shouldBe "user@example.com"
                json["data"]["nickname"].asText() shouldBe "홍길동"
            }
        }
    }

    describe("PATCH /members/me 요청 시") {
        context("기존 내 닉네임으로 프로필을 수정하면") {
            val editRequest = createEditInfoRequest("홍길동")

            it("200 OK와 수정된 프로필 정보가 반환된다") {
                val response = mockMvc.patch("/members/me?tempId=$testId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(editRequest)
                }.andReturn().response

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["email"].asText() shouldBe "user@example.com"
                json["data"]["nickname"].asText() shouldBe "홍길동"
            }
        }

        context("unique한 닉네임으로 프로필을 수정하면") {
            val editRequest = createEditInfoRequest("이몽룡")

            it("200 OK와 수정된 프로필 정보가 반환된다") {
                val response = mockMvc.patch("/members/me?tempId=$testId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(editRequest)
                }.andReturn().response

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["email"].asText() shouldBe "user@example.com"
                json["data"]["nickname"].asText() shouldBe "이몽룡"
            }
        }

        context("이미 있는 닉네임으로 프로필을 수정하면") {
            val editRequest = createEditInfoRequest("임꺽정")

            it("409 DUPLICATE_NICKNAME 에러가 반환된다") {
                val response = mockMvc.patch("/members/me?tempId=$testId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(editRequest)
                }.andReturn().response

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 409
                json["success"].asBoolean() shouldBe false
                json["message"].asText() shouldBe "이미 사용중인 닉네임입니다."
            }
        }
    }

    describe("GET /members/search?email=??? 요청 시") {
        val searchQuery = "sear"

        context("존재하는 이메일 중 prefix 검색값으로 검색하면") {
            it("200 OK와 해당 사용자의 리스트가 반환된다") {
                val response = mockMvc.get("/members/search?email=$searchQuery")
                        .andReturn().response

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["users"][0]["email"].asText() shouldBe "search@example1.com"
                json["data"]["users"][0]["nickname"].asText() shouldBe "임꺽정"
                json["data"]["users"][1]["email"].asText() shouldBe "search@example2.com"
                json["data"]["users"][1]["nickname"].asText() shouldBe "장길산"
            }
        }
    }
})