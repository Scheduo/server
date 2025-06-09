package com.example.scheduo.global.external

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@ActiveProfiles("test")
class RedisConnectionTest @Autowired constructor(
        private val redisTemplate: StringRedisTemplate
) : DescribeSpec({
    describe("Redis 연결 테스트") {
        context("PING 명령을 보내면") {
            it("PONG을 받아야 한다") {
                // RedisConnection을 통해 ping 명령 실행
                val pong = redisTemplate.connectionFactory!!.connection.ping()
                pong shouldBe "PONG"
            }
        }
    }
})