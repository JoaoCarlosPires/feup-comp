#!/bin/sh

FILE=$1
ARGUMENTS=$2

gradle build
java -jar $FILE $ARGUMENTS