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
FLUSH PRIVILEGES;
