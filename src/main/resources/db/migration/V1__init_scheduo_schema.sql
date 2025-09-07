-- V1__init_scheduo_schema.sql
-- Scheduo 프로젝트의 기존 스키마를 Flyway 베이스라인으로 생성
-- 테이블 생성 순서: 참조되는 테이블부터 생성 (의존성 순서 고려)

-- 기존 테이블 삭제 (역순으로 삭제 - 외래키 제약조건 때문)
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS exception;
DROP TABLE IF EXISTS recurrence;
DROP TABLE IF EXISTS participant;
DROP TABLE IF EXISTS calendar;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS member;

-- 1. Member 테이블 (기준 테이블)
CREATE TABLE member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100),
    nickname VARCHAR(100) UNIQUE,
    social_type VARCHAR(50),
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
);

-- 2. Category 테이블 (독립 테이블)
CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    color VARCHAR(50),
    PRIMARY KEY (id)
);

-- 3. Calendar 테이블
CREATE TABLE calendar (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
);

-- 4. Recurrence 테이블
CREATE TABLE recurrence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recurrence_rule TEXT,
    recurrence_end_date DATE,
    PRIMARY KEY (id)
);

-- 5. Participant 테이블 (Calendar과 Member 참조)
CREATE TABLE participant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(100),
    role VARCHAR(50),
    status VARCHAR(50),
    calendar_id BIGINT,
    member_id BIGINT,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (calendar_id) REFERENCES calendar(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 6. Exception 테이블 (Recurrence 참조)
CREATE TABLE exception (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recurrence_id BIGINT,
    exception_date DATE,
    PRIMARY KEY (id),
    FOREIGN KEY (recurrence_id) REFERENCES recurrence(id) ON DELETE CASCADE
);

-- 7. Notification 테이블 (Member 참조)
CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT,
    notification_type VARCHAR(50),
    message VARCHAR(200),
    data JSON,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 8. Schedule 테이블 (모든 테이블 참조)
CREATE TABLE schedule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100),
    location VARCHAR(100),
    memo TEXT,
    is_all_day BOOLEAN NOT NULL,
    start DATETIME(6) NOT NULL,
    end DATETIME(6) NOT NULL,
    notification_time VARCHAR(50),
    is_override BOOLEAN NOT NULL DEFAULT FALSE,
    category_id BIGINT,
    member_id BIGINT,
    calendar_id BIGINT,
    recurrence_id BIGINT,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES category(id),
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (calendar_id) REFERENCES calendar(id) ON DELETE CASCADE,
    FOREIGN KEY (recurrence_id) REFERENCES recurrence(id)
);