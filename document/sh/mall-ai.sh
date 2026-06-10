#!/usr/bin/env bash
# 启动已存在的 mall-ai 镜像
# 注意：必须注入 AI_API_KEY（或在 compose env 文件里设置）否则 Spring AI 启动失败

set -euo pipefail

app_name='mall-ai'
app_port="${PORT:-8086}"

docker stop "${app_name}" 2>/dev/null || true
echo '----stop container----'
docker rm -f "${app_name}" 2>/dev/null || true
echo '----rm container----'

dangling=$(docker images -f "dangling=true" -q || true)
if [[ -n "${dangling}" ]]; then
    docker rmi ${dangling} 2>/dev/null || true
    echo '----rm dangling images----'
fi

if [[ -z "${AI_API_KEY:-}" ]]; then
    echo "ERROR: AI_API_KEY 环境变量未设置。请先 export AI_API_KEY=sk-xxx 后再启动。" >&2
    exit 1
fi

docker run -d \
    --name "${app_name}" \
    --network mall-net \
    -p "${app_port}:${app_port}" \
    -e TZ=Asia/Shanghai \
    -e "AI_API_KEY=${AI_API_KEY}" \
    -e "AI_BASE_URL=${AI_BASE_URL:-https://api.deepseek.com}" \
    -e "AI_MODEL=${AI_MODEL:-deepseek-chat}" \
    -e "ALLOWED_ORIGINS=${ALLOWED_ORIGINS:-http://localhost:8080}" \
    -v /etc/localtime:/etc/localtime:ro \
    -v "/mydata/app/${app_name}/logs:/var/logs" \
    "mall/${app_name}:1.0-SNAPSHOT"
echo '----start container----'
docker ps --filter "name=${app_name}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
