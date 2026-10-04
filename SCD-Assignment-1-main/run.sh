#!/bin/sh
# Compile and run the Campus Management System (needs JDK 17 or newer).
mkdir -p out
javac -d out src/*.java || exit 1
java -cp out Main
