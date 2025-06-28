@echo off
set APP_NAME=Typing Speed Tester
set MAIN_JAR=TypingTestApp.jar
set ICON=logo.ico
set VERSION=1.0.0

:: Compile Java source files
echo Compiling Java source files...
javac -d out src\*.java

:: Create JAR file
echo Creating JAR file...
jar --create --file %MAIN_JAR% --main-class TypingSpeedTester -C out .

:: Package as .exe with shortcut and Start Menu
echo Packaging as .exe...
jpackage ^
--input . ^
--name "%APP_NAME%" ^
--main-jar %MAIN_JAR% ^
--type exe ^
--icon %ICON% ^
--app-version %VERSION% ^
--win-menu ^
--win-shortcut ^
--dest build

echo Done! Check the 'build' folder for the .exe
pause
