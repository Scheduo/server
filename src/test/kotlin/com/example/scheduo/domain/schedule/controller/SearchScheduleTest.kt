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
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchScheduleTest(
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
    lateinit var me: Member
    lateinit var myCalA: Calendar
    lateinit var myCalB: Calendar
    lateinit var otherCal: Calendar

    beforeSpec {
        // 데이터 초기화
        scheduleRepository.deleteAll()
        recurrenceRepository.deleteAll()
        categoryRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()

        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)

        // 사용자 및 캘린더/참여 설정
        me = memberRepository.save(createMember(nickname = "searcher"))
        myCalA = createCalendar(name = "Work")
        myCalB = createCalendar(name = "Weekend")
        otherCal = createCalendar(name = "Other")

        val pA = createParticipant(
            member = me,
            calendar = myCalA,
            role = Role.EDIT,
            participationStatus = ParticipationStatus.ACCEPTED
        )
        val pB = createParticipant(
            member = me,
            calendar = myCalB,
            role = Role.EDIT,
            participationStatus = ParticipationStatus.ACCEPTED
        )
        myCalA.addParticipant(pA)
        myCalB.addParticipant(pB)

        // 다른 사람 멤버/참여는 만들지 않아도 범위 테스트 가능
        calendarRepository.saveAll(listOf(myCalA, myCalB, otherCal))
    }

    afterSpec {
        scheduleRepository.deleteAll()
        recurrenceRepository.deleteAll()
        categoryRepository.deleteAll()
        calendarRepository.deleteAll()
        memberRepository.deleteAll()
    }

    fun saveSchedule(
        title: String,
        calendar: Calendar,
        owner: Member = me,
        start: String = "2025-08-10",
        end: String = "2025-08-10"
    ): Schedule {
        val cat = categoryRepository.save(createCategory())
        return scheduleRepository.save(
            createSchedule(
                title = title,
                start = LocalDate.parse(start).atStartOfDay(),
                end = LocalDate.parse(end).atStartOfDay(),
                member = owner,
                calendar = calendar,
                category = cat
            )
        )
    }

    fun search(keyword: String, tokenMemberId: Long = me.id): String {
        val token = jwtFixture.createValidToken(tokenMemberId)
        val response = req.get("/schedules/search?keyword=$keyword", token = token)
        res.assertSuccess(response) // 기본적으로 200 OK 성공을 기대
        return response.contentAsString
    }

    describe("GET /schedules/search") {

        context("keyword 경계 - 빈 문자열") {
            it("빈 문자열이면 빈 리스트를 반환한다") {
                // 준비
                val s1 = saveSchedule("Meet", myCalA)
                val s2 = saveSchedule("memo", myCalA)
                val s3 = saveSchedule("Alpha", myCalB)
                val s4 = saveSchedule("beta", myCalB)
                // otherCal 스케줄(내가 속하지 않음)
                val sX = saveSchedule("Meet outside", otherCal)

                // 실행
                val response = search(keyword = "")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                contents.shouldHaveSize(0)
            }
        }

        context("keyword 경계 - 대소문자 무시") {
            it("'m'로 검색하면 'Meet', 'memo'가 반환된다") {
                scheduleRepository.deleteAll()
                val s1 = saveSchedule("Meet", myCalA)
                val s2 = saveSchedule("memo", myCalB)
                val s3 = saveSchedule("Beta", myCalA)

                val response = search(keyword = "m")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                val titles = contents.elements().asSequence()
                    .map { it.get("title").asText() }
                    .toList()

                titles.shouldContainExactlyInAnyOrder(listOf("Meet", "memo"))
            }
        }

        context("keyword 경계 - 공백 시작/끝") {
            it("'  Me' 처럼 앞에 공백이 있으면 prefix 매칭 실패하여 0건을 반환한다") {
                scheduleRepository.deleteAll()
                saveSchedule("Meet", myCalA)
                saveSchedule("Memo", myCalA)

                val response = search(keyword = "  Me")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                contents.shouldHaveSize(0)
            }

            it("'Me  ' 처럼 뒤에 공백이 있으면 'Me  %'로 매칭되어 결과가 0건일 수 있다") {
                scheduleRepository.deleteAll()
                saveSchedule("Me", myCalA)
                saveSchedule("Mezz", myCalA)

                val response = search(keyword = "Me  ")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                contents.shouldHaveSize(0)
            }
        }

        context("범위 경계 - 내가 속한 캘린더만 포함") {
            it("다른 캘린더(otherCal)의 스케줄은 결과에서 제외된다") {
                scheduleRepository.deleteAll()
                val s1 = saveSchedule("Meet", myCalA)
                val s2 = saveSchedule("Meet", myCalB)
                val sX = saveSchedule("Meet", otherCal)

                val response = search(keyword = "Me")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                val ids = contents.elements().asSequence()
                    .map { it.get("scheduleId").asLong() }
                    .toList()

                ids.shouldContainExactlyInAnyOrder(listOf(s1.id, s2.id))
            }
        }

        context("결과 경계 - 0건") {
            it("매칭 0건이면 빈 리스트를 반환한다") {
                scheduleRepository.deleteAll()
                saveSchedule("Alpha", myCalA)

                val response = search(keyword = "Z")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                contents.shouldHaveSize(0)
            }
        }

        context("결과 경계 - 1건") {
            it("매칭 1건이면 해당 스케줄만 반환한다") {
                scheduleRepository.deleteAll()
                val s = saveSchedule("Meeting", myCalA)

                val response = search(keyword = "Meet")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                val titles = contents.elements().asSequence()
                    .map { it.get("title").asText() }
                    .toList()

                titles.shouldContainExactlyInAnyOrder(listOf("Meeting"))
            }
        }

        context("결과 경계 - n개") {
            it("다건이면 startDate, startTime, id 순으로 정렬된다") {
                scheduleRepository.deleteAll()
                val a1 = saveSchedule("Meet-1", myCalA, start = "2025-08-10", end = "2025-08-10")
                val a2 = saveSchedule("Meet-2", myCalA, start = "2025-08-10", end = "2025-08-10") // 같은 날, id 순 정렬 영향
                val b1 = saveSchedule("Meet-3", myCalA, start = "2025-08-11", end = "2025-08-11")

                val response = search(keyword = "Meet")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                val ids = contents.elements().asSequence()
                    .map { it.get("scheduleId").asLong() }
                    .toList()

                ids shouldBe listOf(a1.id, a2.id, b1.id)
            }
        }

        context("반복 일정과 단일 일정 혼재") {
            it("반복/단일과 무관하게 title prefix 기준으로 동일하게 매칭된다") {
                scheduleRepository.deleteAll()

                val catA = categoryRepository.save(createCategory())
                val catB = categoryRepository.save(createCategory())
                val rec =
                    recurrenceRepository.save(createRecurrence(frequency = "WEEKLY", recurrenceEndDate = "2025-12-31"))

                val single = scheduleRepository.save(
                    createSchedule(
                        title = "Review",
//                        startDate = "2025-08-01",
//                        endDate = "2025-08-01",
                        start = LocalDateTime.of(2025, 8, 1, 9, 0),
                        end = LocalDateTime.of(2025, 8, 1, 10, 0),
                        member = me,
                        calendar = myCalA,
                        category = catA
                    )
                )

                val recurring = scheduleRepository.save(
                    createSchedule(
                        title = "Review weekly",
//                        startDate = "2025-08-01",
//                        endDate = "2025-08-01",
                        start = LocalDateTime.of(2025, 8, 1, 11, 0),
                        end = LocalDateTime.of(2025, 8, 1, 12, 0),
                        member = me,
                        calendar = myCalA,
                        category = catB,
                        recurrence = rec
                    )
                )

                val response = search(keyword = "Re")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                val titles = contents.elements().asSequence()
                    .map { it.get("title").asText() }
                    .toList()

                titles.shouldContainExactlyInAnyOrder(listOf("Review", "Review weekly"))
            }
        }

        context("반복 일정 검색") {
            it("반복 일정은 단일 일정과 동일하게 하나의 결과로 검색된다") {
                scheduleRepository.deleteAll()

                val cat = categoryRepository.save(createCategory())
                val rec =
                    recurrenceRepository.save(createRecurrence(frequency = "WEEKLY", recurrenceEndDate = "2025-12-31"))

                val recurring = scheduleRepository.save(
                    createSchedule(
                        title = "Review weekly",
//                        startDate = "2025-08-01",
//                        endDate = "2025-08-01",
                        start = LocalDateTime.of(2025, 8, 1, 11, 0),
                        end = LocalDateTime.of(2025, 8, 1, 12, 0),
                        member = me,
                        calendar = myCalA,
                        category = cat,
                        recurrence = rec
                    )
                )

                val response = search(keyword = "Re")
                val json = objectMapper.readTree(response)
                val contents = json["data"]["contents"]

                contents.shouldHaveSize(1)
            }
        }

        context("인증/권한 경계 - 미인증") {
            it("미인증이면 401 Unauthorized를 반환한다") {
                scheduleRepository.deleteAll()
                saveSchedule("Meet", myCalA)

                val response = req.get("/schedules/search?keyword=Me", token = null)

                res.assertFailure(response, ResponseStatus.NOT_EXIST_TOKEN)
            }
        }
    }
})
