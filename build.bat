@echo off
chcp 65001 >nul
cd /d "%~dp0"

where docker >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Docker 命令！
    pause
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker 未运行！
    pause
    exit /b 1
)

echo ============================================
echo  重新构建 Docker 镜像（预计 5-10 分钟）
echo  请耐心等待...
echo ============================================
echo.

docker compose down
docker compose build
docker compose up -d

echo.
echo 后端正在初始化（约 30-60 秒）...

:wait_backend
docker compose ps backend 2>nul | findstr "healthy" >nul 2>&1
if errorlevel 1 (
    timeout /t 3 /nobreak >nul
    goto wait_backend
)

echo 服务已就绪！
start http://localhost