package com.example.scheduo.global.external

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate


@SpringBootTest
class RedisConnectionTest @Autowired constructor(
        private val redisTemplate: StringRedisTemplate
) : DescribeSpec({
    describe("Redis 연결 테스트") {
        context("Redis에 값을 set/get 하면") {
            it("정상 동작해야 한다") {
                val key = "scheduo"
                val value = "proj"

                redisTemplate.opsForValue().set(key, value)
                val result = redisTemplate.opsForValue().get(key)

                result shouldBe value
            }
        }
    }
})