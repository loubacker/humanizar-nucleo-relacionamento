package com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.idempotency;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.ProcessedEventPort;

@Component
public class ProcessedEventGuard {

    private final ProcessedEventPort processedEventPort;

    public ProcessedEventGuard(ProcessedEventPort processedEventPort) {
        this.processedEventPort = processedEventPort;
    }

    public void ensureNotProcessed(String consumerName, UUID eventId, String correlationId) {
        if (processedEventPort.existsByConsumerNameAndEventId(consumerName, eventId)) {
            throw new NucleoRelacionamentoException(ReasonCode.DUPLICATE_EVENT, correlationId);
        }
    }
}
