CREATE DATABASE IF NOT EXISTS kawa_customer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS kawa_wallet   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS kawa_retailer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS kawa_partner  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON kawa_customer.* TO 'kawa'@'%';
GRANT ALL PRIVILEGES ON kawa_wallet.* TO 'kawa'@'%';
GRANT ALL PRIVILEGES ON kawa_retailer.* TO 'kawa'@'%';
GRANT ALL PRIVILEGES ON kawa_partner.* TO 'kawa'@'%';
FLUSH PRIVILEGES;
