package com.example.scheduo.domain.member.controller

import com.example.scheduo.domain.member.entity.NotificationType
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.member.repository.NotificationRepository
import com.example.scheduo.fixture.JwtFixture
import com.example.scheduo.fixture.createMember
import com.example.scheduo.fixture.createNotification
import com.example.scheduo.global.response.status.ResponseStatus
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
) : DescribeSpec({
    lateinit var req: Request
    lateinit var res: Response

    beforeTest {
        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)
    }

    afterTest {
        notificationRepository.deleteAll()
        memberRepository.deleteAll()
    }

    describe("GET /notifications") {
        context("CALENDAR_INVITATION 알림이 있는 경우") {
            it("200 OK와 알림 목록이 반환된다") {
                val inviter = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(nickname = "test2"))
                val data = mapOf("calendarId" to 1, "inviterId" to inviter.id)
                notificationRepository.save(
                    createNotification(
                        member = invitee,
                        type = NotificationType.CALENDAR_INVITATION,
                        data = data
                    )
                )

                val validToken = jwtFixture.createValidToken(invitee.id)
                val response = req.get("/notifications", token = validToken)
                res.assertSuccess(response)

                val notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(invitee.id)
                notifications.size shouldBe 1
                notifications[0].message shouldBe NotificationType.CALENDAR_INVITATION.createMessage(data)
            }
        }
        context("CALENDAR_INVITATION_ACCEPT 알림이 있는 경우") {
            it("200 OK와 알림 목록이 반환된다") {
                val inviter = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(nickname = "test2"))
                val data = mapOf("inviteeNickname" to invitee.nickname, "calendarName" to "Test Calendar")
                notificationRepository.save(
                    createNotification(
                        member = inviter,
                        type = NotificationType.CALENDAR_INVITATION_ACCEPTED,
                        data = data
                    )
                )

                val validToken = jwtFixture.createValidToken(inviter.id)
                val response = req.get("/notifications", token = validToken)
                res.assertSuccess(response)

                val notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(inviter.id)
                notifications.size shouldBe 1
                notifications[0].message shouldBe NotificationType.CALENDAR_INVITATION_ACCEPTED.createMessage(data)
            }
        }
    }

    describe("DELETE /notifications/{notificationId}/read") {
        context("알림이 존재하고 읽지 않은 상태인 경우") {
            it("200 OK와 알림이 읽음 상태로 변경된다") {
                val member = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(nickname = "test2"))
                val data = mapOf("inviteeNickname" to invitee.nickname, "calendarName" to "Test Calendar")
                val notification = notificationRepository.save(
                    createNotification(
                        member = member,
                        type = NotificationType.CALENDAR_INVITATION_ACCEPTED,
                        data = data
                    )
                )

                val validToken = jwtFixture.createValidToken(member.id)
                val response = req.delete("/notifications/${notification.id}/read", token = validToken)
                res.assertSuccess(response)

                val notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(member.id)
                notifications.size shouldBe 0
            }
        }
        context("알림이 존재하지 않거나 이미 읽은 경우") {
            it("404 NOT FOUND가 반환된다") {
                val member = memberRepository.save(createMember(nickname = "test1"))
                val validToken = jwtFixture.createValidToken(member.id)
                val response = req.delete("/notifications/999/read", token = validToken)
                res.assertFailure(response, ResponseStatus.NOTIFICATION_NOT_FOUND)
            }
        }
        context("본인이 아닌 사용자의 알림을 읽으려는 경우") {
            it("403 FORBIDDEN이 반환된다") {
                val member1 = memberRepository.save(createMember(nickname = "test1"))
                val member2 = memberRepository.save(createMember(nickname = "test2"))
                val data = mapOf("inviteeNickname" to member2.nickname, "calendarName" to "Test Calendar")
                val notification = notificationRepository.save(
                    createNotification(
                        member = member2,
                        type = NotificationType.CALENDAR_INVITATION_ACCEPTED,
                        data = data
                    )
                )

                val validToken = jwtFixture.createValidToken(member1.id)
                val response = req.delete("/notifications/${notification.id}/read", token = validToken)
                res.assertFailure(response, ResponseStatus.NOTIFICATION_NOT_OWNER)
            }
        }

    }
})
