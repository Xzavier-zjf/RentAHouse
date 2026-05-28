USE rental_platform;

CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(32),
  email VARCHAR(128),
  avatar VARCHAR(512),
  real_name VARCHAR(64),
  id_card VARCHAR(32),
  status INT DEFAULT 1,
  role VARCHAR(32) DEFAULT 'user',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  ip_address VARCHAR(64),
  user_agent VARCHAR(512),
  login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_login_log_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS house (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  description TEXT,
  owner_id BIGINT NOT NULL,
  address VARCHAR(255),
  city VARCHAR(64),
  district VARCHAR(64),
  price DECIMAL(10,2),
  area FLOAT,
  room_num INT,
  toilet_num INT,
  floor INT,
  total_floor INT,
  orientation VARCHAR(32),
  decoration VARCHAR(64),
  facilities TEXT,
  status INT DEFAULT 1,
  view_count INT DEFAULT 0,
  audit_status INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_house_owner_id (owner_id),
  INDEX idx_house_city_district (city, district)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS house_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  house_id BIGINT NOT NULL,
  url VARCHAR(512) NOT NULL,
  is_cover INT DEFAULT 0,
  sort INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_house_image_house_id (house_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS house_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  house_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_house_favorite_user_house (user_id, house_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  house_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content TEXT,
  rating INT,
  like_count INT DEFAULT 0,
  parent_id BIGINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_comment_house_id (house_id),
  INDEX idx_comment_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comment_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  comment_id BIGINT NOT NULL,
  url VARCHAR(512) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_comment_image_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  house_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  owner_id BIGINT,
  start_date DATE,
  end_date DATE,
  months INT,
  price DECIMAL(10,2),
  total_amount DECIMAL(10,2),
  deposit DECIMAL(10,2),
  status INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_order_user_id (user_id),
  INDEX idx_order_house_id (house_id),
  INDEX idx_order_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  from_user_id BIGINT NOT NULL,
  to_user_id BIGINT NOT NULL,
  content TEXT,
  is_read INT DEFAULT 0,
  type INT DEFAULT 2,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_message_from_user_id (from_user_id),
  INDEX idx_message_to_user_id (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (username, password, email, status, role)
VALUES ('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiV1bYv1IXaZr4Q1V.4Ml1fE6RIJkB2', 'admin@example.com', 1, 'admin')
ON DUPLICATE KEY UPDATE username = username;
