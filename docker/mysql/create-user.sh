#!/bin/bash
mysql -u root --password=${MYSQL_ROOT_PASSWORD} -e "FLUSH PRIVILEGES;  CREATE USER IF NOT EXISTS 'test_user'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}'"
