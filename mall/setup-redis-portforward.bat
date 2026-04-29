@echo off
echo ========================================
echo 设置 WSL2 Redis 端口转发
echo ========================================
echo.

REM 获取 WSL2 的 IP 地址
for /f "tokens=*" %%i in ('wsl hostname -I') do set WSL_IP=%%i

REM 去除前后空格
for /f "tokens=* delims= " %%a in ("%WSL_IP%") do set WSL_IP=%%a

echo 检测到 WSL2 IP 地址: %WSL_IP%
echo.

REM 删除旧的转发规则（如果存在）
echo 正在删除旧的转发规则...
netsh interface portproxy delete v4tov4 listenport=6379 listenaddress=0.0.0.0 >nul 2>&1

REM 添加新的转发规则
echo 正在设置端口转发规则...
netsh interface portproxy add v4tov4 listenport=6379 listenaddress=0.0.0.0 connectport=6379 connectaddress=%WSL_IP%

if %errorlevel% equ 0 (
    echo.
    echo ✓ 端口转发设置成功！
    echo   Windows localhost:6379 -> WSL2 %WSL_IP%:6379
    echo.
) else (
    echo.
    echo ✗ 设置失败，请确保以管理员身份运行此脚本
    echo.
)

REM 显示所有转发规则
echo 当前端口转发规则：
netsh interface portproxy show all

echo.
pause
