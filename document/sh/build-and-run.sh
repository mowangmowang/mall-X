#!/usr/bin/env bash
# 构建 JAR + Docker image + 启动容器，一体化脚本
# 用法：./build-and-run.sh <module>   e.g. ./build-and-run.sh mall-admin
#
# 依赖：
#   1. mvn 已安装（用于 mvn package）
#   2. docker daemon 在运行（或远程 -Ddocker.host 已配置）
#   3. 基础设施 stack 已启动（docker-compose-env.yml），共享 mall-net 网络
#
# 流程：
#   mvn package -pl <module> -am -DskipTests -Ddocker.skip=true
#   → 只产 jar（不构建镜像），避免远程 docker host 不可达
#   → 然后手动用 Dockerfile 构建 image
#   → 然后 docker run 启动容器并接入 mall-net

set -euo pipefail

MODULE="${1:-mall-admin}"
GROUP_NAME="${GROUP_NAME:-mall}"
APP_VERSION="${APP_VERSION:-1.0-SNAPSHOT}"
IMAGE="${GROUP_NAME}/${MODULE}:${APP_VERSION}"
CONTAINER_NAME="${MODULE}"
PORT="${PORT:-8080}"

echo "==== [1/4] mvn package -pl ${MODULE} -am -DskipTests ===="
mvn -q -pl "${MODULE}" -am package -DskipTests -Ddocker.skip=true

# 定位 jar（spring-boot 默认重命名为 original-<artifactId>-<version>.jar +
# 真正的可执行 jar 是 <artifactId>-<version>.jar）
JAR_FILE=$(ls -1 "${MODULE}/target/${MODULE}-${APP_VERSION}.jar" 2>/dev/null || \
           ls -1 "${MODULE}/target/"*.jar 2>/dev/null | head -n 1)
if [[ -z "${JAR_FILE}" || ! -f "${JAR_FILE}" ]]; then
    echo "ERROR: 未找到 ${MODULE} 的 jar 包，请检查 maven 构建是否成功" >&2
    exit 1
fi
echo "Built: ${JAR_FILE}"

echo "==== [2/4] 清理旧容器与镜像 ===="
docker stop "${CONTAINER_NAME}" 2>/dev/null || true
docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true
docker rmi "${IMAGE}" 2>/dev/null || true

echo "==== [3/4] 构建镜像（build context 为 ${MODULE}）===="
# 用 sed 把 jar 拷到临时 build context（document/sh 里放的是通用 Dockerfile）
BUILD_CTX=$(mktemp -d)
trap "rm -rf ${BUILD_CTX}" EXIT
cp "${JAR_FILE}" "${BUILD_CTX}/app.jar"
cp "$(dirname "$0")/Dockerfile" "${BUILD_CTX}/Dockerfile"
docker build -t "${IMAGE}" "${BUILD_CTX}"

echo "==== [4/4] 启动容器（共享 mall-net 网络）===="
docker run -d \
    --name "${CONTAINER_NAME}" \
    --network mall-net \
    -p "${PORT}:${PORT}" \
    -e TZ=Asia/Shanghai \
    -v /etc/localtime:/etc/localtime:ro \
    -v "/mydata/app/${MODULE}/logs:/var/logs" \
    "${IMAGE}"

echo "==== Done. 容器 ${CONTAINER_NAME} 已启动 ===="
docker ps --filter "name=${CONTAINER_NAME}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
