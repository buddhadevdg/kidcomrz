#!/bin/bash
CLASS=com.shareart.service.ServiceRouter
LIB_PATH=/home/ec2-user/kidscomrz/app/lib/*
JAVA_HOME=/home/ec2-user/kidscomrz/app/java_installer/jdk-8u65-linux-x64/bin/java
$JAVA_HOME -classpath "$LIB_PATH" $CLASS

