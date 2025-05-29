package com.example.scheduo.fixture

import com.example.scheduo.global.config.security.provider.JwtProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JwtFixture(
        @Autowired private val jwtProvider: JwtProvider
) {
    fun createValidToken(memberId: Long): String = jwtProvider.createAccessToken(memberId)
    fun createInvalidToken(): String = "invalid.token.abc123"
}
