 @echo off
cd /d "C:\idea\project\untitled1\ticket-system"
"C:\Program Files\Docker\Docker\resources\bin\docker.exe" compose down
echo.
echo 项目已关闭
timeout /t 2 >nul