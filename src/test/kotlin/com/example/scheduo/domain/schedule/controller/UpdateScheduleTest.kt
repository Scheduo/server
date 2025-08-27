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
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UpdateScheduleTest(
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
//            startDate = "2025-08-01",
//            endDate = "2025-08-01",
            start = LocalDateTime.of(2025, 8, 1, 9, 0),
            end = LocalDateTime.of(2025, 8, 1, 10, 0),
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
//            startDate = "2025-08-01",
//            endDate = "2025-08-01",
            start = LocalDateTime.of(2025, 8, 1, 9, 0),
            end = LocalDateTime.of(2025, 8, 1, 10, 0),
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

    describe("PATCH /calendars/{calendarId}/schedules/{scheduleId}") {
        context("schedule이 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val nonExistentScheduleId = 999L
                val body = mapOf(
                    "title" to "수정된 일정",
//                    "startDate" to "2025-08-01",
//                    "endDate" to "2025-08-01",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 1, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 1, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 A",
                    "memo" to "수정된 메모"
                )

                val response =
                    req.patch(
                        "/calendars/${calendar.id}/schedules/$nonExistentScheduleId?date=$date",
                        body = body,
                        token = token
                    )
                res.assertFailure(response, ResponseStatus.SCHEDULE_NOT_FOUND)
            }
        }

        context("calendar가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val nonExistentCalendarId = 999L
                val body = mapOf(
                    "title" to "수정된 일정",
//                    "startDate" to "2025-08-01",
//                    "endDate" to "2025-08-01",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 1, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 1, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 A",
                    "memo" to "수정된 메모"
                )

                val response =
                    req.patch(
                        "/calendars/${nonExistentCalendarId}/schedules/${singleSchedule.id} ?date=$date",
                        body = body,
                        token = token
                    )
                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)

            }
        }

        context("일정이 캘린더에 속하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val anotherCalendar = calendarRepository.save(createCalendar())
                val body = mapOf(
                    "title" to "수정된 일정",
//                    "startDate" to "2025-08-01",
//                    "endDate" to "2025-08-01",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 1, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 1, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 A",
                    "memo" to "수정된 메모"
                )
                val response =
                    req.patch(
                        "/calendars/${anotherCalendar.id}/schedules/${singleSchedule.id}?date=$date",
                        body = body,
                        token = token
                    )
                res.assertFailure(response, ResponseStatus.SCHEDULE_NOT_FOUND)
            }
        }


        context("수정할 권한이 없는 경우") {
            it("403 Forbidden을 반환한다") {
                val memberWithoutPermission = memberRepository.save(createMember(nickname = "no_permission"))
                val participant = createParticipant(
                    member = memberWithoutPermission,
                    calendar = calendar,
                    role = Role.VIEW,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val token = jwtFixture.createValidToken(memberWithoutPermission.id)
                val body = mapOf(
                    "title" to "수정된 일정",
//                    "startDate" to "2025-08-01",
//                    "endDate" to "2025-08-01",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 1, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 1, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 A",
                    "memo" to "수정된 메모",
                    "category" to singleSchedule.category.name
                )
                val response =
                    req.patch(
                        "/calendars/${calendar.id}/schedules/${singleSchedule.id}?date=$date",
                        body = body,
                        token = token
                    )
                res.assertFailure(response, ResponseStatus.PARTICIPANT_PERMISSION_LEAK)
            }
        }

        context("카테고리가 존재하지 않는 경우") {
            it("404 Not Found를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                    "title" to "수정된 일정",
//                    "startDate" to "2025-08-01",
//                    "endDate" to "2025-08-01",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 1, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 1, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 A",
                    "memo" to "수정된 메모",
                    "category" to "존재하지 않는 카테고리"
                )

                val response =
                    req.patch(
                        "/calendars/${calendar.id}/schedules/${singleSchedule.id}?date=$date",
                        body = body,
                        token = token
                    )
                res.assertFailure(response, ResponseStatus.CATEGORY_NOT_FOUND)

            }
        }

        context("단일 일정을 수정하는 경우") {
            it("200 OK와 수정된 일정 정보를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                    "title" to "수정된 단일 일정",
//                    "startDate" to "2025-08-02",
//                    "endDate" to "2025-08-02",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 2, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 2, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 A",
                    "memo" to "수정된 메모",
                    "category" to singleSchedule.category.name,
                    "recurrence" to null,
                )

                val response =
                    req.patch(
                        "/calendars/${calendar.id}/schedules/${singleSchedule.id}?date=$date",
                        body = body,
                        token = token
                    )
                res.assertSuccess(response)
                tx.execute {
                    val updatedSchedule = scheduleRepository.findById(singleSchedule.id)
                    updatedSchedule.ifPresent { schedule ->
                        assert(schedule.title == "수정된 단일 일정")
                        assert(schedule.start.toLocalDate().toString() == "2025-08-02")
                        assert(schedule.end.toLocalDate().toString() == "2025-08-02")
                        assert(schedule.start.toLocalTime().toString() == "10:00")
                        assert(schedule.end.toLocalTime().toString() == "11:00")
                        assert(!schedule.isAllDay)
                        assert(schedule.location == "회의실 A")
                        assert(schedule.memo == "수정된 메모")
                    }
                }
            }
        }

        context("반복 일정 전체를 수정하는 경우") {
            it("200 OK와 수정된 반복 일정 정보를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                    "title" to "수정된 반복 일정",
//                    "startDate" to "2025-08-02",
//                    "endDate" to "2025-08-02",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 2, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 2, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 B",
                    "memo" to "수정된 메모",
                    "category" to recurrenceSchedule.category.name,
                    "recurrence" to mapOf(
                        "frequency" to "WEEKLY",
                        "recurrenceEndDate" to "2025-10-01"
                    ),
                    "scope" to "ALL"
                )
                val response =
                    req.patch(
                        "/calendars/${calendar.id}/schedules/${recurrenceSchedule.id}?date=$date",
                        body = body,
                        token = token
                    )
                res.assertSuccess(response)

                tx.execute {
                    val updatedSchedule = scheduleRepository.findById(recurrenceSchedule.id)
                    updatedSchedule.ifPresent { schedule ->
                        assert(schedule.title == "수정된 반복 일정")
                        assert(schedule.start.toLocalDate().toString() == "2025-08-02")
                        assert(schedule.end.toLocalDate().toString() == "2025-08-02")
                        assert(schedule.start.toLocalTime().toString() == "10:00")
                        assert(schedule.end.toLocalTime().toString() == "11:00")
                        assert(!schedule.isAllDay)
                        assert(schedule.location == "회의실 B")
                        assert(schedule.memo == "수정된 메모")
                        assert(schedule.recurrence.frequency == "WEEKLY")
                        assert(schedule.recurrence.recurrenceEndDate.toString() == "2025-10-01")
                        assert(schedule.recurrence.exceptions.isEmpty())
                    }
                }
            }
        }

        context("반복 일정 중 하나만 수정하는 경우") {
            it("200 OK와 수정된 반복 일정 정보를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                    "title" to "수정된 반복 일정 일부",
//                    "startDate" to "2025-08-02",
//                    "endDate" to "2025-08-02",
//                    "startTime" to "10:00",
//                    "endTime" to "11:00",
                    "startDateTime" to LocalDateTime.of(2025, 8, 2, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 2, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 C",
                    "memo" to "수정된 메모 일부",
                    "category" to recurrenceSchedule.category.name,
                    "recurrence" to mapOf(
                        "frequency" to "WEEKLY",
                        "recurrenceEndDate" to "2025-12-31"
                    ),
                    "scope" to "ONLY_THIS"
                )

                val response =
                    req.patch(
                        "/calendars/${calendar.id}/schedules/${recurrenceSchedule.id}?date=$date",
                        body = body,
                        token = token
                    )

                res.assertSuccess(response)

                tx.execute {
                    val updatedSchedule = scheduleRepository.findById(recurrenceSchedule.id)
                    updatedSchedule.ifPresent {
                        assert(it.recurrence.frequency == recurrenceSchedule.recurrence.frequency)
                        assert(it.recurrence.exceptions.any { it -> it.exceptionDate == LocalDate.parse(date) })
                        assert(it.recurrence.exceptions.size == 1)
                    }
                }
            }
        }

        context("반복 일정에서 이후 일정들을 모두 수정하는 경우") {
            it("200 OK와 수정된 반복 일정 정보를 반환한다") {
                val token = jwtFixture.createValidToken(member.id)
                val body = mapOf(
                    "title" to "수정된 반복 일정 이후",
                    "startDateTime" to LocalDateTime.of(2025, 8, 2, 10, 0),
                    "endDateTime" to LocalDateTime.of(2025, 8, 2, 11, 0),
                    "isAllDay" to false,
                    "location" to "회의실 D",
                    "memo" to "수정된 메모 이후",
                    "category" to recurrenceSchedule.category.name,
                    "recurrence" to mapOf(
                        "frequency" to "WEEKLY",
                        "recurrenceEndDate" to "2025-12-31"
                    ),
                    "scope" to "THIS_AND_FUTURE"
                )

                val response =
                    req.patch(
                        "/calendars/${calendar.id}/schedules/${recurrenceSchedule.id}?date=$date",
                        body = body,
                        token = token
                    )
                res.assertSuccess(response)

                tx.execute {
                    val updatedSchedule = scheduleRepository.findById(recurrenceSchedule.id)
                    updatedSchedule.ifPresent {
                        assert(it.recurrence.recurrenceEndDate == (body["startDateTime"] as LocalDateTime).toLocalDate())
                        assert(it.recurrence.exceptions.isEmpty())
                    }
                }
            }
        }
    }
})