package com.example.scheduo.domain.schedule.controller

import com.example.scheduo.domain.calendar.entity.ParticipationStatus
import com.example.scheduo.domain.calendar.repository.CalendarRepository
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.domain.schedule.repository.CategoryRepository
import com.example.scheduo.domain.schedule.repository.RecurrenceRepository
import com.example.scheduo.domain.schedule.repository.ScheduleRepository
import com.example.scheduo.fixture.*
import com.example.scheduo.global.response.status.ResponseStatus
import com.example.scheduo.util.Request
import com.example.scheduo.util.Response
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GetRangeScheduleTest(
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

    describe("GET /calendars/{calendarId}/schedules/range 요청 시") {
        val minuteFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        fun asMinuteString(node: JsonNode, field: String): String =
            LocalDateTime.parse(node[field].asText()).format(minuteFmt)

        context("정상적인 기간별 일정 조회 요청인 경우") {
            it("200 OK와 해당 기간의 모든 일정을 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
                categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 당일 일정 생성
                val singleDaySchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 11, 12, 0),
                    end = LocalDateTime.of(2025, 4, 11, 13, 0)
                )

                // 기간 일정 생성
                val periodSchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 12, 9, 0),
                    end = LocalDateTime.of(2025, 4, 14, 18, 0)
                )

                scheduleRepository.save(singleDaySchedule)
                scheduleRepository.save(periodSchedule)

                val token = jwtFixture.createValidToken(member.id)
                val response = req.get(
                    "/calendars/${calendar.id}/schedules/range?startDate=2025-04-10&endDate=2025-04-15",
                    token = token
                )

                res.assertSuccess(response)
                val json = objectMapper.readTree(response.contentAsString)
                val schedules = json["data"]["schedules"]

                schedules.size() shouldBe 2
                schedules[0]["title"].asText() shouldBe singleDaySchedule.title
                asMinuteString(schedules[0], "startDateTime") shouldBe singleDaySchedule.start.format(minuteFmt)
                asMinuteString(schedules[0], "endDateTime") shouldBe singleDaySchedule.end.format(minuteFmt)
                schedules[1]["title"].asText() shouldBe periodSchedule.title
                asMinuteString(schedules[1], "startDateTime") shouldBe periodSchedule.start.format(minuteFmt)
                asMinuteString(schedules[1], "endDateTime") shouldBe periodSchedule.end.format(minuteFmt)
            }
        }

        context("기간 일정이 조회 범위와 겹치는 경우") {
            it("200 OK와 겹치는 기간 일정을 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
                categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 조회 범위보다 긴 기간 일정
                val longPeriodSchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 8, 9, 0),
                    end = LocalDateTime.of(2025, 4, 18, 18, 0)
                )

                // 조회 범위와 일부만 겹치는 일정
                val partialOverlapSchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 14, 10, 0),
                    end = LocalDateTime.of(2025, 4, 16, 17, 0)
                )

                scheduleRepository.save(longPeriodSchedule)
                scheduleRepository.save(partialOverlapSchedule)

                val token = jwtFixture.createValidToken(member.id)
                val response = req.get(
                    "/calendars/${calendar.id}/schedules/range?startDate=2025-04-10&endDate=2025-04-15",
                    token = token
                )

                res.assertSuccess(response)
                val json = objectMapper.readTree(response.contentAsString)
                val schedules = json["data"]["schedules"]

                schedules.size() shouldBe 2
                asMinuteString(schedules[0], "startDateTime") shouldBe longPeriodSchedule.start.format(minuteFmt)
                asMinuteString(schedules[0], "endDateTime") shouldBe longPeriodSchedule.end.format(minuteFmt)
                asMinuteString(schedules[1], "startDateTime") shouldBe partialOverlapSchedule.start.format(minuteFmt)
                asMinuteString(schedules[1], "endDateTime") shouldBe partialOverlapSchedule.end.format(minuteFmt)
            }
        }

        context("반복 일정이 있는 경우") {
            it("200 OK와 기간 내 반복 일정들을 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
                categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val weeklyRecurrence = createRecurrence(
                    frequency = "WEEKLY",
                    recurrenceEndDate = "2025-04-30"
                )
                recurrenceRepository.save(weeklyRecurrence)

                // 주간 반복 일정 생성
                val weeklyRecurrenceSchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 7, 14, 0),
                    end = LocalDateTime.of(2025, 4, 7, 15, 0),
                    recurrence = weeklyRecurrence
                )

                // 기간 반복 일정 생성 (2일 짜리가 매주 반복)
                val weeklyPeriodRecurrenceSchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 9, 9, 0),
                    end = LocalDateTime.of(2025, 4, 10, 18, 0),
                    recurrence = weeklyRecurrence
                )

                scheduleRepository.save(weeklyRecurrenceSchedule)
                scheduleRepository.save(weeklyPeriodRecurrenceSchedule)

                val token = jwtFixture.createValidToken(member.id)
                val response = req.get(
                    "/calendars/${calendar.id}/schedules/range?startDate=2025-04-07&endDate=2025-04-20",
                    token = token
                )

                res.assertSuccess(response)
                val json = objectMapper.readTree(response.contentAsString)
                val schedules = json["data"]["schedules"]

                // 4월 7일, 14일 (월요일) + 4월 9-10일, 16-17일 (수-목요일)
                schedules.size() shouldBeGreaterThan 3

                // 첫 번째 일정 검증 (4월 7일 월요일)
                asMinuteString(schedules[0], "startDateTime") shouldBe weeklyRecurrenceSchedule.start.format(minuteFmt)
                asMinuteString(schedules[0], "endDateTime") shouldBe weeklyRecurrenceSchedule.end.format(minuteFmt)
            }
        }

        context("조회 기간에 일정이 없는 경우") {
            it("200 OK와 빈 일정 목록을 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val token = jwtFixture.createValidToken(member.id)
                val response = req.get(
                    "/calendars/${calendar.id}/schedules/range?startDate=2025-04-10&endDate=2025-04-15",
                    token = token
                )

                res.assertSuccess(response)
                val json = objectMapper.readTree(response.contentAsString)
                val schedules = json["data"]["schedules"]

                schedules.size() shouldBe 0
            }
        }

        context("존재하지 않는 캘린더 조회 요청인 경우") {
            it("404 NOT FOUND를 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val token = jwtFixture.createValidToken(member.id)
                val response = req.get(
                    "/calendars/999/schedules/range?startDate=2025-04-10&endDate=2025-04-15",
                    token = token
                )

                res.assertFailure(response, ResponseStatus.CALENDAR_NOT_FOUND)
            }
        }

        context("일정이 시간순으로 정렬되는 경우") {
            it("200 OK와 정렬된 일정 목록을 반환한다") {
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                    member = member,
                    calendar = calendar,
                    participationStatus = ParticipationStatus.ACCEPTED
                )
                categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 늦은 시간 일정
                val lateSchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 11, 15, 0),
                    end = LocalDateTime.of(2025, 4, 11, 16, 0)
                )

                // 이른 시간 일정
                val earlySchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),
                    start = LocalDateTime.of(2025, 4, 11, 9, 0),
                    end = LocalDateTime.of(2025, 4, 11, 10, 0)
                )

                // 다음날 일정
                val nextDaySchedule = createSchedule(
                    member = member,
                    calendar = calendar,
                    category = categoryRepository.findAll().first(),

                    start = LocalDateTime.of(2025, 4, 12, 8, 0),
                    end = LocalDateTime.of(2025, 4, 12, 9, 0)
                )

                scheduleRepository.save(lateSchedule)
                scheduleRepository.save(earlySchedule)
                scheduleRepository.save(nextDaySchedule)

                val token = jwtFixture.createValidToken(member.id)
                val response = req.get(
                    "/calendars/${calendar.id}/schedules/range?startDate=2025-04-11&endDate=2025-04-12",
                    token = token
                )

                res.assertSuccess(response)
                val json = objectMapper.readTree(response.contentAsString)
                val schedules = json["data"]["schedules"]

                schedules.size() shouldBe 3
                // 날짜순, 시간순 정렬 확인
                asMinuteString(schedules[0], "startDateTime") shouldBe earlySchedule.start.format(minuteFmt)
                asMinuteString(schedules[0], "endDateTime") shouldBe earlySchedule.end.format(minuteFmt)
                asMinuteString(schedules[1], "startDateTime") shouldBe lateSchedule.start.format(minuteFmt)
                asMinuteString(schedules[1], "endDateTime") shouldBe lateSchedule.end.format(minuteFmt)
                asMinuteString(schedules[2], "startDateTime") shouldBe nextDaySchedule.start.format(minuteFmt)
                asMinuteString(schedules[2], "endDateTime") shouldBe nextDaySchedule.end.format(minuteFmt)
            }
        }
    }
})