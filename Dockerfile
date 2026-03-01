# -- Stage de Build --
FROM maven:3.9.12-eclipse-temurin-25-alpine AS build

WORKDIR /app

#-- Baixa dependências antecipadamente para otimizar cache --
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# -- Copia o código-fonte e compila o aplicativo --
COPY src src
RUN mvn -B -DskipTests package

# -- Stage de Execução (Runtime) --
FROM eclipse-temurin:25-jre-alpine

# -- Instala libgcc e cria usuário não-root --
RUN apk add --no-cache libgcc gcompat && addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# -- Copia o JAR compilado do stage de build com permissão de acesso ao usuário não-root --
COPY --from=build --chown=appuser:appgroup /app/target/humanizar-nucleo-relacionamento-0.0.1-SNAPSHOT.jar app.jar

# -- Configura variáveis de ambiente para otimização de memória e desempenho --
ENV MALLOC_ARENA_MAX=2
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xss256k -XX:+UseSerialGC -XX:ReservedCodeCacheSize=64m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError -Dsun.net.inetaddr.ttl=30 -Dsun.net.inetaddr.negative.ttl=2"

EXPOSE 9001

USER appuser
ENTRYPOINT ["java","-jar","app.jar"]
