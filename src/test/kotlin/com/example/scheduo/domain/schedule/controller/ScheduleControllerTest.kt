package com.example.scheduo.domain.schedule.controller

import com.example.scheduo.domain.calendar.repository.CalendarRepository
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.schedule.entity.Recurrence
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val memberRepository: MemberRepository,
    @Autowired val calendarRepository: CalendarRepository,
    @Autowired val categoryRepository: CategoryRepository,
    @Autowired val scheduleRepository: ScheduleRepository,
    @Autowired val recurrenceRepository: RecurrenceRepository,
    @Autowired val jwtFixture: JwtFixture,
) : DescribeSpec({
    lateinit var req: Request
    lateinit var res: Response

    beforeContainer {
        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)
    }

    afterTest {
        scheduleRepository.deleteAll()
        recurrenceRepository.deleteAll()
        categoryRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()
    }

    describe("POST /calendars/{calendarId}/schedules") {
        context("단일 일정 생성 요청인 경우") {
            it("200 OK를 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar
                )
                categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val token = jwtFixture.createValidToken(member.id)

                val scheduleData = mapOf(
                    "title" to "제목",
                    "allDay" to true,
                    "startDate" to "2025-05-20",
                    "endDate" to "2025-05-20",
                    "startTime" to "10:00", // hh:mm
                    "endTime" to "11:00",
                    "location" to "회의실 A",
                    "category" to "개인",
                    "memo" to "주간 업무 회의",
                    "notificationTime" to "THIRTY_MINUTES_BEFORE",
                    "recurrence" to null
                )

                val response = req.post("/calendars/${calendar.id}/schedules", scheduleData, token = token)

                res.assertSuccess(response)
            }
        }
        context("반복 일정 생성 요청인 경우") {
            it("Recurrence Rule이 생성되고 200 OK를 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar
                )
                categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val token = jwtFixture.createValidToken(member.id)

                val scheduleData = mapOf(
                    "title" to "제목",
                    "allDay" to true,
                    "startDate" to "2025-05-20", // yyyy-mm-dd
                    "endDate" to "2025-05-20",
                    "startTime" to "10:00", // hh:mm
                    "endTime" to "11:00",
                    "location" to "회의실 A",
                    "category" to "개인",
                    "memo" to "주간 업무 회의",
                    "notificationTime" to "THIRTY_MINUTES_BEFORE",
                    "recurrence" to mapOf(
                        "frequency" to "WEEKLY",
                        "recurrenceEndDate" to "2025-06-20",
                    )
                )

                val response = req.post("/calendars/${calendar.id}/schedules", scheduleData, token = token)

                val recurrenceMap = scheduleData["recurrence"] as Map<String, String>
                val expectedRule = Recurrence.create(
                    recurrenceMap["frequency"]!!,
                    recurrenceMap["recurrenceEndDate"]!!
                )
                res.assertSuccess(response)

                val recurrence = recurrenceRepository.findAll().firstOrNull()

                recurrence?.recurrenceRule shouldBe expectedRule.recurrenceRule

            }
        }
        context("캘린더가 존재하지 않는 경우") {
            it("404 NOT FOUND를 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val token = jwtFixture.createValidToken(member.id)

                val scheduleData = mapOf(
                    "title" to "제목",
                    "allDay" to true,
                    "startDate" to "2025-05-20", // yyyy-mm-dd
                    "endDate" to "2025-05-20",
                    "startTime" to "10:00", // hh:mm
                    "endTime" to "11:00",
                    "location" to "회의실 A",
                    "category" to "개인",
                    "memo" to "주간 업무 회의",
                    "notificationTime" to "THIRTY_MINUTES_BEFORE",
                    "recurrence" to null // 반복 설정이 없는 경우
                )

                val response = req.post("/calendars/999/schedules", scheduleData, token = token)
                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("카테고리가 존재하지 않는 경우") {
            it("404 NOT FOUND를 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar
                )
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val token = jwtFixture.createValidToken(member.id)

                val scheduleData = mapOf(
                    "title" to "제목",
                    "allDay" to true,
                    "startDate" to "2025-05-20", // yyyy-mm-dd
                    "endDate" to "2025-05-20",
                    "startTime" to "10:00", // hh:mm
                    "endTime" to "11:00",
                    "location" to "회의실 A",
                    "category" to "존재하지 않는 카테고리",
                    "memo" to "주간 업무 회의",
                    "notificationTime" to "THIRTY_MINUTES_BEFORE",
                    "recurrence" to null // 반복 설정이 없는 경우
                )

                val response = req.post("/calendars/${calendar.id}/schedules", scheduleData, token = token)
                res.assertFailure(response, ResponseStatus.CATEGORY_NOT_FOUND)
            }
        }
    }
})