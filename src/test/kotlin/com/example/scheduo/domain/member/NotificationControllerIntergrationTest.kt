package com.example.scheduo.domain.member

import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.Notification
import com.example.scheduo.domain.member.entity.NotificationType
import com.example.scheduo.domain.member.entity.SocialType
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.member.repository.NotificationRepository
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

        // 테스트용 멤버 저장
        val savedMember = memberRepository.save(
                Member(null, "user@example.com", "홍길동", SocialType.GOOGLE)
        )
        memberId = savedMember.id

        notificationRepository.save(Notification(
                null,
                savedMember,
                NotificationType.SCHEDULE_NOTIFICATION,
                "회의 일정이 곧 시작됩니다",
                mapOf("scheduleId" to 45, "calendarId" to 3)
        ))
        notificationRepository.save(Notification(
                null,
                savedMember,
                NotificationType.CALENDAR_INVITATION,
                "새로운 캘린더 초대가 도착했습니다",
                mapOf("calendarId" to 7)
        ))
        notificationRepository.save(Notification(
                null,
                savedMember,
                NotificationType.SCHEDULE_NOTIFICATION,
                "중요 일정 알림",
                mapOf("scheduleId" to 88, "calendarId" to 3)
        ))
        notificationRepository.save(Notification(
                null,
                savedMember,
                NotificationType.CALENDAR_INVITATION,
                "캘린더 초대 수락됨",
                mapOf("calendarId" to 10, "inviterId" to 2)
        ))
    }

    describe("NotificationController GET /notifications 요청 시") {
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
