-- Create skins_images table first (required for FK in skins table)
CREATE TABLE IF NOT EXISTS skins_images (
    skin_name VARCHAR(255) PRIMARY KEY,
    image_url TEXT NOT NULL
);

-- Create skins table
CREATE TABLE IF NOT EXISTS skins (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    asset_id VARCHAR(255) NOT NULL,
    float_value DOUBLE PRECISION,
    wear VARCHAR(50),
    paint_seed INTEGER,
    paint_index INTEGER,
    sticker_count INTEGER,
    store VARCHAR(100) NOT NULL,
    price_in_cents BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    link TEXT NOT NULL,
    is_sold BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (name) REFERENCES skins_images (skin_name)
);

-- Create index on name for faster lookups and FK relationship
CREATE INDEX IF NOT EXISTS idx_skins_name ON skins (name);

-- Create index on is_sold for filtering queries
CREATE INDEX IF NOT EXISTS idx_skins_is_sold ON skins (is_sold);

-- Create index on created_at for time-based queries
CREATE INDEX IF NOT EXISTS idx_skins_created_at ON skins (created_at);

-- Create index on created_at for time-based queries
CREATE INDEX IF NOT EXISTS idx_skins_created_at ON skins (created_at);

-- Create stickers table
CREATE TABLE IF NOT EXISTS stickers (
    id BIGSERIAL PRIMARY KEY,
    skin_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    slot INTEGER,
    wear DOUBLE PRECISION,
    skin_id_ref INTEGER,
    class_id VARCHAR(255),
    FOREIGN KEY (skin_id) REFERENCES skins (id) ON DELETE CASCADE
);

-- Create index on skin_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_stickers_skin_id ON stickers (skin_id);

-- Create history_update_tasks table
CREATE TABLE IF NOT EXISTS history_update_tasks (
    skin_id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (skin_id) REFERENCES skins (id) ON DELETE CASCADE
);

-- Create index on status for filtering queries
CREATE INDEX IF NOT EXISTS idx_history_update_tasks_status ON history_update_tasks (status);

-- Create index on expires_at for cleanup queries
CREATE INDEX IF NOT EXISTS idx_history_update_tasks_expires_at ON history_update_tasks (expires_at);

-- Create failed_conversions table
CREATE TABLE IF NOT EXISTS failed_conversions (
    id BIGSERIAL PRIMARY KEY,
    skin_id VARCHAR(255) NOT NULL,
    original_price BIGINT NOT NULL,
    original_currency VARCHAR(10) NOT NULL,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (skin_id) REFERENCES skins (id) ON DELETE CASCADE
);

-- Create index on skin_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_failed_conversions_skin_id ON failed_conversions (skin_id);

-- Create index on retry_count for retry logic
CREATE INDEX IF NOT EXISTS idx_failed_conversions_retry_count ON failed_conversions (retry_count);