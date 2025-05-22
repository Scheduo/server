package com.example.scheduo.domain.member

import com.example.scheduo.domain.member.controller.MemberController
import com.example.scheduo.domain.member.dto.MemberRequestDto
import com.example.scheduo.domain.member.dto.MemberResponseDto
import com.example.scheduo.domain.member.service.MemberService
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@WebMvcTest(MemberController::class)
@Import(TestConfig::class)
@MockBean(JpaMetamodelMappingContext::class)
class MemberControllerTest(
        @Autowired val mockMvc: MockMvc,
        @Autowired val objectMapper: ObjectMapper,
        @Autowired val memberService: MemberService,
) : DescribeSpec({


    describe("인증된 사용자가") {
        val memberProfile = MemberResponseDto.GetProfile(1L, "user@example.com", "홍길동")

        context("프로필을 조회하면") {
            beforeTest {
                every { memberService.getMyProfile(1L) } returns memberProfile
            }

            it("200 OK와 프로필 정보가 반환된다") {
                val response = mockMvc.get("/members/me?tempId=1")
                        .andReturn().response
                println(response.contentAsString)

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["email"].asText() shouldBe "user@example.com"
                json["data"]["nickname"].asText() shouldBe "홍길동"
            }
        }

        context("프로필을 수정하면") {
            val editRequest = MemberRequestDto.EditInfo("이몽룡")
            val editedProfile = MemberResponseDto.GetProfile(1L, "user@example.com", "이몽룡")

            beforeTest {
                every { memberService.editMyProfile(1L, match { it.nickname == "이몽룡" }) } returns editedProfile
            }

            it("200 OK와 수정된 프로필 정보가 반환된다") {
                val response = mockMvc.patch("/members/me?tempId=1") {
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
    }

    describe("이메일로 사용자를 검색할 때") {
        val searchQuery = "sear"
        val profiles = listOf(
                MemberResponseDto.GetProfile(2L, "search@example1.com", "임꺽정"),
                MemberResponseDto.GetProfile(3L, "search@example2.com", "장길산")
        )
        val foundMembers = MemberResponseDto.SearchProfiles.from(profiles)

        context("존재하는 이메일 중 일부 검색값으로 검색하면") {
            beforeTest {
                every { memberService.searchByEmail(searchQuery) } returns foundMembers
            }

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

@TestConfiguration
class TestConfig {
    @Bean
    fun memberService(): MemberService = mockk(relaxed = true)
}