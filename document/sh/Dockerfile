# 通用 Spring Boot 镜像构建模板（兼容 mall-admin / mall-portal / mall-search / mall-ai）
# 用法 1（推荐：build-and-run.sh 自动处理）：
#   ./document/sh/build-and-run.sh mall-admin
#
# 用法 2（手动）：
#   # 1. 用 mvn 打包
#   mvn -pl mall-admin -am package -DskipTests -Ddocker.skip=true
#   # 2. 把 jar 拷到当前目录并命名为 app.jar（或在 docker build 时覆盖 JAR_FILE）
#   cp mall-admin/target/mall-admin-1.0-SNAPSHOT.jar ./app.jar
#   docker build -t mall/mall-admin:1.0-SNAPSHOT .
#
# 用法 3（fabric8 docker-maven-plugin 直接构建）：
#   mvn -pl mall-admin -am package -Ddocker.skip=false

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="macrozheng"

# 通过 --build-arg 注入；不传时使用默认值
ARG JAR_FILE=app.jar
ENV JAR_FILE=${JAR_FILE}

WORKDIR /app

# 安装时区数据 + 上海时区（apk add tzdata 是为了拿到 zoneinfo 文件）
RUN apk add --no-cache tzdata wget \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

COPY ${JAR_FILE} app.jar

# 容器内统一日志目录
RUN mkdir -p /var/logs
VOLUME ["/var/logs"]

EXPOSE 8080

# 健康检查（默认指向 8080；不同服务可改用 service-specific Docker image）
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "app.jar"]
