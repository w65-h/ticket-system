@echo off
chcp 65001 >nul
cd /d "C:\idea\project\untitled1\ticket-system"
"C:\Program Files\Docker\Docker\resources\bin\docker.exe" compose up -d --build
echo.
echo 容器已启动，后端正在初始化（约 30-60 秒）...
echo 建议：待容器状态全部变成 Healthy 后再访问 http://localhost
echo 可通过命令 "docker compose ps" 查看容器健康状态
echo.
timeout /t 5 >nul
start http://localhost