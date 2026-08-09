CREATE TABLE users (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    email         NVARCHAR(255) NOT NULL,
    password_hash NVARCHAR(100) NOT NULL,
    full_name     NVARCHAR(150) NOT NULL,
    phone         NVARCHAR(20)  NULL,
    role          NVARCHAR(30)  NOT NULL,
    enabled       BIT           NOT NULL DEFAULT 1,
    created_at    DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at    DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'RESTAURANT_OWNER', 'DRIVER', 'ADMIN'))
);

CREATE TABLE addresses (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    label      NVARCHAR(50)  NOT NULL,
    line1      NVARCHAR(200) NOT NULL,
    line2      NVARCHAR(200) NULL,
    city       NVARCHAR(100) NOT NULL,
    latitude   FLOAT         NULL,
    longitude  FLOAT         NULL,
    is_default BIT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_addresses_user ON addresses (user_id);
