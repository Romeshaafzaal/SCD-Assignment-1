@echo off
rem Compile and run the Campus Management System (needs JDK 17 or newer).
if not exist out mkdir out
javac -d out src\*.java || exit /b 1
java -cp out Main
