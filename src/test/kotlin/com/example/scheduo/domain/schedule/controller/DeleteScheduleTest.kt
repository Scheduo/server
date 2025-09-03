package com.example.scheduo.domain.schedule.controller

import com.example.scheduo.domain.calendar.entity.Calendar
import com.example.scheduo.domain.calendar.entity.ParticipationStatus
import com.example.scheduo.domain.calendar.entity.Role
import com.example.scheduo.domain.calendar.repository.CalendarRepository
import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto
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
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeleteScheduleTest(
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
    lateinit var calendar: Calendar
    lateinit var singleSchedule: Schedule
    lateinit var recurrenceSchedule: Schedule

    val date = "2025-08-12"

    beforeSpec {
        scheduleRepository.deleteAll()
        recurrenceRepository.deleteAll()
        categoryRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()

        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)

        member = memberRepository.save(createMember(nickname = "test"))
        calendar = createCalendar()
        val participant = createParticipant(
            member = member,
            calendar = calendar,
            role = Role.EDIT,
            participationStatus = ParticipationStatus.ACCEPTED
        )
        calendar.addParticipant(participant)
        calendarRepository.save(calendar)
    }


    beforeTest {
        singleSchedule = createSchedule(
            title = "단일 일정",
            startDate = "2025-08-01",
            endDate = "2025-08-01",
            member = member,
            calendar = calendar,
            category = categoryRepository.save(createCategory())
        )

        val recurrence = createRecurrence(
            frequency = "WEEKLY",
            recurrenceEndDate = "2025-12-31"
        )

        recurrenceRepository.save(recurrence)

        recurrenceSchedule = createSchedule(
            title = "반복 일정",
            startDate = "2025-08-01",
            endDate = "2025-08-01",
            member = member,
            calendar = calendar,
            category = categoryRepository.save(createCategory()),
            recurrence = recurrence
        )

        scheduleRepository.saveAll(listOf(singleSchedule, recurrenceSchedule))
    }

    afterTest {
        scheduleRepository.deleteAll()
        recurrenceRepository.deleteAll()
    }

    afterSpec {
        categoryRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()
    }

    describe("DELETE /calendars/{calendarId}/schedules/{scheduleId}") {
        context("캘린더가 존재하지 않는 경우") {
            it("404 Not Found 응답을 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val nonExistentCalendarId = 999L
                val response =
                    req.delete("/calendars/$nonExistentCalendarId/schedules/${singleSchedule.id}", token = token)
                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }
        context("일정 수정 권한이 없는 경우") {
            it("403 Forbidden 응답을 반환한다") {
                val anotherMember = memberRepository.save(createMember(nickname = "another"))
                val anotherParticipant = createParticipant(
                    member = anotherMember,
                    calendar = calendar,
                    role = Role.VIEW,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
                calendar.addParticipant(anotherParticipant)
                calendarRepository.save(calendar)
                val token = jwtFixture.createValidToken(anotherMember.id)
                val response =
                    req.delete("/calendars/${calendar.id}/schedules/${singleSchedule.id}", token = token)

                res.assertFailure(response, ResponseStatus.PARTICIPANT_PERMISSION_LEAK)
            }
        }

        context("일정이 존재하지 않는 경우") {
            it("404 Not Found 응답을 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val nonExistentScheduleId = 999L
                val response =
                    req.delete("/calendars/${calendar.id}/schedules/$nonExistentScheduleId", token = token)
                res.assertFailure(response, ResponseStatus.SCHEDULE_NOT_FOUND)
            }
        }

        context("일정이 캘린더에 속하지 않는 경우") {
            it("404 Not Found 응답을 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val anotherCalendar = calendarRepository.save(createCalendar())
                val anotherSchedule = createSchedule(
                    title = "다른 캘린더 일정",
                    startDate = "2025-08-01",
                    endDate = "2025-08-01",
                    member = member,
                    calendar = anotherCalendar,
                    category = categoryRepository.save(createCategory())
                )
                scheduleRepository.save(anotherSchedule)
                val response =
                    req.delete("/calendars/${calendar.id}/schedules/${anotherSchedule.id}", token = token)
                res.assertFailure(response, ResponseStatus.SCHEDULE_NOT_FOUND)

            }
        }

        context("단일 일정을 삭제하는 경우") {
            it("200 OK 응답을 반환하고, 일정이 삭제된다") {
                val token = jwtFixture.createValidToken(member.id)
                val scope = ScheduleRequestDto.Scope.ALL.name
                val date = "2025-08-01"
                val response = req.delete(
                    "/calendars/${calendar.id}/schedules/${singleSchedule.id}?scope=$scope&date=$date",
                    token = token
                )
                res.assertSuccess(response)

                tx.execute {
                    val deletedSchedule = scheduleRepository.existsById(singleSchedule.id)
                    deletedSchedule shouldBe false
                }
            }
        }

        context("반복 일정의 모든 날짜를 삭제하는 경우") {
            it("200 OK 응답을 반환하고, 반복 일정이 삭제된다") {
                val token = jwtFixture.createValidToken(member.id)
                val scope = ScheduleRequestDto.Scope.ALL.name
                val response = req.delete(
                    "/calendars/${calendar.id}/schedules/${recurrenceSchedule.id}?scope=$scope",
                    token = token
                )
                res.assertSuccess(response)

                tx.execute {
                    val deletedSchedule = scheduleRepository.existsById(recurrenceSchedule.id)
                    val deletedRecurrence = recurrenceRepository.existsById(recurrenceSchedule.recurrence!!.id)
                    deletedSchedule shouldBe false
                    deletedRecurrence shouldBe false
                }
            }
        }

        context("반복 일정의 특정 날짜를 삭제하는 경우") {
            it("200 OK 응답을 반환하고, 해당 날짜가 삭제된다") {
                val token = jwtFixture.createValidToken(member.id)
                val scope = ScheduleRequestDto.Scope.ONLY_THIS.name
                val date = "2025-08-12"
                val response = req.delete(
                    "/calendars/${calendar.id}/schedules/${recurrenceSchedule.id}?scope=$scope&date=$date",
                    token = token
                )
                res.assertSuccess(response)

                tx.execute {
                    val deletedSchedule = scheduleRepository.findById(recurrenceSchedule.id)
                    deletedSchedule.isPresent shouldBe true
                    deletedSchedule.ifPresent {
                        it.recurrence.exceptions.any { it -> it.exceptionDate == LocalDate.parse(date) } shouldBe true
                        it.recurrence.exceptions.size shouldBe 1
                    }
                }
            }
        }

        context("반복 일정의 특정 날짜 이후 일정들을 모두 삭제하는 경우") {
            it("200 OK 응답을 반환하고, 해당 날짜 이후 일정들이 삭제된다") {
                val token = jwtFixture.createValidToken(member.id)
                val scope = ScheduleRequestDto.Scope.THIS_AND_FUTURE.name
                val date = "2025-08-12"
                val response = req.delete(
                    "/calendars/${calendar.id}/schedules/${recurrenceSchedule.id}?scope=$scope&date=$date",
                    token = token
                )
                res.assertSuccess(response)

                tx.execute {
                    val deletedSchedule = scheduleRepository.findById(recurrenceSchedule.id)
                    deletedSchedule.isPresent shouldBe true
                    deletedSchedule.ifPresent {
                        it.recurrence.exceptions.size shouldBe 0
                        it.recurrence.recurrenceEndDate shouldBe LocalDate.parse(date)
                    }
                }
            }
        }
    }
})