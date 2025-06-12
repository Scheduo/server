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
import io.kotest.matchers.ints.shouldBeGreaterThan
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
                        participationStatus = ParticipationStatus.ACCEPTED
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
                        participationStatus = ParticipationStatus.ACCEPTED
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
                val owner = memberRepository.save(createMember(nickname = "test1"))
                val invitee = memberRepository.save(createMember(nickname = "test2"))
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
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
                        nickname = invitee.nickname,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept", token = validToken)

                res.assertSuccess(response)
                val updatedParticipant = participantRepository.findById(participant.id).get()
                updatedParticipant.status shouldBe ParticipationStatus.ACCEPTED

                await().atMost(2, TimeUnit.SECONDS).untilAsserted {
                    val notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(owner.id)
                    notifications.size shouldBe 1
                    notifications[0].message shouldBe NotificationType.CALENDAR_INVITATION_ACCEPTED.createMessage(
                        mapOf(
                            "inviteeNickname" to invitee.nickname,
                            "calendarName" to calendar.name,
                        )
                    )
                }
            }
        }

        context("초대가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                val validToken = jwtFixture.createValidToken(invitee.id)
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept", token = validToken)

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
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept", token = validToken)

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
                val response =
                    req.post("/calendars/${calendar.id}/invite/accept", token = validToken)

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
                json["data"]["title"].asText() shouldBe "Test Calendar"
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

    describe("PATCH /calendars/{calendarId}") {
        context("정상 캘린더 수정 요청일 경우") {
            it("200 OK를 반환한다") {
                val owner = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        role = Role.OWNER,
                        calendar = calendar,
                        member = owner,
                        participationStatus = ParticipationStatus.ACCEPTED
                    )
                )

                val token = jwtFixture.createValidToken(owner.id)

                val request = mapOf("title" to "Edit Calendar", "nickname" to "Edit Nickname")
                val response = req.patch("/calendars/${calendar.id}", request, token)

                res.assertSuccess(response)
            }
        }

        context("캘린더가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val token = jwtFixture.createValidToken(owner.id)

                val request = mapOf("title" to "Edit Calendar")
                val response = req.patch("/calendars/999", request, token)

                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("캘린더 소유자가 아닌 멤버가 제목을 수정할 경우") {
            it("403 Forbidden을 반환한다") {
                val owner = memberRepository.save(createMember())
                val participant = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.saveAll(
                    listOf(
                        createParticipant(
                            role = Role.OWNER,
                            calendar = calendar,
                            member = owner,
                            participationStatus = ParticipationStatus.ACCEPTED
                        ),
                        createParticipant(
                            calendar = calendar,
                            member = participant,
                            participationStatus = ParticipationStatus.ACCEPTED
                        )
                    )
                )

                val token = jwtFixture.createValidToken(participant.id)

                val request = mapOf("title" to "Edit Calendar")
                val response = req.patch("/calendars/${calendar.id}", request, token)

                res.assertFailure(response, ResponseStatus.MEMBER_NOT_OWNER)
            }
        }

        context("해당 캘린더의 참여자가 아닐경우") {
            it("403 Forbidden을 반환한다") {
                val owner = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())

                val token = jwtFixture.createValidToken(owner.id)

                val request = mapOf("title" to "Test Calendar", "nickname" to "Edit Nickname")
                val response = req.patch("/calendars/${calendar.id}", request, token)

                res.assertFailure(response, ResponseStatus.INVALID_CALENDAR_PARTICIPATION)
            }
        }

        context("참여자의 상태가 ACCEPT가 아닐경우") {
            it("403 Forbidden을 반환한다") {
                val participant = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = participant,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )

                val token = jwtFixture.createValidToken(participant.id)

                val request = mapOf("nickname" to "Edit Nickname")
                val response = req.patch("/calendars/${calendar.id}", request, token)

                res.assertFailure(response, ResponseStatus.MEMBER_NOT_ACCEPT)
            }
        }
    }

    describe("DELETE /calendars/{calendarId}") {
        context("정상 캘린더 삭제 요청일 경우") {
            it("200 OK를 반환한다") {
                val owner = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        role = Role.OWNER,
                        calendar = calendar,
                        member = owner,
                        participationStatus = ParticipationStatus.ACCEPTED
                    )
                )

                val token = jwtFixture.createValidToken(owner.id)

                val response = req.delete("/calendars/${calendar.id}", token)
                res.assertSuccess(response)
            }
        }

        context("캘린더가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val token = jwtFixture.createValidToken(owner.id)

                val response = req.delete("/calendars/999", token)

                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("캘린더 소유자가 아닌 멤버가 캘린더 삭제를 요청할 경우") {
            it("403 Forbidden을 반환한다") {
                val owner = memberRepository.save(createMember())
                val participant = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.saveAll(
                    listOf(
                        createParticipant(
                            role = Role.OWNER,
                            calendar = calendar,
                            member = owner,
                            participationStatus = ParticipationStatus.ACCEPTED
                        ),
                        createParticipant(
                            calendar = calendar,
                            member = participant,
                            participationStatus = ParticipationStatus.ACCEPTED
                        )
                    )
                )

                val token = jwtFixture.createValidToken(participant.id)

                val response = req.delete("/calendars/${calendar.id}", token)
                res.assertFailure(response, ResponseStatus.MEMBER_NOT_OWNER)
            }
        }
    }

    describe("GET /calendars") {
        context("정상 모든 캘린더 조회 요청일 경우") {
            it("200 OK를 반환하고 모든 캘린더 정보를 반환한다.") {
                val member = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = member,
                        participationStatus = ParticipationStatus.ACCEPTED
                    )
                )

                val token = jwtFixture.createValidToken(member.id)

                val response = req.get("/calendars", token)

                res.assertSuccess(response)

                val json = objectMapper.readTree(response.contentAsString)
                json["data"]["calendars"].size() shouldBeGreaterThan 0

                json["data"]["calendars"][0]["calendarId"].asLong() shouldBeGreaterThan 0
                json["data"]["calendars"][0]["title"].asText() shouldBe calendar.name
            }
        }

        context("아직 캘린더 참여 수락을 하지 않았을 경우") {
            it("200 OK를 반환하나, 빈 calendar 리스트를 반환한다.") {
                val member = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar())
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = member,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )

                val token = jwtFixture.createValidToken(member.id)

                val response = req.get("/calendars", token)

                res.assertSuccess(response)

                val json = objectMapper.readTree(response.contentAsString)
                json["data"]["calendars"].size() shouldBe 0
            }
        }

        context("아무 캘린더에도 참여하지 않은 경우") {
            it("200 OK를 반환하나, 빈 calendar 리스트를 반환한다.") {
                val member = memberRepository.save(createMember())

                val token = jwtFixture.createValidToken(member.id)

                val response = req.get("/calendars", token)

                res.assertSuccess(response)

                val json = objectMapper.readTree(response.contentAsString)
                json["data"]["calendars"].size() shouldBe 0
            }
        }
    }

    describe("PATCH /calendars/{calendarId}/participants/{participantId}") {
        context("오너가 참여자 권한을 EDITOR에서 VIEW로 수정하면") {
            it("200 OK를 반환하고 권한이 변경된다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar,
                                role = Role.EDIT,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)
                val request = mapOf("role" to "VIEW")

                val response = req.patch("/calendars/${calendar.id}/participants/${targetParticipant.id}", request, validToken)

                res.assertSuccess(response)
                val updatedParticipant = participantRepository.findById(targetParticipant.id).get()
                updatedParticipant.role shouldBe Role.VIEW
            }
        }

        context("오너가 참여자 권한을 EDITOR에서 OWNER로 수정하면") {
            it("200 OK를 반환하고 기존 오너는 EDITOR로, 대상은 OWNER로 변경된다") {
                val currentOwner = memberRepository.save(createMember(nickname = "currentOwner"))
                val newOwner = memberRepository.save(createMember(email = "newowner@gmail.com", nickname = "newOwner"))
                val calendar = calendarRepository.save(createCalendar())

                val currentOwnerParticipant = participantRepository.save(
                        createParticipant(
                                member = currentOwner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = currentOwner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = newOwner,
                                calendar = calendar,
                                role = Role.EDIT,
                                nickname = newOwner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(currentOwner.id)
                val request = mapOf("role" to "OWNER")

                val response = req.patch("/calendars/${calendar.id}/participants/${targetParticipant.id}", request, validToken)

                res.assertSuccess(response)
                val updatedTargetParticipant = participantRepository.findById(targetParticipant.id).get()
                val updatedCurrentOwner = participantRepository.findById(currentOwnerParticipant.id).get()

                updatedTargetParticipant.role shouldBe Role.OWNER
                updatedCurrentOwner.role shouldBe Role.EDIT
            }
        }

        context("오너가 아닌 사용자가 권한 수정을 시도하면") {
            it("403 Forbidden을 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val editor = memberRepository.save(createMember(email = "editor@gmail.com", nickname = "editor"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                participantRepository.save(
                        createParticipant(
                                member = editor,
                                calendar = calendar,
                                role = Role.EDIT,
                                nickname = editor.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar,
                                role = Role.VIEW,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(editor.id)
                val request = mapOf("role" to "EDIT")

                val response = req.patch("/calendars/${calendar.id}/participants/${targetParticipant.id}", request, validToken)

                println("Response status: ${response.status}")
                println("Response body: ${response.contentAsString}")
                res.assertFailure(response, ResponseStatus.MEMBER_NOT_OWNER)
            }
        }

        context("존재하지 않는 캘린더에 대해 요청하면") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val validToken = jwtFixture.createValidToken(owner.id)
                val request = mapOf("role" to "VIEW")

                val response = req.patch("/calendars/999/participants/1", request, validToken)

                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("캘린더에 존재하지 않는 participant에 대해 권한 수정을 시도하면") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)
                val request = mapOf("role" to "VIEW")

                val response = req.patch("/calendars/${calendar.id}/participants/999", request, validToken)

                res.assertFailure(response, ResponseStatus.PARTICIPANT_NOT_FOUND)
            }
        }

        context("요청자가 해당 캘린더의 참여자가 아닌 경우") {
            it("403 FORBIDDEN을 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val outsider = memberRepository.save(createMember(email = "outsider@gmail.com", nickname = "outsider"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar,
                                role = Role.VIEW,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(outsider.id)
                val request = mapOf("role" to "EDIT")

                val response = req.patch("/calendars/${calendar.id}/participants/${targetParticipant.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVALID_CALENDAR_PARTICIPATION)
            }
        }

        context("대상 참여자가 다른 캘린더의 참여자인 경우") {
            it("403 FORBIDDEN을 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar1 = calendarRepository.save(createCalendar(name = "Calendar 1"))
                val calendar2 = calendarRepository.save(createCalendar(name = "Calendar 2"))

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar1,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val otherCalendarParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar2,
                                role = Role.VIEW,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)
                val request = mapOf("role" to "EDIT")

                val response = req.patch("/calendars/${calendar1.id}/participants/${otherCalendarParticipant.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.INVALID_CALENDAR_PARTICIPATION)
            }
        }

        context("role 필드가 누락된 경우") {
            it("400 Validation Error를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar,
                                role = Role.VIEW,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)
                val request = mapOf<String, Any>() // role 필드 누락

                val response = req.patch("/calendars/${calendar.id}/participants/${targetParticipant.id}", request, validToken)

                res.assertValidationFailure(response, ResponseStatus.VALIDATION_ERROR, "역할은 필수입니다.")
            }
        }

        context("PENDING 상태인 참여자의 권한을 수정하려고 하면") {
            it("400 Bad Request를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val pendingParticipant = memberRepository.save(createMember(email = "pending@gmail.com", nickname = "pending"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = pendingParticipant,
                                calendar = calendar,
                                role = Role.VIEW,
                                nickname = pendingParticipant.nickname,
                                participationStatus = ParticipationStatus.PENDING // PENDING 상태
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)
                val request = mapOf("role" to "EDIT")

                val response = req.patch("/calendars/${calendar.id}/participants/${targetParticipant.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.PARTICIPANT_NOT_ACCEPTED)
            }
        }

        context("DECLINED 상태인 참여자의 권한을 수정하려고 하면") {
            it("400 Bad Request를 반환한다") {
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val declinedParticipant = memberRepository.save(createMember(email = "declined@gmail.com", nickname = "declined"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = declinedParticipant,
                                calendar = calendar,
                                role = Role.VIEW,
                                nickname = declinedParticipant.nickname,
                                participationStatus = ParticipationStatus.DECLINED // DECLINED 상태
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)
                val request = mapOf("role" to "EDIT")

                val response = req.patch("/calendars/${calendar.id}/participants/${targetParticipant.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.PARTICIPANT_NOT_ACCEPTED)
            }
        }
    }

    describe("DELETE /calendars/{calendarId}/participants/{participantId}") {
        context("오너가 일반 참여자를 내보내면") {
            it("200 OK를 반환하고 참여자가 제거된다") {
                // Given
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar,
                                role = Role.EDIT,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)

                // When
                val response = req.delete("/calendars/${calendar.id}/participants/${targetParticipant.id}", validToken)

                // Then
                res.assertSuccess(response)
//                participantRepository.findById(targetParticipant.id) shouldBe Optional.empty()
            }
        }

        context("오너가 아닌 사용자가 참여자를 내보내려고 하면") {
            it("403 Forbidden을 반환한다") {
                // Given
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val editor = memberRepository.save(createMember(email = "editor@gmail.com", nickname = "editor"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                participantRepository.save(
                        createParticipant(
                                member = editor,
                                calendar = calendar,
                                role = Role.EDIT,
                                nickname = editor.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar,
                                role = Role.VIEW,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(editor.id)

                // When
                val response = req.delete("/calendars/${calendar.id}/participants/${targetParticipant.id}", validToken)

                // Then
                res.assertFailure(response, ResponseStatus.MEMBER_NOT_OWNER)
            }
        }

        context("오너가 자신을 내보내려고 하면") {
            it("400 Bad Request를 반환한다") {
                // Given
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val calendar = calendarRepository.save(createCalendar())

                val ownerParticipant = participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)

                // When
                val response = req.delete("/calendars/${calendar.id}/participants/${ownerParticipant.id}", validToken)

                // Then
                res.assertFailure(response, ResponseStatus.CANNOT_REMOVE_OWNER)
            }
        }

        context("존재하지 않는 캘린더에서 참여자를 내보내려고 하면") {
            it("404 Not Found를 반환한다") {
                // Given
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val validToken = jwtFixture.createValidToken(owner.id)

                // When
                val response = req.delete("/calendars/999/participants/1", validToken)

                // Then
                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("존재하지 않는 참여자를 내보내려고 하면") {
            it("403 FORBIDDEN을 반환한다") {
                // Given
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)

                // When
                val response = req.delete("/calendars/${calendar.id}/participants/999", validToken)

                // Then
                res.assertFailure(response, ResponseStatus.INVALID_CALENDAR_PARTICIPATION)
            }
        }

        context("다른 캘린더의 참여자를 내보내려고 하면") {
            it("403 FORBIDDEN을 반환한다") {
                // Given
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar1 = calendarRepository.save(createCalendar(name = "Calendar 1"))
                val calendar2 = calendarRepository.save(createCalendar(name = "Calendar 2"))

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar1,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val otherCalendarParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar2,
                                role = Role.VIEW,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(owner.id)

                // When
                val response = req.delete("/calendars/${calendar1.id}/participants/${otherCalendarParticipant.id}", validToken)

                // Then
                res.assertFailure(response, ResponseStatus.INVALID_CALENDAR_PARTICIPATION)
            }
        }

        context("요청자가 해당 캘린더의 참여자가 아닌 경우") {
            it("403 FORBIDDEN을 반환한다") {
                // Given
                val owner = memberRepository.save(createMember(nickname = "owner"))
                val outsider = memberRepository.save(createMember(email = "outsider@gmail.com", nickname = "outsider"))
                val participant = memberRepository.save(createMember(email = "participant@gmail.com", nickname = "participant"))
                val calendar = calendarRepository.save(createCalendar())

                participantRepository.save(
                        createParticipant(
                                member = owner,
                                calendar = calendar,
                                role = Role.OWNER,
                                nickname = owner.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val targetParticipant = participantRepository.save(
                        createParticipant(
                                member = participant,
                                calendar = calendar,
                                role = Role.VIEW,
                                nickname = participant.nickname,
                                participationStatus = ParticipationStatus.ACCEPTED
                        )
                )

                val validToken = jwtFixture.createValidToken(outsider.id)

                // When
                val response = req.delete("/calendars/${calendar.id}/participants/${targetParticipant.id}", validToken)

                // Then
                res.assertFailure(response, ResponseStatus.INVALID_CALENDAR_PARTICIPATION)
            }
        }
    }
})