#!/usr/bin/env bash
# 已废弃：此脚本原先从 document/sh/ 目录直接 docker build，build context 错误且 Dockerfile 硬编码 mall-admin。
# 新版统一改用 fabric8 docker-maven-plugin 或 document/sh/build-and-run.sh。
#
# 旧脚本保留以防回退到旧的部署方式。
# 推荐：
#   1) Maven 一键打包 + 推镜像：mvn -pl mall-admin -am package -DskipTests
#   2) 启动容器：./document/sh/mall-admin.sh
#   3) 或 docker compose -f document/docker/docker-compose-app.yml -p mall up -d

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "${SCRIPT_DIR}/build-and-run.sh" mall-admin
