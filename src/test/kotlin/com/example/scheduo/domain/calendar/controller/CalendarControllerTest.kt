package com.example.scheduo.domain.calendar.controller;

import com.example.scheduo.domain.calendar.entity.ParticipationStatus
import com.example.scheduo.domain.calendar.repository.CalendarRepository
import com.example.scheduo.domain.calendar.repository.ParticipantRepository
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.fixture.JwtFixture
import com.example.scheduo.fixture.createCalendar
import com.example.scheduo.fixture.createMember
import com.example.scheduo.fixture.createParticipant
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
@AutoConfigureMockMvc()
@ActiveProfiles("test")
@Transactional()
class CalendarControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val calendarRepository: CalendarRepository,
    @Autowired private val participantRepository: ParticipantRepository,
    @Autowired val jwtFixture: JwtFixture
) : DescribeSpec({
    lateinit var req: Request
    lateinit var res: Response


    beforeTest {
        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)
    }
    val validToken = jwtFixture.createValidToken(1L!!)

    afterTest {
        participantRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()
    }

    describe("POST /calendars/{calendarId}/invite") {
        context("정상 초대 요청일 경우") {
            it("200 OK를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar(member = owner))
                val request = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite?memberId=${owner.id}", request, validToken)

                res.assertSuccess(response)
            }
        }
        context("캘린더가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com"))
                val request = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/999/invite?memberId=${owner.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("초대한 멤버가 캘린더 소유자가 아닌 경우") {
            it("403 Forbidden을 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar(member = owner))
                val request = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite?memberId=100", request, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_NOT_OWNER)
            }
        }

        context("초대할 멤버가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val owner = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar(member = owner))
                val request = mapOf("memberId" to 999)
                val response = req.post("/calendars/${calendar.id}/invite?memberId=${owner.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_NOT_FOUND)
            }
        }

        context("초대할 멤버가 이미 초대된 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar(member = owner))
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.PENDING
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite?memberId=${owner.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_ALREADY_INVITED)
            }
        }

        context("초대할 멤버가 이미 참여 중인 경우") {
            it("409 Conflict를 반환한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar(member = owner))
                participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.ACCEPTED
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite?memberId=${owner.id}", request, validToken)

                res.assertFailure(response, ResponseStatus.MEMBER_ALREADY_PARTICIPANT)
            }
        }

        context("초대할 멤버가 초대를 이미 거부한 경우") {
            it("200 OK를 반환하고 상태를 PENDING으로 변경한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember(email = "test2@gmail.com"))
                val calendar = calendarRepository.save(createCalendar(member = owner))
                val participant = participantRepository.save(
                    createParticipant(
                        calendar = calendar,
                        member = invitee,
                        participationStatus = ParticipationStatus.DECLINED
                    )
                )
                val request = mapOf("memberId" to invitee.id)
                val response = req.post("/calendars/${calendar.id}/invite?memberId=${owner.id}", request, validToken)

                res.assertSuccess(response)
                val updatedParticipant = participantRepository.findById(participant.id).get()
                updatedParticipant.status shouldBe ParticipationStatus.PENDING
            }
        }
    }

    describe("POST /calendars/{calendarId}/invite/accept") {
        context("정상적으로 초대를 수락하는 경우") {
            it("200 OK를 반환하고 참여 상태를 ACCEPTED로 변경한다") {
                val owner = memberRepository.save(createMember())
                val invitee = memberRepository.save(createMember())
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
                val calendar = calendarRepository.save(createCalendar(member = owner))
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
})