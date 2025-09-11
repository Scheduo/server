-- schedule
CREATE TABLE schedule_v2 (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    calendar_id       BIGINT NOT NULL,
    category_id       BIGINT NOT NULL,
    serial_id         VARCHAR(100) NOT NULL,
    title             VARCHAR(100) NOT NULL,
    location          VARCHAR(100) NULL,
    memo              VARCHAR(100) NULL,
    is_all_day        BOOLEAN NOT NULL DEFAULT FALSE,
    start             DATETIME(6) NULL,
    end               DATETIME(6) NULL,
    notification_time VARCHAR(50) NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT
);

CREATE TABLE recurrence_v2 (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    schedule_id         BIGINT NOT NULL,
    recurrence_rule     TEXT   NOT NULL,
    recurrence_end_date DATE   NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (schedule_id) REFERENCES schedule_v2(id) ON DELETE CASCADE
);

CREATE TABLE exception_v2 (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    recurrence_id    BIGINT NOT NULL,
    exception_date   DATE   NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (recurrence_id) REFERENCES recurrence_v2(id) ON DELETE CASCADE
);