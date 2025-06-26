#!/bin/bash

# Start apache2
service apache2 start

# Fix starting problem mysql
# chown -R mysql:mysql /var/lib/mysql /var/run/mysqld
find /var/lib/mysql -type f -exec touch {} \;

# Start mysql
result=$(/etc/init.d/mysql start)
echo "Result starting mysql: " result

# Sleep infinity to make the shell always active
sleep infinity


