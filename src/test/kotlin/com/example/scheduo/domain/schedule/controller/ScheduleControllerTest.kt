package com.example.scheduo.domain.schedule.controller

import com.example.scheduo.domain.calendar.entity.ParticipationStatus
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

    describe("GET /calendars/{calendarId}/schedules/monthly 요청 시") {
        context("단일 일정이 있는 경우") {
            it("해당 월의 단일 일정만 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(member = member, calendar = calendar, participationStatus = ParticipationStatus.ACCEPTED )
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 7월 일정 2개
                val julySchedule1 = createSchedule(
                        title = "7월 회의1",
                        startDate = "2025-07-10",
                        endDate = "2025-07-10",
                        member = member,
                        calendar = calendar,
                        category = category
                )
                val julySchedule2 = createSchedule(
                        title = "7월 회의2",
                        startDate = "2025-07-25",
                        endDate = "2025-07-25",
                        member = member,
                        calendar = calendar,
                        category = category
                )
                // 6월 일정 (포함되면 안됨)
                val juneSchedule = createSchedule(
                        title = "6월 회의",
                        startDate = "2025-06-15",
                        endDate = "2025-06-15",
                        member = member,
                        calendar = calendar,
                        category = category
                )

                scheduleRepository.save(julySchedule1)
                scheduleRepository.save(julySchedule2)
                scheduleRepository.save(juneSchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules/monthly?date=2025-07-01", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 2
            }
        }

        context("매일 반복 일정이 있는 경우") {
            it("해당 월의 반복 일정 인스턴스들을 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(member = member, calendar = calendar, participationStatus = ParticipationStatus.ACCEPTED)
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 매일 반복 일정 (7월 1일부터 7월 10일까지)
                val dailyRecurrence = createRecurrence(
                        frequency = "DAILY",
                        recurrenceEndDate = "2025-07-10"
                )
                val savedRecurrence = recurrenceRepository.save(dailyRecurrence)

                val dailySchedule = createSchedule(
                        title = "매일 운동",
                        startDate = "2025-07-01",
                        endDate = "2025-07-01",
                        member = member,
                        calendar = calendar,
                        category = category,
                        recurrence = savedRecurrence
                )
                scheduleRepository.save(dailySchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules/monthly?date=2025-07-01", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 10 // 7월 1일부터 10일까지 매일 = 10개
            }
        }

        context("매주 반복 일정이 있는 경우") {
            it("해당 월의 주별 반복 일정 인스턴스들을 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(member = member, calendar = calendar, participationStatus = ParticipationStatus.ACCEPTED)
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 매주 반복 일정 (7월 첫째 주 월요일부터 7월 말까지)
                val weeklyRecurrence = createRecurrence(
                        frequency = "WEEKLY",
                        recurrenceEndDate = "2025-07-31"
                )
                recurrenceRepository.save(weeklyRecurrence)

                val weeklySchedule = createSchedule(
                        title = "주간 회의",
                        startDate = "2025-07-07", // 7월 첫째 주 월요일
                        endDate = "2025-07-07",
                        member = member,
                        calendar = calendar,
                        category = category,
                        recurrence = weeklyRecurrence
                )
                scheduleRepository.save(weeklySchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules/monthly?date=2025-07-01", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 4 // 7월 내 월요일 4번 (7, 14, 21, 28일)
            }
        }

        context("매달 반복 일정이 있는 경우") {
            it("해당 월의 월별 반복 일정 인스턴스를 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(member = member, calendar = calendar, participationStatus = ParticipationStatus.ACCEPTED)
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 매달 반복 일정 (4월부터 12월까지)
                val monthlyRecurrence = createRecurrence(
                        frequency = "MONTHLY",
                        recurrenceEndDate = "2025-12-31"
                )
                recurrenceRepository.save(monthlyRecurrence)

                val monthlySchedule = createSchedule(
                        title = "월간 정기 회의",
                        startDate = "2025-04-15", // 4월 15일 시작
                        endDate = "2025-04-15",
                        member = member,
                        calendar = calendar,
                        category = category,
                        recurrence = monthlyRecurrence
                )
                scheduleRepository.save(monthlySchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules/monthly?date=2025-07-01", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 1 // 7월 15일 1개
            }
        }

        context("매년 반복 일정이 있는 경우") {
            it("해당 월의 연별 반복 일정 인스턴스를 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(member = member, calendar = calendar, participationStatus = ParticipationStatus.ACCEPTED)
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 매년 반복 일정 (2025년부터 2027년까지)
                val yearlyRecurrence = createRecurrence(
                        frequency = "YEARLY",
                        recurrenceEndDate = "2027-12-31"
                )
                recurrenceRepository.save(yearlyRecurrence)

                val yearlySchedule = createSchedule(
                        title = "생일 축하",
                        startDate = "2025-07-20", // 7월 20일 생일
                        endDate = "2025-07-20",
                        member = member,
                        calendar = calendar,
                        category = category,
                        recurrence = yearlyRecurrence
                )
                scheduleRepository.save(yearlySchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules/monthly?date=2025-07-01", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)
                println("RESULT!!!! => " + responseBody.toString())

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 1 // 2025년 7월 20일 1개

            }
        }
    }

    describe("GET /calendars/{calendarId}/schedules 요청 시") {
        context("해당 날짜에 단일 일정만 있는 경우") {
            it("해당 날짜의 단일 일정들을 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                        member = member,
                        calendar = calendar,
                        participationStatus = ParticipationStatus.ACCEPTED
                )
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 2025-07-15 당일 일정들
                val morningSchedule = createSchedule(
                        title = "아침 회의",
                        startDate = "2025-07-15",
                        endDate = "2025-07-15",
                        startTime = "09:00",
                        endTime = "10:00",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category
                )
                val afternoonSchedule = createSchedule(
                        title = "오후 미팅",
                        startDate = "2025-07-15",
                        endDate = "2025-07-15",
                        startTime = "14:00",
                        endTime = "15:00",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category
                )
                // 다른 날짜 일정 (포함되면 안됨)
                val otherDaySchedule = createSchedule(
                        title = "다른 날 일정",
                        startDate = "2025-07-16",
                        endDate = "2025-07-16",
                        member = member,
                        calendar = calendar,
                        category = category
                )

                scheduleRepository.save(morningSchedule)
                scheduleRepository.save(afternoonSchedule)
                scheduleRepository.save(otherDaySchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules?date=2025-07-15", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 2

                val scheduleDetails = schedulesNode.map { it.path("title").asText() to it.path("startTime").asText() }
                scheduleDetails.contains("아침 회의" to "09:00:00") shouldBe true
                scheduleDetails.contains("오후 미팅" to "14:00:00") shouldBe true
            }
        }

        context("해당 날짜를 포함하는 기간 일정이 있는 경우") {
            it("기간 일정도 함께 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                        member = member,
                        calendar = calendar,
                        participationStatus = ParticipationStatus.ACCEPTED
                )
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                // 2025-07-15를 포함하는 기간 일정
                val periodSchedule = createSchedule(
                        title = "3일간 워크샵",
                        startDate = "2025-07-14",
                        endDate = "2025-07-16",
                        isAllDay = true,
                        member = member,
                        calendar = calendar,
                        category = category
                )
                // 당일 일정
                val daySchedule = createSchedule(
                        title = "당일 회의",
                        startDate = "2025-07-15",
                        endDate = "2025-07-15",
                        startTime = "14:00",
                        endTime = "15:00",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category
                )

                scheduleRepository.save(periodSchedule)
                scheduleRepository.save(daySchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules?date=2025-07-15", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 2

                // 기간 일정과 당일 일정 모두 포함
                val titles = (0 until schedulesNode.size()).map {
                    schedulesNode[it].path("title").asText()
                }
                titles.contains("3일간 워크샵") shouldBe true
                titles.contains("당일 회의") shouldBe true
            }
        }

        context("해당 날짜에 반복 일정이 있는 경우") {
            it("반복 일정의 해당 날짜 인스턴스를 반환한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                        member = member,
                        calendar = calendar,
                        participationStatus = ParticipationStatus.ACCEPTED
                )
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val weeklyRecurrence = createRecurrence(
                        frequency = "WEEKLY",
                        recurrenceEndDate = "2025-12-31"
                )
                recurrenceRepository.save(weeklyRecurrence)

                // 매주 월요일 반복 일정
                val recurringSchedule = createSchedule(
                        title = "주간 회의",
                        startDate = "2025-07-14", // 월요일 시작
                        endDate = "2025-07-14",
                        startTime = "10:00",
                        endTime = "11:00",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category,
                        recurrence = weeklyRecurrence
                )
                scheduleRepository.save(recurringSchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When - 2025-07-21 (월요일) 조회
                val response = req.get("/calendars/${calendar.id}/schedules?date=2025-07-21", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 1
                schedulesNode[0].path("title").asText() shouldBe "주간 회의"
            }
        }

        context("기간/종일/시간 일정이 섞여 있는 경우") {
            it("기간일정 → 종일일정 → 시간순(동점 시 생성순)으로 정렬한다") {
                // Given
                val member = memberRepository.save(createMember(nickname = "test"))
                val calendar = createCalendar()
                val participant = createParticipant(
                        member = member,
                        calendar = calendar,
                        participationStatus = ParticipationStatus.ACCEPTED
                )
                val category = categoryRepository.save(createCategory())
                calendar.addParticipant(participant)
                calendarRepository.save(calendar)

                val targetDate = "2025-07-15"

                // 1. 기간 일정 (시작일 < 대상일 = 가장 상단에 배치)
                val periodSchedule = createSchedule(
                        title = "기간 프로젝트",
                        startDate = "2025-07-14",
                        endDate = "2025-07-16",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category
                )

                // 2. 종일 일정 (당일, 종일 = 기간아님 → 두 번째)
                val allDaySchedule = createSchedule(
                        title = "종일 행사",
                        startDate = "2025-07-15",
                        endDate = "2025-07-15",
                        isAllDay = true,
                        member = member,
                        calendar = calendar,
                        category = category
                )

                // 3. 시간 일정 (당일, 14:00 시작, 먼저 생성)
                val timeScheduleA = createSchedule(
                        title = "오후 회의 A",
                        startDate = "2025-07-15",
                        endDate = "2025-07-15",
                        startTime = "14:00",
                        endTime = "15:00",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category
                )

                // 4. 시간 일정 (당일, 14:00 시작, 같은 시간, 더 나중에 생성)
                val timeScheduleB = createSchedule(
                        title = "오후 회의 B",
                        startDate = "2025-07-15",
                        endDate = "2025-07-15",
                        startTime = "14:00",
                        endTime = "16:00",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category
                )

                // 5. 시간 일정 (당일, 09:00 시작, 날짜 기준으로 A/B보다 윗줄)
                val morningSchedule = createSchedule(
                        title = "오전 회의",
                        startDate = "2025-07-15",
                        endDate = "2025-07-15",
                        startTime = "09:00",
                        endTime = "10:00",
                        isAllDay = false,
                        member = member,
                        calendar = calendar,
                        category = category
                )

                // 6. 해당 일이 아닌 일정 (포함 안 되어야 함)
                val otherDaySchedule = createSchedule(
                        title = "제외 테스트",
                        startDate = "2025-07-16",
                        endDate = "2025-07-16",
                        member = member,
                        calendar = calendar,
                        category = category
                )

                // 일정 생성 및 저장 (createdAt 순서 적용)
                scheduleRepository.save(periodSchedule)
                scheduleRepository.save(allDaySchedule)
                scheduleRepository.save(morningSchedule)
                scheduleRepository.save(timeScheduleA)
                scheduleRepository.save(timeScheduleB)
                scheduleRepository.save(otherDaySchedule)

                val token = jwtFixture.createValidToken(member.id)

                // When
                val response = req.get("/calendars/${calendar.id}/schedules?date=$targetDate", token = token)

                // Then
                res.assertSuccess(response)
                val responseBody = response.contentAsString
                val jsonNode = objectMapper.readTree(responseBody)

                val schedulesNode = jsonNode.path("data").path("schedules")
                schedulesNode.size() shouldBe 5

                // 기간 > 종일 > 오전(시간) > 오후(동일 시간, 생성순) 테스트
                schedulesNode[0].path("title").asText() shouldBe "기간 프로젝트"
                schedulesNode[1].path("title").asText() shouldBe "종일 행사"
                schedulesNode[2].path("title").asText() shouldBe "오전 회의"
                schedulesNode[3].path("title").asText() shouldBe "오후 회의 A"
                schedulesNode[4].path("title").asText() shouldBe "오후 회의 B"
            }
        }
    }
})