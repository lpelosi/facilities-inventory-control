#!/bin/bash
database="facilities"
date=$(date +"%b-%d-%Y")
backup_path="../db/"
# get name
echo "Please enter your name:"
read user
echo `mysqldump -u "$user" -p "$database" > "$backup_path""$database"-"$date".sql`
