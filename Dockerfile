# Stage 1: Build ứng dụng
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Tận dụng Docker Cache: Tải trước dependency trước khi copy src
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy mã nguồn và đóng gói ứng dụng
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime (Chạy ứng dụng)
FROM eclipse-temurin:21-jre

WORKDIR /app

# Tạo user non-root để tăng bảo mật
RUN addgroup --system spring && adduser --system --ingroup spring spring

# Copy file JAR đã build
COPY --from=build /app/target/*.jar app.jar

# Tạo thư mục uploads và phân quyền cho user spring
RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring

EXPOSE 8080

# Chạy ứng dụng hỗ trợ biến môi trường JAVA_OPTS linh hoạt
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
