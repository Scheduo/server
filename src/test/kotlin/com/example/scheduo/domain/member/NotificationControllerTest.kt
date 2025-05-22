package com.example.scheduo.domain.member

import com.example.scheduo.domain.member.controller.NotificationController
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.entity.Notification
import com.example.scheduo.domain.member.entity.NotificationType
import com.example.scheduo.domain.member.service.NotificationService
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(NotificationController::class)
@Import(NotificationTestConfig::class)
@MockBean(JpaMetamodelMappingContext::class)
class NotificationControllerDescribeSpec(
        @Autowired val mockMvc: MockMvc,
        @Autowired val objectMapper: ObjectMapper,
        @Autowired val notificationService: NotificationService
) : DescribeSpec({
    describe("NotificationController GET /notifications 요청 시") {
        context("인증된 사용자가 요청할 때") {
            val memberId = 1L
            val memberMock = mockk<Member>() // member는 실제로는 memberId로 조회되므로 mock 처리

            val notification1 = Notification(
                    123,
                    memberMock,
                    NotificationType.SCHEDULE_NOTIFICATION,
                    "회의 일정이 곧 시작됩니다",
                    mapOf("scheduleId" to 45, "calendarId" to 3)
            )

            val notification2 = Notification(
                    124,
                    memberMock,
                    NotificationType.CALENDAR_INVITATION,
                    "새로운 캘린더 초대가 도착했습니다",
                    mapOf("calendarId" to 7)
            )

            val notification3 = Notification(
                    125,
                    memberMock,
                    NotificationType.SCHEDULE_NOTIFICATION,
                    "중요 일정 알림",
                    mapOf("scheduleId" to 88, "calendarId" to 3)
            )

            val notification4 = Notification(
                    126,
                    memberMock,
                    NotificationType.CALENDAR_INVITATION,
                    "캘린더 초대 수락됨",
                    mapOf("calendarId" to 10, "inviterId" to 2)
            )

            every { notificationService.findAllByMemberId(memberId) } returns listOf(
                    notification1, notification2, notification3, notification4
            )

            it("반환된 알림에는 type, title, data, createdAt이 포함된다") {
                val response = mockMvc.get("/notifications?memberId=1")
                        .andReturn().response
                println(response.contentAsString)

                val json = objectMapper.readTree(response.contentAsString)
                json["code"].asInt() shouldBe 200
                json["success"].asBoolean() shouldBe true
                json["message"].asText() shouldBe "OK."
                json["data"]["notifications"][0]["type"].asText() shouldBe "SCHEDULE_NOTIFICATION"
                json["data"]["notifications"][0]["title"].asText() shouldBe "회의 일정이 곧 시작됩니다"
                json["data"]["notifications"][0]["data"]["scheduleId"].asInt() shouldBe 45
                json["data"]["notifications"][0]["data"]["calendarId"].asInt() shouldBe 3
                json["data"]["notifications"][0]["createdAt"].isNull
            }
        }

    }
})

@TestConfiguration
class NotificationTestConfig {
    @Bean
    fun notificationService(): NotificationService = mockk(relaxed = true)
}