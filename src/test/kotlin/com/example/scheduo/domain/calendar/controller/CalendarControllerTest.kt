package com.example.scheduo.domain.calendar.controller;

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.calendar.entity.ParticipationStatus
import com.example.scheduo.domain.calendar.repository.CalendarRepository
import com.example.scheduo.domain.calendar.repository.ParticipantRepository
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.fixture.createCalendar
import com.example.scheduo.fixture.createMember
import com.example.scheduo.fixture.createParticipant
import com.example.scheduo.global.response.status.ResponseStatus
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc()
@ActiveProfiles("test")
@Transactional
class CalendarControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val calendarRepository: CalendarRepository,
    @Autowired private val participantRepository: ParticipantRepository
) : DescribeSpec({

    lateinit var owner: Member
    lateinit var invitee: Member
    lateinit var calendar: Calendar

    beforeSpec {
        owner = memberRepository.save(createMember())
        invitee = memberRepository.save(createMember(email = "test2@gmail.com"))
        calendar = calendarRepository.save(createCalendar(member = owner))
    }

    describe("POST /calendars/{calendarId}/invite") {
        context("정상 초대 요청일 경우") {
            it("200 OK를 반환한다") {
                val request = mapOf("memberId" to invitee.id)
                val response = mockMvc.post("/calendars/${calendar.id}/invite?memberId=${owner.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn().response
                response.status shouldBe 200
                val responseBody = objectMapper.readTree(response.contentAsString)
                responseBody["code"].asInt() shouldBe 200
                responseBody["success"].asBoolean() shouldBe true
            }
        }
        context("캘린더가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val request = mapOf("memberId" to invitee.id)
                val response = mockMvc.post("/calendars/999/invite?memberId=${owner.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn().response
                response.status shouldBe 404
                val responseBody = objectMapper.readTree(response.contentAsString)
                responseBody["code"].asInt() shouldBe 404
                responseBody["success"].asBoolean() shouldBe false
                responseBody["message"].asText() shouldBe ResponseStatus.CALENDAR_NOT_FOUND.message
            }
        }

        context("초대한 멤버가 캘린더 소유자가 아닌 경우") {
            it("403 Forbidden을 반환한다") {
                val request = mapOf("memberId" to invitee.id)
                val response = mockMvc.post("/calendars/${calendar.id}/invite?memberId=100") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn().response
                response.status shouldBe 403
                val responseBody = objectMapper.readTree(response.contentAsString)
                responseBody["code"].asInt() shouldBe 403
                responseBody["success"].asBoolean() shouldBe false
                responseBody["message"].asText() shouldBe ResponseStatus.MEMBER_NOT_OWNER.message
            }
        }

        context("초대할 멤버가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val request = mapOf("memberId" to 999)
                val response = mockMvc.post("/calendars/${calendar.id}/invite?memberId=${owner.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn().response
                response.status shouldBe 404
                val responseBody = objectMapper.readTree(response.contentAsString)
                responseBody["code"].asInt() shouldBe 404
                responseBody["success"].asBoolean() shouldBe false
                responseBody["message"].asText() shouldBe ResponseStatus.MEMBER_NOT_FOUND.message
            }
        }

        context("초대할 멤버가 이미 초대된 경우") {
            val alreadyInvited = memberRepository.save(createMember())
            participantRepository.save(
                createParticipant(
                    calendar = calendar,
                    member = alreadyInvited,
                    participationStatus = ParticipationStatus.PENDING
                )
            )
            it("409 Conflict를 반환한다") {
                val request = mapOf("memberId" to alreadyInvited.id)
                val response = mockMvc.post("/calendars/${calendar.id}/invite?memberId=${owner.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn().response
                response.status shouldBe 409
                val responseBody = objectMapper.readTree(response.contentAsString)
                responseBody["code"].asInt() shouldBe 409
                responseBody["success"].asBoolean() shouldBe false
                responseBody["message"].asText() shouldBe ResponseStatus.MEMBER_ALREADY_INVITED.message
            }
        }

        context("초대할 멤버가 이미 참여 중인 경우") {
            val participatingMember = memberRepository.save(createMember())
            participantRepository.save(
                createParticipant(
                    calendar = calendar,
                    member = participatingMember,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
            )
            it("409 Conflict를 반환한다") {
                val request = mapOf("memberId" to participatingMember.id)
                val response = mockMvc.post("/calendars/${calendar.id}/invite?memberId=${owner.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn().response
                response.status shouldBe 409
                val responseBody = objectMapper.readTree(response.contentAsString)
                responseBody["code"].asInt() shouldBe 409
                responseBody["success"].asBoolean() shouldBe false
                responseBody["message"].asText() shouldBe ResponseStatus.MEMBER_ALREADY_PARTICIPANT.message
            }
        }

        context("초대할 멤버가 초대를 이미 거부한 경우") {
            val declinedMember = memberRepository.save(createMember())
            val participant = participantRepository.save(
                createParticipant(
                    calendar = calendar,
                    member = declinedMember,
                    participationStatus = ParticipationStatus.DECLINED
                )
            )
            it("200 OK를 반환하고 상태를 PENDING으로 변경한다") {
                val request = mapOf("memberId" to declinedMember.id)
                val response = mockMvc.post("/calendars/${calendar.id}/invite?memberId=${owner.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andReturn().response
                response.status shouldBe 200
                val responseBody = objectMapper.readTree(response.contentAsString)
                responseBody["code"].asInt() shouldBe 200
                responseBody["success"].asBoolean() shouldBe true

                val updatedParticipant = participantRepository.findById(participant.id).get()
                updatedParticipant.status shouldBe ParticipationStatus.PENDING

            }
        }
    }


})