
ALTER TABLE member
    RENAME COLUMN createdAt TO created_at,
    RENAME COLUMN updatedAt TO updated_at;

ALTER TABLE calendar
    RENAME COLUMN createdAt TO created_at,
    RENAME COLUMN updatedAt TO updated_at;

ALTER TABLE participant
    RENAME COLUMN createdAt TO created_at,
    RENAME COLUMN updatedAt TO updated_at;

ALTER TABLE notification
    RENAME COLUMN createdAt TO created_at,
    RENAME COLUMN updatedAt TO updated_at;

ALTER TABLE schedule
    RENAME COLUMN createdAt TO created_at,
    RENAME COLUMN updatedAt TO updated_at;