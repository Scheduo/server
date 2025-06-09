package com.example.scheduo.global.auth

import com.example.scheduo.domain.member.entity.Member
import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.fixture.createMember
import com.example.scheduo.global.config.security.provider.JwtProvider
import com.example.scheduo.global.response.status.ResponseStatus
import com.example.scheduo.util.Request
import com.example.scheduo.util.Response
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest @Autowired constructor(
        val mockMvc: MockMvc,
        val objectMapper: ObjectMapper,
        val jwtProvider: JwtProvider,
        val redisTemplate: RedisTemplate<String, String>,
        val memberRepository: MemberRepository
) : DescribeSpec({
    lateinit var req: Request
    lateinit var res: Response

    val member: Member = memberRepository.save(createMember(nickname = "테스트")) // 테스트용 member fixture
    val deviceUUID = "test-device-uuid-1"
    val refreshTokenKey = "refresh:${member.id}:$deviceUUID"

    beforeTest {
        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)
    }


    describe("로그인 시") {
        context("일반적인 요청일 경우") {
            it("redis에 refreshToken k:v가 생성된다") {
                val refreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 1, TimeUnit.MINUTES)

                // redis에서 값 조회
                val stored = redisTemplate.opsForValue().get(refreshTokenKey)

                stored shouldBe refreshToken
            }
        }
    }

    describe("POST /auth/logout 요청 시") {
        context("정상 요청일 경우") {
            it("redis에서 refreshToken이 삭제된다") {
                // refreshToken 저장
                val refreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 1, TimeUnit.MINUTES)

                val result = req.post("/auth/logout", mapOf("refreshToken" to refreshToken), jwtProvider.createAccessToken(member.id))

                // redis에서 삭제 확인
                val stored = redisTemplate.opsForValue().get(refreshTokenKey)

                res.assertSuccess(result)
                stored shouldBe null
            }
        }

        context("존재하지 않는 refreshToken일 경우") {
            it("401 에러를 반환한다") {
                val invalidRefreshToken = "invalid.token.value"
                val result = req.post("/auth/logout", mapOf("refreshToken" to invalidRefreshToken), jwtProvider.createAccessToken(member.id))
                res.assertFailure(result, ResponseStatus.REFRESH_TOKEN_INVALID)
            }
        }

        context("이미 삭제된(없는) refreshToken일 경우") {
            it("401 에러를 반환한다") {
                val refreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                // 저장하지 않고 바로 삭제 시도
                val result = req.post("/auth/logout", mapOf("refreshToken" to refreshToken), jwtProvider.createAccessToken(member.id))
                res.assertFailure(result, ResponseStatus.EXPIRED_REFRESH_TOKEN)
            }
        }

        context("accessToken/refreshToken의 memberId가 불일치한 경우") {
            it("401 에러를 반환한다") {
                val anotherMember = memberRepository.save(createMember(nickname = "다른멤버"))
                val refreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                redisTemplate.opsForValue().set("refresh:${member.id}:$deviceUUID", refreshToken, 1, TimeUnit.MINUTES)
                val result = req.post("/auth/logout", mapOf("refreshToken" to refreshToken), jwtProvider.createAccessToken(anotherMember.id))
                res.assertFailure(result, ResponseStatus.REFRESH_TOKEN_MEMBER_MISMATCH)
                memberRepository.delete(anotherMember)
            }
        }
    }

    describe("POST /auth/token 요청 시") {
        context("정상 요청일 경우") {
            it("redis에 새로운 refreshToken이 저장된다") {
                // 기존 refreshToken 저장
                val oldRefreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                redisTemplate.opsForValue().set(refreshTokenKey, oldRefreshToken, 1, TimeUnit.MINUTES)
                Thread.sleep(1000)

                val result = req.post("/auth/token", mapOf("refreshToken" to oldRefreshToken), jwtProvider.createAccessToken(member.id))

                // 응답에서 새로운 refreshToken 추출
                val response = objectMapper.readTree(result.contentAsString)
                val newRefreshToken = response["data"]["refreshToken"].asText()

                // redis에 새로운 refreshToken이 저장되어 있는지 확인
                val stored = redisTemplate.opsForValue().get(refreshTokenKey)

                res.assertSuccess(result)
                stored shouldBe newRefreshToken
                stored shouldNotBe oldRefreshToken
            }
        }

        context("존재하지 않는 refreshToken일 경우") {
            it("401 에러를 반환한다") {
                val invalidRefreshToken = "invalid.token.value"
                val result = req.post("/auth/token", mapOf("refreshToken" to invalidRefreshToken), jwtProvider.createAccessToken(member.id))
                res.assertFailure(result, ResponseStatus.REFRESH_TOKEN_INVALID)
            }
        }

        context("만료된 refreshToken일 경우") {
            it("401 에러를 반환한다") {
                val refreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 1, TimeUnit.MILLISECONDS)
                Thread.sleep(10)
                val result = req.post("/auth/token", mapOf("refreshToken" to refreshToken), jwtProvider.createAccessToken(member.id))
                res.assertFailure(result, ResponseStatus.EXPIRED_REFRESH_TOKEN)
            }
        }

        context("이미 사용된 refreshToken(로테이션된 토큰)으로 재요청할 경우") {
            it("401 에러를 반환한다") {
                val refreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 1, TimeUnit.MINUTES)
                Thread.sleep(1000)

                // 1차 로테이션 성공
                val firstResult = req.post("/auth/token", mapOf("refreshToken" to refreshToken), jwtProvider.createAccessToken(member.id))
                res.assertSuccess(firstResult)

                val response1 = objectMapper.readTree(firstResult.contentAsString)
                val newRefreshToken1 = response1["data"]["refreshToken"].asText()

                // 2차: 이전 refreshToken으로 재요청
                val secondResult = req.post("/auth/token", mapOf("refreshToken" to refreshToken), jwtProvider.createAccessToken(member.id))
                val response2 = objectMapper.readTree(firstResult.contentAsString)
                val newRefreshToken2 = response2["data"]["refreshToken"].asText()

                res.assertFailure(secondResult, ResponseStatus.OUTDATED_REFRESH_TOKEN)
            }
        }

        context("accessToken 없이 요청해도 refreshToken이 유효하면") {
            it("새로운 accessToken과 refreshToken이 정상적으로 발급된다") {
                // given: 유효한 refreshToken만 준비
                val refreshToken = jwtProvider.createRefreshToken(member.id, deviceUUID)
                redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 1, TimeUnit.MINUTES)
                Thread.sleep(1000)

                // when: accessToken 없이 요청
                val result = req.post(
                        "/auth/token",
                        mapOf("refreshToken" to refreshToken)
                        // Authorization 헤더 없이 요청
                )

                // then: 새로운 accessToken, refreshToken이 잘 발급되고 redis에 저장되어야 함
                val response = objectMapper.readTree(result.contentAsString)
                val newAccessToken = response["data"]["accessToken"].asText()
                val newRefreshToken = response["data"]["refreshToken"].asText()
                val stored = redisTemplate.opsForValue().get(refreshTokenKey)

                res.assertSuccess(result)
                newAccessToken shouldNotBe null
                newRefreshToken shouldNotBe refreshToken
                stored shouldBe newRefreshToken
            }
        }

        // cleanup
        redisTemplate.delete(refreshTokenKey)
        memberRepository.delete(member)
    }
})