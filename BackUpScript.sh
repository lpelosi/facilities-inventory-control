#!/bin/bash
database="facilities"
date=$(date +"%b-%d-%Y")
backup_path=""
# get name
echo "Please enter your name:"
read user
echo `mysqldump -u "$user" -p "$database" > "$backup_path""$database"-"$date".sql`
