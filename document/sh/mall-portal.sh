#!/usr/bin/env bash
# 启动已存在的 mall-portal 镜像

set -euo pipefail

app_name='mall-portal'
app_port="${PORT:-8085}"

docker stop "${app_name}" 2>/dev/null || true
echo '----stop container----'
docker rm -f "${app_name}" 2>/dev/null || true
echo '----rm container----'

dangling=$(docker images -f "dangling=true" -q || true)
if [[ -n "${dangling}" ]]; then
    docker rmi ${dangling} 2>/dev/null || true
    echo '----rm dangling images----'
fi

docker run -d \
    --name "${app_name}" \
    --network mall-net \
    -p "${app_port}:${app_port}" \
    -e TZ=Asia/Shanghai \
    -v /etc/localtime:/etc/localtime:ro \
    -v "/mydata/app/${app_name}/logs:/var/logs" \
    "mall/${app_name}:1.0-SNAPSHOT"
echo '----start container----'
docker ps --filter "name=${app_name}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
