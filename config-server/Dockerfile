# Multi-stage: 1. aşamada Maven ile build alınır, 2. aşamaya sadece jar kopyalanır.
# Böylece final imajda Maven, kaynak kod ve .m2 cache bulunmaz -> çok daha küçük imaj.

# ---------- 1. AŞAMA: BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# config-server/pom.xml parent olarak ../pom.xml'e referans veriyor,
# bu yüzden kök pom da imaja kopyalanmalı.
# NOT: build context repo kökü olmalı:
#   docker build -f config-server/Dockerfile -t config-server .
COPY pom.xml pom.xml
COPY config-server config-server

# Modül dizininden build al (sadece config-server derlenir, diğer modüller gerekmez)
WORKDIR /build/config-server
RUN mvn package -DskipTests

# ---------- 2. AŞAMA: RUNTIME ----------
# Çalıştırmak için sadece JRE yeterli
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Jar'ı builder aşamasından kopyala
COPY --from=builder /build/config-server/target/config-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]