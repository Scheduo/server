package com.example.scheduo.domain.member.controller

import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.Notification
import com.example.scheduo.domain.member.entity.NotificationType
import com.example.scheduo.domain.member.entity.SocialType
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.member.repository.NotificationRepository
import com.example.scheduo.fixture.createCalendarInvitationNotification
import com.example.scheduo.fixture.createMemberGOOGLE
import com.example.scheduo.fixture.createScheduleNotification
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerIntegrationTest(
        @Autowired val mockMvc: MockMvc,
        @Autowired val objectMapper: ObjectMapper,
        @Autowired val notificationRepository: NotificationRepository,
        @Autowired val memberRepository: MemberRepository
) : DescribeSpec({
    var memberId: Long? = null

    beforeTest {
        // DB 초기화
        notificationRepository.deleteAll()
        memberRepository.deleteAll()

        val savedMember = memberRepository.save(
                createMemberGOOGLE(email = "user@example.com", nickname = "홍길동")
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

    describe("GET /notifications 요청 시") {
        context("인증된 사용자가 요청할 때") {
            // TODO: 인증 연동 시 memberId 대신 인증 헤더로 테스트 가능하도록 수정
            it("200 OK와 알림 목록이 반환된다") {
                val response = mockMvc.get("/notifications?memberId=$memberId")
                        .andReturn().response

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["notifications"].size() shouldBe 4
            }

            it("반환된 알림에는 type, title, data, createdAt이 포함된다") {
                val response = mockMvc.get("/notifications?memberId=$memberId")
                        .andReturn().response
                println(response.contentAsString)

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["notifications"][0]["type"].asText() shouldBe "CALENDAR_INVITATION"
                json["data"]["notifications"][0]["title"].asText() shouldBe "캘린더 초대 수락됨"
                json["data"]["notifications"][0]["data"]["calendarId"].asInt() shouldBe 10
                json["data"]["notifications"][0]["data"]["inviterId"].asInt() shouldBe 2
                json["data"]["notifications"][0]["createdAt"].isNull shouldBe false
            }
        }
    }
})
