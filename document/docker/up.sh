#!/usr/bin/env bash
# mall 完整栈一键启动/停止
# 自动按顺序拉起 env + app 两个 compose stack
# 共享项目名 -p mall + 共享网络 mall-net
#
# 用法：
#   ./document/docker/up.sh start         # 启动
#   ./document/docker/up.sh stop          # 停止
#   ./document/docker/up.sh restart       # 重启
#   ./document/docker/up.sh ps            # 状态
#   ./document/docker/up.sh logs -f       # 跟踪日志
#   ./document/docker/up.sh down          # 停止并删除容器、网络

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT="${PROJECT:-mall}"
ACTION="${1:-start}"
shift || true

ENV_FILE="${SCRIPT_DIR}/docker-compose-env.yml"
APP_FILE="${SCRIPT_DIR}/docker-compose-app.yml"

case "${ACTION}" in
    start|up)
        echo "==== Starting infrastructure stack (mysql/redis/rabbitmq/es/mongo/minio/...) ===="
        docker compose -f "${ENV_FILE}" -p "${PROJECT}" up -d "$@"
        echo ""
        echo "==== Waiting for infrastructure to be healthy ===="
        # 等待关键服务健康
        for svc in mysql redis elasticsearch; do
            echo -n "  ${svc}: "
            for i in {1..30}; do
                state=$(docker compose -f "${ENV_FILE}" -p "${PROJECT}" ps --format json "${svc}" 2>/dev/null | python -c "import sys,json;d=json.loads(sys.stdin.read() or '[]');print(d[0]['Health'] if d else 'starting')" 2>/dev/null || echo "starting")
                if [[ "${state}" == "healthy" ]]; then
                    echo "healthy"
                    break
                fi
                sleep 2
            done
            if [[ "${state:-}" != "healthy" ]]; then
                echo "timeout (continuing anyway)"
            fi
        done
        echo ""
        echo "==== Starting app stack (mall-admin/mall-portal/mall-search/mall-ai) ===="
        docker compose -f "${APP_FILE}" -p "${PROJECT}" up -d "$@"
        echo ""
        echo "==== Done. 状态 ===="
        docker compose -f "${ENV_FILE}" -p "${PROJECT}" ps
        docker compose -f "${APP_FILE}" -p "${PROJECT}" ps
        ;;
    stop)
        echo "==== Stopping app stack ===="
        docker compose -f "${APP_FILE}" -p "${PROJECT}" stop "$@" 2>/dev/null || true
        echo "==== Stopping infrastructure stack ===="
        docker compose -f "${ENV_FILE}" -p "${PROJECT}" stop "$@" 2>/dev/null || true
        ;;
    restart)
        "${SCRIPT_DIR}/up.sh" stop "$@"
        "${SCRIPT_DIR}/up.sh" start "$@"
        ;;
    ps)
        docker compose -f "${ENV_FILE}" -p "${PROJECT}" ps
        docker compose -f "${APP_FILE}" -p "${PROJECT}" ps
        ;;
    logs)
        docker compose -f "${ENV_FILE}" -p "${PROJECT}" logs "$@"
        docker compose -f "${APP_FILE}" -p "${PROJECT}" logs "$@"
        ;;
    down)
        echo "==== Removing app stack ===="
        docker compose -f "${APP_FILE}" -p "${PROJECT}" down "$@" 2>/dev/null || true
        echo "==== Removing infrastructure stack (keeps volumes) ===="
        docker compose -f "${ENV_FILE}" -p "${PROJECT}" down "$@" 2>/dev/null || true
        echo ""
        echo "数据卷保留。如需删除：docker volume prune"
        echo "如需删除 mall-net 网络：docker network rm mall-net"
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|ps|logs|down} [args...]" >&2
        exit 1
        ;;
esac
