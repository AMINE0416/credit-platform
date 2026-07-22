@echo off
REM Kill Java processes on port 8081
echo Stopping Java processes...
taskkill /IM java.exe /F

REM Wait for cleanup
timeout /t 2

REM Navigate to project directory
cd /d C:\Users\asus\Documents\credit-platform

REM Start the application
echo.
echo Starting Credit Platform...
java -jar target/credit-platform-0.0.1-SNAPSHOT.jar

REM Keep window open on exit
pause
