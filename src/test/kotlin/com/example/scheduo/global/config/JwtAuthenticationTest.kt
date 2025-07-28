package com.example.scheduo.global.config

import com.example.scheduo.domain.member.repository.MemberRepository
import com.example.scheduo.fixture.JwtFixture
import com.example.scheduo.fixture.createMember
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
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class JwtAuthenticationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val memberRepository: MemberRepository,
    @Autowired val jwtFixture: JwtFixture
) : DescribeSpec({

    lateinit var req: Request
    lateinit var res: Response

    beforeTest {
        req = Request(mockMvc, objectMapper)
        res = Response(objectMapper)
    }

    afterTest {
        memberRepository.deleteAll()
    }

    describe("인증이 필요한 API 요청 시") {
        context("유효한 토큰이 Authorization 헤더에 있을 경우") {
            it("200 OK와 정상 응답이 반환된다") {
                val member = memberRepository.save(createMember(email = "test@example.com"))
                val validToken = jwtFixture.createValidToken(member.id)
                val response = req.get("/members/me", token = validToken)
                res.assertSuccess(response)
            }
        }

        context("토큰이 없을 경우") {
            it("401 Unauthorized와 에러 메시지가 반환된다") {
                val response = req.get("/members/me")
                res.assertFailure(response, ResponseStatus.NOT_EXIST_TOKEN)
            }
        }

        context("유효하지 않은 토큰이 Authorization 헤더에 있을 경우") {
            it("401 Unauthorized와 에러 메시지가 반환된다") {
                val invalidToken = jwtFixture.createInvalidToken()
                val response = req.get("/members/me", token = invalidToken)
                res.assertFailure(response, ResponseStatus.INVALID_TOKEN)
            }
        }
    }
})
