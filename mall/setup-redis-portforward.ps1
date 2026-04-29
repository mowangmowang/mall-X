# ========================================
# 设置 WSL2 Redis 端口转发 (PowerShell 版本)
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "设置 WSL2 Redis 端口转发" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取 WSL2 的 IP 地址
$wslIp = wsl hostname -I
$wslIp = $wslIp.Trim()

if ([string]::IsNullOrEmpty($wslIp)) {
    Write-Host "✗ 无法获取 WSL2 IP 地址，请确保 WSL2 正在运行" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "检测到 WSL2 IP 地址: $wslIp" -ForegroundColor Green
Write-Host ""

# 删除旧的转发规则
Write-Host "正在删除旧的转发规则..." -ForegroundColor Yellow
netsh interface portproxy delete v4tov4 listenport=6379 listenaddress=0.0.0.0 2>$null

# 添加新的转发规则
Write-Host "正在设置端口转发规则..." -ForegroundColor Yellow
$result = netsh interface portproxy add v4tov4 listenport=6379 listenaddress=0.0.0.0 connectport=6379 connectaddress=$wslIp

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ 端口转发设置成功！" -ForegroundColor Green
    Write-Host "  Windows localhost:6379 -> WSL2 ${wslIp}:6379" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "✗ 设置失败，请确保以管理员身份运行此脚本" -ForegroundColor Red
    Write-Host "  右键点击脚本 -> '使用 PowerShell 运行' -> 选择'是'允许管理员权限" -ForegroundColor Red
    Write-Host ""
    pause
    exit 1
}

# 显示所有转发规则
Write-Host "当前端口转发规则：" -ForegroundColor Cyan
netsh interface portproxy show all

Write-Host ""
Write-Host "提示: 每次重启 WSL2 后需要重新运行此脚本" -ForegroundColor Yellow
Write-Host ""
pause
