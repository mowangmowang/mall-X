#!/usr/bin/env bash
# 启动已存在的 mall-admin 镜像
# 前置：image 已构建（mvn -pl mall-admin -am package），共享网络 mall-net 已创建
#   docker compose -f document/docker/docker-compose-env.yml -p mall up -d
#
# 推荐改用：docker compose -f document/docker/docker-compose-app.yml -p mall up -d

set -euo pipefail

app_name='mall-admin'
app_port="${PORT:-8080}"

docker stop "${app_name}" 2>/dev/null || true
echo '----stop container----'
docker rm -f "${app_name}" 2>/dev/null || true
echo '----rm container----'

# 清理 dangling 镜像（保护：仅当有匹配项才删）
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
