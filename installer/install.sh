#!/usr/bin/env bash

sbt assembly
cp ../target/scala-2.11/RealmsOfTrinityTracker.jar /usr/local/bin/RealmsOfTrinityTracker.jar

# I need to import the database data
psql -c 'CREATE DATABASE realmsoftrinitytracker'

function execSql(){
	psql -d realmsoftrinitytracker -f $1
}

execSql ../sql/tables.sql
execSql ../sql/views.sql
execSql ../sql/dump.sql

v=cat /proc/version
if [[ $v == *"Ubuntu"* ]]; then
	cp resource/RealmsOfTrinityTracker.desktop ~/.config/autostart/RealmsOfTrinityTracker.desktop
else
	echo "Could not install autostart: OS not supported."
fi
