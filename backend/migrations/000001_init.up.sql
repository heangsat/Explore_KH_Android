-- Core catalog and user tables for Explore KH.
-- MariaDB 11.x, InnoDB, utf8mb4 for Khmer text.

CREATE TABLE IF NOT EXISTS provinces (
    id VARCHAR(64) NOT NULL,
    name_en VARCHAR(120) NOT NULL,
    name_kh VARCHAR(120) NOT NULL,
    region VARCHAR(60) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attractions (
    id VARCHAR(64) NOT NULL,
    province_id VARCHAR(64) NOT NULL,
    title_en VARCHAR(200) NOT NULL,
    title_kh VARCHAR(200) NOT NULL,
    category ENUM('Heritage', 'Eco-Tourism', 'Culture', 'Leisure') NOT NULL,
    description_en TEXT NOT NULL,
    description_kh TEXT NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    price_khr VARCHAR(40) NOT NULL,
    price_usd VARCHAR(40) NOT NULL,
    price_khr_value BIGINT NOT NULL DEFAULT 0,
    price_usd_value DECIMAL(10, 2) NOT NULL DEFAULT 0,
    opening_hours VARCHAR(120) NOT NULL,
    is_24_hours TINYINT(1) NOT NULL DEFAULT 0,
    rating DECIMAL(2, 1) NOT NULL DEFAULT 4.5,
    image_url VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_attractions_province (province_id),
    KEY idx_attractions_category (category),
    KEY idx_attractions_rating (rating),
    KEY idx_attractions_price_usd (price_usd_value),
    FULLTEXT KEY ft_attractions_search (title_en, title_kh, description_en, description_kh),
    CONSTRAINT fk_attractions_province
        FOREIGN KEY (province_id) REFERENCES provinces (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Opening hours can contain more than one range per day (for example a midday
-- break), so ranges are stored separately from the display string.
CREATE TABLE IF NOT EXISTS attraction_hours (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    attraction_id VARCHAR(64) NOT NULL,
    opens_at TIME NOT NULL,
    closes_at TIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_attraction_hours_attraction (attraction_id),
    CONSTRAINT fk_attraction_hours_attraction
        FOREIGN KEY (attraction_id) REFERENCES attractions (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_refresh_tokens_hash (token_hash),
    KEY idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    attraction_id VARCHAR(64) NOT NULL,
    rating TINYINT UNSIGNED NOT NULL,
    comment TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_reviews_user_attraction (user_id, attraction_id),
    KEY idx_reviews_attraction (attraction_id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_reviews_attraction
        FOREIGN KEY (attraction_id) REFERENCES attractions (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bookmarks (
    user_id BIGINT UNSIGNED NOT NULL,
    attraction_id VARCHAR(64) NOT NULL,
    notes TEXT NULL,
    saved_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, attraction_id),
    KEY idx_bookmarks_attraction (attraction_id),
    CONSTRAINT fk_bookmarks_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_attraction
        FOREIGN KEY (attraction_id) REFERENCES attractions (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS catalog_versions (
    id TINYINT UNSIGNED NOT NULL,
    version BIGINT UNSIGNED NOT NULL DEFAULT 1,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO catalog_versions (id, version)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE version = version;
