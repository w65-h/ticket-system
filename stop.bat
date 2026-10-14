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

echo [信息] 正在停止所有服务...
docker compose stop

echo.
echo [完成] 所有服务已停止，数据已保留
echo 下次双击 start.bat 即可快速启动
echo.
pause