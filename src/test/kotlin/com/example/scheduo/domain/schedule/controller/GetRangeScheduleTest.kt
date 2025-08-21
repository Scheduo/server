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
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GetRangeScheduleTest (
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
                        startDate = "2025-04-11",
                        endDate = "2025-04-11",
                        startTime = "12:00",
                        endTime = "13:00"
                )

                // 기간 일정 생성
                val periodSchedule = createSchedule(
                        member = member,
                        calendar = calendar,
                        category = categoryRepository.findAll().first(),
                        startDate = "2025-04-12",
                        endDate = "2025-04-14",
                        startTime = "09:00",
                        endTime = "18:00"
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
                schedules[0]["startDate"].asText() shouldBe "2025-04-11"
                schedules[0]["endDate"].asText() shouldBe "2025-04-11"
                schedules[1]["title"].asText() shouldBe periodSchedule.title
                schedules[1]["startDate"].asText() shouldBe "2025-04-12"
                schedules[1]["endDate"].asText() shouldBe "2025-04-14"
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
                        startDate = "2025-04-08",
                        endDate = "2025-04-18",
                        startTime = "09:00",
                        endTime = "18:00"
                )

                // 조회 범위와 일부만 겹치는 일정
                val partialOverlapSchedule = createSchedule(
                        member = member,
                        calendar = calendar,
                        category = categoryRepository.findAll().first(),
                        startDate = "2025-04-14",
                        endDate = "2025-04-16",
                        startTime = "10:00",
                        endTime = "17:00"
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
                schedules[0]["startDate"].asText() shouldBe "2025-04-08"
                schedules[0]["endDate"].asText() shouldBe "2025-04-18"
                schedules[1]["startDate"].asText() shouldBe "2025-04-14"
                schedules[1]["endDate"].asText() shouldBe "2025-04-16"
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
                        startDate = "2025-04-07",
                        endDate = "2025-04-07",
                        startTime = "14:00",
                        endTime = "15:00",
                        recurrence = weeklyRecurrence
                )

                // 기간 반복 일정 생성 (2일 짜리가 매주 반복)
                val weeklyPeriodRecurrenceSchedule = createSchedule(
                        member = member,
                        calendar = calendar,
                        category = categoryRepository.findAll().first(),
                        startDate = "2025-04-09",
                        endDate = "2025-04-10",
                        startTime = "09:00",
                        endTime = "18:00",
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
                schedules[0]["startDate"].asText() shouldBe "2025-04-07"
                schedules[0]["endDate"].asText() shouldBe "2025-04-07"
                schedules[0]["startTime"].asText() shouldBe "14:00"
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
                        startDate = "2025-04-11",
                        endDate = "2025-04-11",
                        startTime = "15:00",
                        endTime = "16:00"
                )

                // 이른 시간 일정
                val earlySchedule = createSchedule(
                        member = member,
                        calendar = calendar,
                        category = categoryRepository.findAll().first(),
                        startDate = "2025-04-11",
                        endDate = "2025-04-11",
                        startTime = "09:00",
                        endTime = "10:00"
                )

                // 다음날 일정
                val nextDaySchedule = createSchedule(
                        member = member,
                        calendar = calendar,
                        category = categoryRepository.findAll().first(),
                        startDate = "2025-04-12",
                        endDate = "2025-04-12",
                        startTime = "08:00",
                        endTime = "09:00"
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
                schedules[0]["startDate"].asText() shouldBe "2025-04-11"
                schedules[0]["startTime"].asText() shouldBe "09:00"
                schedules[1]["startDate"].asText() shouldBe "2025-04-11"
                schedules[1]["startTime"].asText() shouldBe "15:00"
                schedules[2]["startDate"].asText() shouldBe "2025-04-12"
                schedules[2]["startTime"].asText() shouldBe "08:00"
            }
        }
    }
})