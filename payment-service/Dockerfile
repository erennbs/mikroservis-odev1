# Multi-stage: 1. aşamada Maven ile build alınır, 2. aşamaya sadece jar kopyalanır.
# Böylece final imajda Maven, kaynak kod ve .m2 cache bulunmaz -> çok daha küçük imaj.

# ---------- 1. AŞAMA: BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# payment-service/pom.xml parent olarak ../pom.xml'e referans veriyor,
# bu yüzden kök pom da imaja kopyalanmalı.
# NOT: build context repo kökü olmalı:
#   docker build -f payment-service/Dockerfile -t payment-service .
COPY pom.xml pom.xml
COPY payment-service payment-service

# Modül dizininden build al (sadece payment-service derlenir, diğer modüller gerekmez)
WORKDIR /build/payment-service
RUN mvn package -DskipTests

# ---------- 2. AŞAMA: RUNTIME ----------
# Çalıştırmak için sadece JRE yeterli
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Jar'ı builder aşamasından kopyala
COPY --from=builder /build/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar app.jar

# Port 0 (rastgele) ile ayağa kalkar ve kendini Eureka'ya kaydeder; sabit port EXPOSE edilmez.
ENTRYPOINT ["java", "-jar", "app.jar"]
