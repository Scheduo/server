package com.example.scheduo.fixture

import com.example.scheduo.domain.schedule.entity.Category
import com.example.scheduo.domain.schedule.entity.Color

fun createCategory(): Category {
    return Category(
        null,
        "개인",
        Color.RED
    )
}