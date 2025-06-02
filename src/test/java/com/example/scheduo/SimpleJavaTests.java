package com.example.scheduo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SimpleJavaTests {

	@DisplayName("JUnit 동작 확인")
	@Test
	void test() {
		Assertions.assertThat(1 + 2).isEqualTo(3);
	}

}
