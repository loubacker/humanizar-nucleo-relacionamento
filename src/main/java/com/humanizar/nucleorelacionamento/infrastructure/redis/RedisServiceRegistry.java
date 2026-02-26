package com.humanizar.nucleorelacionamento.infrastructure.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class RedisServiceRegistry {

    private static final Logger logger = Logger.getLogger(RedisServiceRegistry.class.getName());

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${service.registry.enabled}")
    private boolean registryEnabled;

    @Value("${service.registry.ttl}")
    private long ttl;

    @Value("${service.registry.port}")
    private int servicePort;

    private final String instanceId = UUID.randomUUID().toString();
    private final String serviceName = "humanizar-nucleo-relacionamento";
    private String serviceAddress;
    private String serviceKey;

    @EventListener(ApplicationReadyEvent.class)
    public void registerService() {
        if (!registryEnabled) {
            logger.info("O registro de serviço está desativado");
            return;
        }

        try {
            this.serviceAddress = detectServiceAddress();
            this.serviceKey = String.format("humanizar-nucleo-relacionamento:%s", instanceId);

            logger.info("Registrando serviço no Redis");
            logger.info(String.format("Endereço do Serviço: %s", serviceAddress));
            logger.info(String.format("ID da Instância: %s", instanceId));
            logger.info(String.format("Chave do Serviço: %s", serviceKey));

            sendHeartbeat();
        } catch (UnknownHostException e) {
            logger.severe(String.format("Falha ao detectar endereço do serviço: %s", e.getMessage()));
        }
    }

    @Scheduled(fixedRateString = "${service.registry.heartbeat-interval}")
    public void sendHeartbeat() {
        if (!registryEnabled || serviceKey == null) {
            return;
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemoryBytes = totalMemory - freeMemory;
            long usedMemoryMB = usedMemoryBytes / (1024 * 1024);

            Map<String, String> serviceInfo = new HashMap<>();
            serviceInfo.put("address", serviceAddress);
            serviceInfo.put("memory_usage_mb", String.valueOf(usedMemoryMB));
            serviceInfo.put("last_heartbeat", String.valueOf(System.currentTimeMillis()));
            serviceInfo.put("status", "online");
            serviceInfo.put("service_name", serviceName);
            serviceInfo.put("instance_id", instanceId);

            @SuppressWarnings("Convert2Lambda")
            SessionCallback<Object> callback = new SessionCallback<Object>() {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                public Object execute(RedisOperations operations) {
                    operations.multi();
                    operations.opsForHash().putAll(serviceKey, serviceInfo);
                    operations.expire(serviceKey, ttl, TimeUnit.SECONDS);
                    return operations.exec();
                }
            };
            redisTemplate.execute(callback);

            logger.info(String.format("Heartbeat enviado. Uso de memória: %d MB", usedMemoryMB));
        } catch (Exception e) {
            logger.severe(String.format("Erro ao enviar heartbeat: %s", e.getMessage()));
        }
    }

    @PreDestroy
    public void deregisterService() {
        if (!registryEnabled || serviceKey == null) {
            return;
        }

        try {
            logger.info("Removendo registro do serviço do Redis");
            redisTemplate.delete(serviceKey);
            logger.info("Serviço desregistrado com sucesso");
        } catch (IllegalStateException e) {
            logger.info(
                    "Conexão com Redis já fechada durante o encerramento (comportamento esperado). O serviço irá expirar automaticamente via TTL.");
        } catch (Exception e) {
            logger.warning(String.format("Erro ao desregistrar serviço: %s", e.getMessage()));
        }
    }

    private String detectServiceAddress() throws UnknownHostException {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("::1")) {
                try (java.net.Socket socket = new java.net.Socket()) {
                    socket.connect(new java.net.InetSocketAddress("8.8.8.8", 80));
                    ipAddress = socket.getLocalAddress().getHostAddress();
                } catch (java.io.IOException e) {
                    logger.warning("Não foi possível determinar IP da rede, usando localhost");
                    ipAddress = "localhost";
                }
            }

            return String.format("%s:%d", ipAddress, servicePort);
        } catch (UnknownHostException e) {
            logger.warning("Falha ao detectar IP do serviço, usando localhost");
            return String.format("localhost:%d", servicePort);
        }
    }

    // Getters monitoring/debugging
    public String getInstanceId() {
        return instanceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public boolean isRegistryEnabled() {
        return registryEnabled;
    }
}