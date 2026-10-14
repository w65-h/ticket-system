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

echo [信息] 正在启动服务...
docker compose up -d

echo.
echo 后端正在初始化（约 30-60 秒）...
echo 可通过命令 "docker compose ps" 查看容器健康状态

:wait_backend
docker compose ps backend 2>nul | findstr "healthy" >nul 2>&1
if errorlevel 1 (
    timeout /t 3 /nobreak >nul
    goto wait_backend
)

echo 服务已就绪！
start http://localhost