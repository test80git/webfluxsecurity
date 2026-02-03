CREATE TABLE items
(
    id         SERIAL PRIMARY KEY,
    user_id     SERIAL,
    text       VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);