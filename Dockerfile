# 使用Alpine版本的JDK以减小镜像体积
FROM amazoncorretto:8-alpine

# 设置工作目录
WORKDIR /app

# 复制本地构建好的jar文件到容器中
COPY target/java-xxe-demo-1.0-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8888

# 设置启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

# 添加标签
LABEL maintainer="XXE Demo" \
      version="1.0" \
      description="XXE漏洞测试演示应用"