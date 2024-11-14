-- Refresh Token Table
CREATE TABLE refresh_token
(
    id          INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    token       VARCHAR(255)                             NOT NULL, -- Ensuring token is unique to avoid duplicates
    expiry_date TIMESTAMP WITHOUT TIME ZONE              NOT NULL,
    user_id     INTEGER                                  NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uc_refresh_token_user UNIQUE (user_id),             -- Unique constraint on user_id
    CONSTRAINT uc_refresh_token_token UNIQUE (token),              -- Unique constraint on token itself
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES _user (id) ON DELETE CASCADE
);