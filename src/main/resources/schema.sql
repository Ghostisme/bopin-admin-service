CREATE TABLE IF NOT EXISTS app_user (
  id VARCHAR(64) PRIMARY KEY,
  role VARCHAR(32) NOT NULL,
  nickname VARCHAR(100) NOT NULL,
  avatar VARCHAR(500) NOT NULL DEFAULT '',
  phone VARCHAR(40) NOT NULL,
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  city VARCHAR(100) NOT NULL DEFAULT '',
  categories CLOB NOT NULL,
  intro CLOB NOT NULL,
  experience_years INT NOT NULL DEFAULT 0,
  card_status VARCHAR(24) NOT NULL DEFAULT 'INCOMPLETE',
  card_data CLOB NOT NULL DEFAULT '{}',
  created_at BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_app_user_role_phone ON app_user(role, phone);

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS card_status VARCHAR(24) NOT NULL DEFAULT 'INCOMPLETE';
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS card_data CLOB NOT NULL DEFAULT '{}';

-- 模卡是主播可独立维护的作品集合；企业端只读取其中一张主展示模卡。
CREATE TABLE IF NOT EXISTS anchor_card (
  id VARCHAR(64) PRIMARY KEY,
  owner_id VARCHAR(64) NOT NULL,
  card_data CLOB NOT NULL,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE,
  status VARCHAR(24) NOT NULL DEFAULT 'PUBLIC',
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_anchor_card_owner ON anchor_card(owner_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS ix_anchor_card_primary ON anchor_card(owner_id, is_primary, status);

CREATE TABLE IF NOT EXISTS auth_session (
  token VARCHAR(100) PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  expires_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS user_wallet (
  user_id VARCHAR(64) PRIMARY KEY,
  card_balance INT NOT NULL DEFAULT 0,
  member_level VARCHAR(32) NOT NULL DEFAULT 'FREE',
  ai_quota INT NOT NULL DEFAULT 0,
  updated_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS job_notice (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(160) NOT NULL,
  job_type VARCHAR(32) NOT NULL,
  category VARCHAR(64) NOT NULL,
  salary_min DECIMAL(12,2) NOT NULL,
  salary_max DECIMAL(12,2) NOT NULL,
  salary_unit VARCHAR(32) NOT NULL,
  salary_display VARCHAR(80) NOT NULL,
  city VARCHAR(100) NOT NULL,
  address VARCHAR(300) NOT NULL,
  distance_km DECIMAL(10,2) NOT NULL DEFAULT 0,
  longitude DECIMAL(12,6) NOT NULL DEFAULT 0,
  latitude DECIMAL(12,6) NOT NULL DEFAULT 0,
  duties CLOB NOT NULL,
  requirements CLOB NOT NULL,
  tags CLOB NOT NULL,
  publisher_id VARCHAR(64) NOT NULL,
  publisher_name VARCHAR(180) NOT NULL,
  publisher_avatar VARCHAR(500) NOT NULL DEFAULT '',
  publisher_real_name BOOLEAN NOT NULL DEFAULT FALSE,
  publisher_enterprise BOOLEAN NOT NULL DEFAULT FALSE,
  urgent BOOLEAN NOT NULL DEFAULT FALSE,
  published_at BIGINT NOT NULL,
  view_count INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  apply_count INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS contact_unlock (
  id VARCHAR(64) PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  job_id VARCHAR(64) NOT NULL,
  cost INT NOT NULL,
  created_at BIGINT NOT NULL,
  UNIQUE(user_id, job_id)
);

CREATE TABLE IF NOT EXISTS membership_order (
  id VARCHAR(64) PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  plan VARCHAR(32) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS ai_script (
  id VARCHAR(64) PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  scene VARCHAR(80) NOT NULL,
  product VARCHAR(160) NOT NULL,
  tone VARCHAR(80) NOT NULL,
  content CLOB NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS conversation (
  id VARCHAR(64) PRIMARY KEY,
  role VARCHAR(32) NOT NULL,
  name VARCHAR(120) NOT NULL,
  avatar VARCHAR(500) NOT NULL DEFAULT '',
  last_message VARCHAR(500) NOT NULL,
  last_time BIGINT NOT NULL,
  unread INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS chat_message (
  id VARCHAR(64) PRIMARY KEY,
  conversation_id VARCHAR(64) NOT NULL,
  content CLOB NOT NULL,
  message_type VARCHAR(32) NOT NULL,
  from_me BOOLEAN NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS message_quota (
  user_id VARCHAR(64) PRIMARY KEY,
  remaining_count INT NOT NULL,
  total_count INT NOT NULL
);

CREATE TABLE IF NOT EXISTS employment_contract (
  id VARCHAR(64) PRIMARY KEY,
  anchor_name VARCHAR(120) NOT NULL,
  company VARCHAR(180) NOT NULL,
  job_title VARCHAR(160) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS settlement (
  id VARCHAR(64) PRIMARY KEY,
  contract_id VARCHAR(64) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  gross_amount DECIMAL(12,2) NOT NULL,
  service_fee DECIMAL(12,2) NOT NULL,
  net_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS paid_invitation (
  id VARCHAR(64) PRIMARY KEY,
  company VARCHAR(180) NOT NULL,
  anchor_name VARCHAR(120) NOT NULL,
  job_title VARCHAR(160) NOT NULL,
  fee DECIMAL(12,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS course (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(180) NOT NULL,
  mode VARCHAR(32) NOT NULL,
  city VARCHAR(100) NOT NULL,
  starts_at BIGINT NOT NULL,
  capacity INT NOT NULL,
  enrolled INT NOT NULL DEFAULT 0,
  price DECIMAL(12,2) NOT NULL,
  status VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS course_enrollment (
  id VARCHAR(64) PRIMARY KEY,
  course_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  score INT,
  certificate_no VARCHAR(80),
  created_at BIGINT NOT NULL,
  UNIQUE(course_id, user_id)
);

CREATE TABLE IF NOT EXISTS equipment_product (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(180) NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  group_price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL,
  participants INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS platform_order (
  id VARCHAR(64) PRIMARY KEY,
  order_type VARCHAR(32) NOT NULL,
  item_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS annual_event (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(180) NOT NULL,
  city VARCHAR(100) NOT NULL,
  event_date BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_registration (
  id VARCHAR(64) PRIMARY KEY,
  event_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  votes INT NOT NULL DEFAULT 0,
  created_at BIGINT NOT NULL,
  UNIQUE(event_id, user_id)
);

CREATE TABLE IF NOT EXISTS crossborder_settlement (
  id VARCHAR(64) PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  country VARCHAR(80) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  foreign_amount DECIMAL(14,2) NOT NULL,
  rate DECIMAL(14,6) NOT NULL,
  cny_amount DECIMAL(14,2) NOT NULL,
  fee DECIMAL(14,2) NOT NULL,
  net_cny DECIMAL(14,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS eor_provider (
  id VARCHAR(64) PRIMARY KEY,
  country VARCHAR(80) NOT NULL,
  name VARCHAR(180) NOT NULL,
  currencies VARCHAR(120) NOT NULL,
  service_fee DECIMAL(8,4) NOT NULL,
  rating DECIMAL(3,2) NOT NULL,
  status VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS eor_request (
  id VARCHAR(64) PRIMARY KEY,
  provider_id VARCHAR(64) NOT NULL,
  company VARCHAR(180) NOT NULL,
  candidate VARCHAR(120) NOT NULL,
  country VARCHAR(80) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at BIGINT NOT NULL
);
