# Multi-stage: 1. aşamada Maven ile build alınır, 2. aşamaya sadece jar kopyalanır.
# Böylece final imajda Maven, kaynak kod ve .m2 cache bulunmaz -> çok daha küçük imaj.

# ---------- 1. AŞAMA: BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# gateway-server/pom.xml parent olarak ../pom.xml'e referans veriyor,
# bu yüzden kök pom da imaja kopyalanmalı.
# NOT: build context repo kökü olmalı:
#   docker build -f gateway-server/Dockerfile -t gateway-server .
COPY pom.xml pom.xml
COPY gateway-server gateway-server

# Modül dizininden build al (sadece gateway-server derlenir, diğer modüller gerekmez)
WORKDIR /build/gateway-server
RUN mvn package -DskipTests

# ---------- 2. AŞAMA: RUNTIME ----------
# Çalıştırmak için sadece JRE yeterli
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Jar'ı builder aşamasından kopyala
COPY --from=builder /build/gateway-server/target/gateway-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
