package com.example.scheduo

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SimpleKotilnTests: FunSpec( {
    test("kotest 동작 확인") {
        1 + 2 shouldBe 3
    }
})