package com.example.scheduo.domain.calendar.controller;

import com.example.scheduo.domain.calendar.entity.ParticipationStatus
import com.example.scheduo.domain.calendar.entity.Role
import com.example.scheduo.domain.calendar.repository.CalendarRepository
import com.example.scheduo.domain.calendar.repository.ParticipantRepository
import com.example.scheduo.domain.member.entity.NotificationType
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.member.repository.NotificationRepository
import com.example.scheduo.fixture.JwtFixture
import com.example.scheduo.fixture.createCalendar
import com.example.scheduo.fixture.createMember
import com.example.scheduo.fixture.createParticipant
import com.example.scheduo.global.response.status.ResponseStatus
import com.example.scheduo.util.Request
import com.example.scheduo.util.Response
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.awaitility.Awaitility.await
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc()
@ActiveProfiles("test")
@Transactional()
class CalendarControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val calendarRepository: CalendarRepository,
    @Autowired private val participantRepository: ParticipantRepository,
    @Autowired private val notificationRepository: NotificationRepository,
    @Autowired private val jwtFixture: JwtFixture
) : DescribeSpec({
    lateinit var req: Request
    lateinit var res: Response


    beforeTest {
        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)
    }

    afterTest {
        notificationRepository.deleteAll()
        participantRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()
    }

    describe("POST /calendars/{calendarId}/invite") {
        context("정상 초대 요청일 경우") {
            it("200 OK를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com", nickname = "test2"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        member = owner,
                        calendar = calendar,
                        role = Role.OWNER,
                        nickname = owner.nickname,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val validToken = jwtFixture.createValidToken(owner.id)
                val body = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite", body, validToken)

                res.assertSuccess(response)

                await().atMost(1, TimeUnit.SECONDS).untilAsserted {
                    val notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(invitee.id)
                    notifications.size shouldBe 1
                    notifications[0].message shouldBe NotificationType.CALENDAR_INVITATION.createMessage(
                        mapOf(
                            "calendarId" to calendar.id,
                            "calendarName" to calendar.name,
                            "inviterName" to owner.nickname
                        )
                    )
                    notifications[0].data["calendarId"] shouldBe calendar.id
                }
            }
        }

        context("캘린더가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com", nickname = "test2"))
                val validToken = jwtFixture.createValidToken(owner.id)
                val body = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/999/invite", body, validToken)

                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("초대한 멤버가 캘린더 소유자가 아닌 경우") {
            it("403 Forbidden을 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com", nickname = "test2"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        member = owner,
                        calendar = calendar,
                        role = Role.OWNER,
                        nickname = owner.nickname,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val validToken = jwtFixture.createValidToken(invitee.id)
                val body = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite", body, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_NOT_OWNER)
            }
        }

        context("초대할 멤버가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        member = owner,
                        calendar = calendar,
                        role = Role.OWNER,
                        nickname = owner.nickname,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val validToken = jwtFixture.createValidToken(owner.id)
                val body = mapOf("memberId" to 999)
                val response = req.post("/calendars/${calendar.id}/invite?memberId=${owner.id}", body, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_NOT_FOUND)
            }
        }

        context("초대할 멤버가 이미 초대된 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com", nickname = "test2"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        member = owner,
                        calendar = calendar,
                        role = Role.OWNER,
                        nickname = owner.nickname,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val validToken = jwtFixture.createValidToken(owner.id)
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val body = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite", body, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_ALREADY_INVITED)
            }
        }

        context("초대할 멤버가 이미 참여 중인 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com", nickname = "test2"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        member = owner,
                        calendar = calendar,
                        role = Role.OWNER,
                        nickname = owner.nickname,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val validToken = jwtFixture.createValidToken(owner.id)
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.ACCEPTED
                    )
                )
                val body = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite", body, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_ALREADY_PARTICIPANT)
            }
        }

        context("초대할 멤버가 초대를 이미 거부한 경우") {
            it("200 OK를 반환하고 상태를 PENDING으로 변경한다") {
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com", nickname = "test2"))
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(owner.id)
                participantRepository.save(
                    createParticipant(
                        member = owner,
                        calendar = calendar,
                        role = Role.OWNER,
                        nickname = owner.nickname,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val participant = participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.DECLINED
                    )
                )
                val body = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite", body, validToken)

                res.assertSuccess(response)
                val updatedParticipant = participantRepository.findById(participant.id).get()
                updatedParticipant.status shouldBe ParticipationStatus.PENDING

                await().atMost(2, TimeUnit.SECONDS).untilAsserted {
                    val notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(invitee.id)
                    notifications.size shouldBe 1
                    notifications[0].message shouldBe NotificationType.CALENDAR_INVITATION.createMessage(
                        mapOf(
                            "calendarId" to calendar.id,
                            "calendarName" to calendar.name,
                            "inviterName" to owner.nickname
                        )
                    )
                    notifications[0].data["calendarId"] shouldBe calendar.id
                }
            }
        }
    }

    describe("POST /calendars/{calendarId}/invite/accept") {
        context("정상적으로 초대를 수락하는 경우") {
            it("200 OK를 반환하고 참여 상태를 ACCEPTED로 변경한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                val participant = participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept?memberId=${invitee.id}", request, validToken)

                res.assertSuccess(response)
                val updatedParticipant = participantRepository.findById(participant.id).get()
                updatedParticipant.status shouldBe ParticipationStatus.ACCEPTED
            }
        }

        context("초대가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept?memberId=${invitee.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVITATION_NOT_FOUND)
            }
        }

        context("초대가 이미 수락된 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.ACCEPTED
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept?memberId=${invitee.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVITATION_ALREADY_ACCEPTED)
            }
        }

        context("초대가 이미 거부된 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.DECLINED
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept?memberId=${invitee.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVITATION_ALREADY_DECLINED)
            }
        }
    }

    describe("POST /calendars/{calendarId}/invite/decline") {
        context("정상적으로 초대를 거부하는 경우") {
            it("200 OK를 반환하고 참여 상태를 DECLINED로 변경한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                val participant = participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/decline?memberId=${invitee.id}", request, validToken)

                res.assertSuccess(response)
                val updatedParticipant = participantRepository.findById(participant.id).get()
                updatedParticipant.status shouldBe ParticipationStatus.DECLINED
            }
        }
        context("초대가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/decline?memberId=${invitee.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVITATION_NOT_FOUND)
            }
        }
        context("초대가 이미 수락된 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.ACCEPTED
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/decline?memberId=${invitee.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVITATION_ALREADY_ACCEPTED)
            }
        }
        context("초대가 이미 거부된 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.DECLINED
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/decline?memberId=${invitee.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVITATION_ALREADY_DECLINED)
            }
        }
    }

    describe("POST /calendars") {
        context("정상 캘린더 생성 요청일 경우") {
            it("생성된 캘린더 정보와 함께 200 OK를 반환한다") {
                val owner = createMember()
                val members = listOf(
                    owner,
                    createMember(email = "test2@gmail.com"),
                    createMember(email = "test3@gmail.com"),
                )
                val participants = memberRepository.saveAll(members)

                val token = jwtFixture.createValidToken(owner.id)

                val request = mapOf(
                    "title" to "Test Calendar",
                    "participants" to participants.map {
                        mapOf(
                            "memberId" to it.id,
                            "role" to "VIEW"
                        )
                    }
                )
                val response = req.post("/calendars", request, token)

                res.assertSuccess(response)

                val json = objectMapper.readTree(response.contentAsString)
                json["data"]["calendarId"].asLong() shouldBeGreaterThan 0
                json["data"]["calendarTitle"].asText() shouldBe "Test Calendar"
            }
        }

        context("제목이 빈 스트링이거나 null 일경우") {
            it("400 Valid Error를 반환한다") {
                val owner = createMember()
                val members = listOf(
                    owner,
                    createMember(email = "test2@gmail.com"),
                    createMember(email = "test3@gmail.com"),
                )
                val participants = memberRepository.saveAll(members)

                val token = jwtFixture.createValidToken(owner.id)

                val request = mapOf(
                    "title" to "",
                    "participants" to participants.map {
                        mapOf(
                            "memberId" to it.id,
                            "role" to "VIEW"
                        )
                    }
                )
                val response = req.post("/calendars", request, token)

                res.assertValidationFailure(response, ResponseStatus.VALIDATION_ERROR, "캘린더 제목은 필수입니다.")
            }
        }

        context("참가할 memberId가 누락된 경우") {
            it("400 Valid Error를 반환한다") {
                val owner = memberRepository.save(createMember())
                val token = jwtFixture.createValidToken(owner.id)

                val request = mapOf(
                    "title" to "Test Calendar",
                    "participants" to listOf(
                        mapOf(
                            "memberId" to null,
                            "role" to "VIEW"
                        )
                    )
                )
                val response = req.post("/calendars", request, token)

                res.assertValidationFailure(response, ResponseStatus.VALIDATION_ERROR, "memberId는 필수입니다.")
            }
        }

        context("참가할 멤버가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val token = jwtFixture.createValidToken(owner.id)

                val request = mapOf(
                    "title" to "Test Calendar",
                    "participants" to listOf(
                        mapOf(
                            "memberId" to 999,
                            "role" to "VIEW"
                        )
                    )
                )
                val response = req.post("/calendars", request, token)

                res.assertFailure(response, ResponseStatus.MEMBER_NOT_FOUND)
            }
        }
    }
})