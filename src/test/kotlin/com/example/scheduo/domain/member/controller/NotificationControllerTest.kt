package com.example.scheduo.domain.member.controller

import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.member.repository.NotificationRepository
import com.example.scheduo.fixture.JwtFixture
import com.example.scheduo.fixture.createCalendarInvitationNotification
import com.example.scheduo.fixture.createMember
import com.example.scheduo.fixture.createScheduleNotification
import com.example.scheduo.util.Request
import com.example.scheduo.util.Response
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class NotificationControllerTest(
        @Autowired val mockMvc: MockMvc,
        @Autowired val objectMapper: ObjectMapper,
        @Autowired val notificationRepository: NotificationRepository,
        @Autowired val memberRepository: MemberRepository,
        @Autowired val jwtFixture: JwtFixture
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val notificationRepository: NotificationRepository,
    @Autowired val memberRepository: MemberRepository
) : DescribeSpec({
    var memberId: Long? = null
    lateinit var req: Request
    lateinit var res: Response

    beforeTest {
        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)

        // DB 초기화
        notificationRepository.deleteAll()
        memberRepository.deleteAll()

        val savedMember = memberRepository.save(
            createMember(email = "user@example.com", nickname = "홍길동")
        )
        memberId = savedMember.id

        notificationRepository.save(
            createScheduleNotification(member = savedMember)
        )
        notificationRepository.save(
            createCalendarInvitationNotification(member = savedMember)
        )
        notificationRepository.save(
            createScheduleNotification(
                member = savedMember,
                title = "중요 일정 알림",
                data = mapOf("scheduleId" to 88, "calendarId" to 3)
            )
        )
        notificationRepository.save(
            createCalendarInvitationNotification(
                member = savedMember,
                title = "캘린더 초대 수락됨",
                data = mapOf("calendarId" to 10, "inviterId" to 2)
            )
        )
    }

    afterTest {
        notificationRepository.deleteAll()
        memberRepository.deleteAll()
    }

    describe("GET /notifications 요청 시") {
        context("인증된 사용자가 요청할 때") {
            val validToken = jwtFixture.createValidToken(memberId!!)

            // TODO: 인증 연동 시 memberId 대신 인증 헤더로 테스트 가능하도록 수정
            it("200 OK와 알림 목록이 반환된다") {
                val response = mockMvc.get("/notifications?memberId=$memberId") {
                    header("Authorization", "Bearer $validToken")
                }
                        .andReturn().response
                val response = req.get("/notifications?memberId=$memberId")

                res.assertSuccess(response)

                val json = objectMapper.readTree(response.contentAsString)
                json["data"]["notifications"].size() shouldBe 4
                json["data"]["notifications"][0]["type"].asText() shouldBe "CALENDAR_INVITATION"
                json["data"]["notifications"][0]["title"].asText() shouldBe "캘린더 초대 수락됨"
                json["data"]["notifications"][0]["data"]["calendarId"].asInt() shouldBe 10
                json["data"]["notifications"][0]["data"]["inviterId"].asInt() shouldBe 2
                json["data"]["notifications"][0]["createdAt"].isNull shouldBe false
            }
        }
    }
})
