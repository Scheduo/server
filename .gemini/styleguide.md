# 🖐️ Style Guide for Scheduo Project

본 프로젝트에서는 일관된 코드 스태일과 아키텍처 설계를 유지하기 위해 다음과 같은 가이드라인을 따릅니다.

---

## 1. 아키텍처 콘벤션

### ✅ Service 인터페이스 도입

* 모든 서비스 클래스는 인터페이스로 정의하고, 구현체는 `impl` 패키지에 위치시킨다.

    * 예시: `MemberService`, `MemberServiceImpl`

### ✅ 객체 생성 방식

* 모든 객체 생성 시 Builder 패턴을 사용합니다. 생성자는 지양합니다.

    * ❌ `new Member(...)`
    * ✅ `Member.builder().email("test@example.com").build()`

---

## 2. DTO 및 Entity 변환 규칙

### ✅ Service 계층에서의 변환

* **Service** 계층에서 DTO ↔ Entity 변환 메서드 호출 (`to`, `from`).

### ✅ DTO의 역할

* DTO는 객체를 담고 변환만 합니다. 행위(비즈니스 로직)은 포함하지 않습니다.
* 메서드명은 `fromEntity` 대신 `from`, `toEntity` 대신 `to`로 간단히 작성합니다.

    * ❌ `GetProfile`
    * ✅ `Profile`

---

## 3. 테스트 코드 콘벤션

### ✅ 테스트 코드 스태일

* Kotest의 `DescribeSpec`을 활용하며, 다음과 같은 형식을 따릅니다:

```kotlin
describe("POST /calendar 요청 시") {
    context("인증된 사용자가 정상적으로 캔레더를 생성하면") {
        it("201 Created와 캔레더 정보를 반환한다") {
            // ...
        }
    }
    context("인증되지 않은 사용자가 요청하면") {
        it("오류를 반환한다") {
            // ...
        }
    }
}
```

### ✅ 테스트 환경 설정

* `application-test.yml`을 분리하고 `test` 프로필을 통해 적용합니다.
  예: `@ActiveProfiles("test")`

---

## 4. Fixture 관리

### ✅ 위치

* `com.example.scheduo.fixture` 패키지를 생성하여 모든 fixture를 관리합니다.

### ✅ 도메인별로 분류

* `MemberFixture.kt`, `CalendarFixture.kt` 등 도메인 단위로 파

---

## 5. 리뷰 언어 정책

리뷰 언어 정책

- ✅ 한글 사용: 모든 코멘트, 제안, 설명은 한글로 작성
- ❌ 영어 사용 금지: 리뷰 시 영어 사용은 절대 불허
- 📝 예외 상황: 영어 기술 용어는 허용하되, 설명은 반드시 한글로 작성

---

## 6. 메서드 네이버 콘벤션 (Controller / Service / ServiceImpl)

* Controller가 호출하는 메서드는 대치적인 도메인 기능을 목록적으로 설계

    * ex) `getMyProfile()`, `createCalendar()`
* Service가 호출하는 메서드는 구체적 기능을 목록적으로 설계

    * ex) `findById()`, `save()`, `update()`
* ServiceImpl에서는 Service 역할을 구현하며, 메서드 이름은 Service 인터페이스와 동일

---
