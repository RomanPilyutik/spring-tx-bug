# create databases
CREATE DATABASE IF NOT EXISTS `test_db`;
CREATE DATABASE IF NOT EXISTS `another_test_db`;

# grant rights
GRANT ALL PRIVILEGES ON *.* TO `test_user`@`%`;
