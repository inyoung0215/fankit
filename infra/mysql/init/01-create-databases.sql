-- 서비스별 분리된 schema (DB-per-service 원칙)
-- MSA 경계 = DB 경계. 한 서비스가 다른 서비스의 DB에 직접 접근하는 것은 금지.

CREATE DATABASE IF NOT EXISTS fankit_user
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS fankit_order
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS fankit_payment
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS fankit_settlement
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS fankit_admin
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- fankit_* 패턴의 모든 schema에 대한 권한
GRANT ALL PRIVILEGES ON `fankit\_%`.* TO 'fankit'@'%';

-- MySQL 8의 default caching_sha2_password는 useSSL=false JDBC 환경에서 거절됨
-- mysql_native_password로 강제 → 개발 편의 (운영은 SSL + caching_sha2 권장)
ALTER USER 'fankit'@'%' IDENTIFIED WITH mysql_native_password BY 'fankit';

-- Docker for Mac 환경에서 host의 published port로 들어오는 연결은 mysqld 입장에서
-- localhost로 인식되는 케이스가 있어 'fankit'@'localhost' 명시적 생성 필요
CREATE USER IF NOT EXISTS 'fankit'@'localhost' IDENTIFIED WITH mysql_native_password BY 'fankit';
GRANT ALL PRIVILEGES ON `fankit\_%`.* TO 'fankit'@'localhost';

FLUSH PRIVILEGES;
