package com.example.scheduo.domain.schedule.controller

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.calendar.entity.ParticipationStatus
import com.example.scheduo.domain.calendar.entity.Role
import com.example.scheduo.domain.calendar.repository.CalendarRepository
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.schedule.entity.Schedule
import com.example.scheduo.domain.schedule.repository.CategoryRepository
import com.example.scheduo.domain.schedule.repository.RecurrenceRepository
import com.example.scheduo.domain.schedule.repository.ScheduleRepository
import com.example.scheduo.fixture.*
import com.example.scheduo.global.response.status.ResponseStatus
import com.example.scheduo.util.Request
import com.example.scheduo.util.Response
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShareScheduleTest(
        @Autowired val mockMvc: MockMvc,
        @Autowired val objectMapper: ObjectMapper,
        @Autowired val memberRepository: MemberRepository,
        @Autowired val calendarRepository: CalendarRepository,
        @Autowired val categoryRepository: CategoryRepository,
        @Autowired val scheduleRepository: ScheduleRepository,
        @Autowired val recurrenceRepository: RecurrenceRepository,
        @Autowired val jwtFixture: JwtFixture,
        @Autowired val tx: TransactionTemplate,
) : DescribeSpec({

    lateinit var req: Request
    lateinit var res: Response
    lateinit var member: Member
    lateinit var fromCalendar: Calendar
    lateinit var toCalendar: Calendar

    beforeSpec {
        scheduleRepository.deleteAll()
        recurrenceRepository.deleteAll()
        categoryRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()

        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)

        // 멤버
        member = memberRepository.save(createMember(nickname = "share_tester"))

        // 기본 카테고리(default) - 서비스에서 조회하므로 반드시 준비
        categoryRepository.save(createCategory(name = "default"))

        // from 캘린더
        fromCalendar = createCalendar(name = "from")
        val fromParticipant = createParticipant(
                member = member,
                calendar = fromCalendar,
                role = Role.VIEW, // 조회 권한이면 충분 (요구사항: from은 participant면 ok)
                participationStatus = ParticipationStatus.ACCEPTED
        )
        fromCalendar.addParticipant(fromParticipant)
        calendarRepository.save(fromCalendar)

        // to 캘린더
        toCalendar = createCalendar(name = "to")
        val toParticipant = createParticipant(
                member = member,
                calendar = toCalendar,
                role = Role.EDIT, // 요구사항: participant 이면서 edit 이상
                participationStatus = ParticipationStatus.ACCEPTED,
                nickname = "to-nickname" // title로 복사되어야 함
        )
        toCalendar.addParticipant(toParticipant)
        calendarRepository.save(toCalendar)
    }

    afterSpec {
        scheduleRepository.deleteAll()
        recurrenceRepository.deleteAll()
        categoryRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()
    }

    describe("POST /calendars/{calendarId}/schedules/share") {

        context("정상 요청 - 4개의 일정 공유") {
            it("200 OK와 함께 target 캘린더에 4개의 일정이 생성된다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                        "targetCalendarId" to toCalendar.id,
                        "schedules" to listOf(
                                mapOf("startDateTime" to "2025-07-01T10:00:00", "endDateTime" to "2025-07-01T11:00:00"),
                                mapOf("startDateTime" to "2025-07-08T10:00:00", "endDateTime" to "2025-07-08T11:00:00"),
                                mapOf("startDateTime" to "2025-07-15T10:00:00", "endDateTime" to "2025-07-15T11:00:00"),
                                mapOf("startDateTime" to "2025-07-22T10:00:00", "endDateTime" to "2025-07-22T11:00:00"),
                        )
                )

                val response = req.post(
                        "/calendars/${fromCalendar.id}/schedules/share",
                        body = body,
                        token = token
                )
                res.assertSuccess(response)

                tx.execute {
                    val saved = scheduleRepository.findAll()
                            .filter { it.calendar.id == toCalendar.id }
                    assert(saved.size == 4)

                    // 공통 필드 검증
                    saved.forEach { s: Schedule ->
                        // title = target 캘린더의 닉네임
                        assert(s.title == "to-nickname")

                        // category/location/memo = default
                        assert(s.category.name == "default")

                        // recurrence = null
                        assert(s.recurrence == null)

                        // allDay = 서버 계산 (10:00~11:00이므로 false)
                        assert(!s.isAllDay)

                        // 날짜/시간 형식 검증 (서비스가 문자열로 저장하더라도 DB에서 타입 변환되어 들어가있을 수 있으므로 도메인 필드 타입에 맞춰 확인)
                        assert(s.startTime.toString().startsWith("10:00"))
                        assert(s.endTime.toString().startsWith("11:00"))
                    }
                }
            }
        }

        context("권한 부족 - toCalendar 편집 권한 없음") {
            it("403 Forbidden(PARTICIPANT_PERMISSION_LEAK)을 반환한다") {
                // toCalendar에서 권한을 VIEW로 낮추기
                tx.execute {
                    val cal = calendarRepository.findByIdWithParticipants(toCalendar.id!!).get()
                    val participant = cal.participants.first { it.member.id == member.id }
                    participant.updateRole(Role.VIEW) // fixture의 도메인 메서드가 없다면 직접 set
                    calendarRepository.save(cal)
                }

                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                        "targetCalendarId" to toCalendar.id,
                        "schedules" to listOf(
                                mapOf("startDateTime" to "2025-07-01T10:00:00", "endDateTime" to "2025-07-01T11:00:00")
                        )
                )

                val response = req.post(
                        "/calendars/${fromCalendar.id}/schedules/share",
                        body = body,
                        token = token
                )

                res.assertFailure(response, ResponseStatus.PARTICIPANT_PERMISSION_LEAK)
            }
        }

        context("캘린더가 존재하지 않는 경우(fromCalendar)") {
            it("404 Not Found(CALENDAR_NOT_FOUND)를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val nonExistentCalendarId = 999_999L
                val body = mapOf(
                        "targetCalendarId" to toCalendar.id,
                        "schedules" to listOf(
                                mapOf("startDateTime" to "2025-07-01T10:00:00", "endDateTime" to "2025-07-01T11:00:00")
                        )
                )

                val response = req.post(
                        "/calendars/$nonExistentCalendarId/schedules/share",
                        body = body,
                        token = token
                )
                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("target 캘린더가 존재하지 않는 경우(toCalendar)") {
            it("404 Not Found(CALENDAR_NOT_FOUND)를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                        "targetCalendarId" to 999_999L,
                        "schedules" to listOf(
                                mapOf("startDateTime" to "2025-07-01T10:00:00", "endDateTime" to "2025-07-01T11:00:00")
                        )
                )

                val response = req.post(
                        "/calendars/${fromCalendar.id}/schedules/share",
                        body = body,
                        token = token
                )
                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("fromCalendar에 참여자가 아닌 경우") {
            it("403 INVALID_CALENDAR_PARTICIPATION을 반환한다") {
                val memberBelongTo = memberRepository.save(createMember(nickname = "not_participant_1"))
                val toParticipant = createParticipant(
                        member = memberBelongTo,
                        calendar = toCalendar,
                        role = Role.EDIT, // 요구사항: participant 이면서 edit 이상
                        participationStatus = ParticipationStatus.ACCEPTED,
                        nickname = "to-nickname2" // title로 복사되어야 함
                )
                toCalendar.addParticipant(toParticipant)
                calendarRepository.save(toCalendar)

                val token = jwtFixture.createValidToken(memberBelongTo.id)
                val body = mapOf(
                        "targetCalendarId" to toCalendar.id,
                        "schedules" to listOf(
                                mapOf("startDateTime" to "2025-07-01T10:00:00", "endDateTime" to "2025-07-01T11:00:00")
                        )
                )

                val response = req.post(
                        "/calendars/${fromCalendar.id}/schedules/share",
                        body = body,
                        token = token
                )
                res.assertFailure(response, ResponseStatus.PARTICIPANT_NOT_FOUND)
            }
        }

        context("toCalendar에 참여자가 아닌 경우") {
            it("403 INVALID_CALENDAR_PARTICIPATION을 반환한다") {
                val memberBelongFrom = memberRepository.save(createMember(nickname = "not_participant_2"))
                val fromParticipant = createParticipant(
                        member = memberBelongFrom,
                        calendar = fromCalendar,
                        role = Role.VIEW, // 조회 권한이면 충분 (요구사항: from은 participant면 ok)
                        participationStatus = ParticipationStatus.ACCEPTED
                )
                fromCalendar.addParticipant(fromParticipant)
                calendarRepository.save(fromCalendar)

                val token = jwtFixture.createValidToken(memberBelongFrom.id)
                val body = mapOf(
                        "targetCalendarId" to toCalendar.id,
                        "schedules" to listOf(
                                mapOf("startDateTime" to "2025-07-01T10:00:00", "endDateTime" to "2025-07-01T11:00:00")
                        )
                )

                val response = req.post(
                        "/calendars/${fromCalendar.id}/schedules/share",
                        body = body,
                        token = token
                )
                res.assertFailure(response, ResponseStatus.PARTICIPANT_NOT_FOUND)
            }
        }

        context("잘못된 입력 - end < start") {
            it("400 Bad Request(INVALID_SCHEDULE_RANGE)를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                        "targetCalendarId" to toCalendar.id,
                        "schedules" to listOf(
                                mapOf("startDateTime" to "2025-07-02T12:00:00", "endDateTime" to "2025-07-02T11:00:00")
                        )
                )
                val response = req.post(
                        "/calendars/${fromCalendar.id}/schedules/share",
                        body = body,
                        token = token
                )

                res.assertFailure(response, ResponseStatus.INVALID_SCHEDULE_RANGE)
            }
        }
    }
})
