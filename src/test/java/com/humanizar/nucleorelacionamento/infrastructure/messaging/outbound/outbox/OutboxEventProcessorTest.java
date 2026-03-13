package com.humanizar.nucleorelacionamento.infrastructure.messaging.outbound.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.domain.model.OutboxEvent;
import com.humanizar.nucleorelacionamento.domain.model.enums.OutboxStatus;
import com.humanizar.nucleorelacionamento.domain.port.OutboxEventPort;
import com.humanizar.nucleorelacionamento.infrastructure.messaging.outbound.rabbit.RabbitOutboxPublisher;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventPort outboxEventPort;

    @Mock
    private RabbitOutboxPublisher rabbitOutboxPublisher;

    @Mock
    private OutboxRetryPolicy outboxRetryPolicy;

    @InjectMocks
    private OutboxEventProcessor outboxEventProcessor;

    @Test
    void shouldClaimAndLockEventsFromOutbox() {
        OutboxEvent event = newOutboxEvent(OutboxStatus.NEW);
        when(outboxEventPort.findPendingForRelay(anyList(), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(event));
        when(outboxEventPort.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<OutboxEvent> claimed = outboxEventProcessor.claimBatch(20);

        assertEquals(1, claimed.size());
        assertEquals(OutboxStatus.LOCKED, event.getStatus());
        assertNotNull(event.getLockedBy());
        assertNotNull(event.getNextRetryAt());
        verify(outboxEventPort).save(event);
    }

    @Test
    void shouldProcessUsingFreshEntityInsteadOfStaleInput() {
        UUID eventId = UUID.randomUUID();
        OutboxEvent staleEvent = newOutboxEventWithEventId(eventId, OutboxStatus.NEW);
        OutboxEvent freshEvent = newOutboxEventWithEventId(eventId, OutboxStatus.NEW);

        when(outboxEventPort.findPendingForRelay(anyList(), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(freshEvent));
        when(outboxEventPort.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        outboxEventProcessor.claimBatch(1);

        when(outboxEventPort.findByEventId(eventId)).thenReturn(Optional.of(freshEvent));

        outboxEventProcessor.processEvent(staleEvent);

        verify(rabbitOutboxPublisher).publish(freshEvent);
        verify(outboxEventPort, never()).save(staleEvent);
        assertEquals(OutboxStatus.NEW, staleEvent.getStatus());
        assertEquals(OutboxStatus.PUBLISHED, freshEvent.getStatus());
    }

    @Test
    void shouldSkipProcessingWhenFencingFails() {
        OutboxEvent staleEvent = newOutboxEvent(OutboxStatus.NEW);
        OutboxEvent freshEvent = newOutboxEventWithEventId(staleEvent.getEventId(), OutboxStatus.NEW);
        when(outboxEventPort.findByEventId(staleEvent.getEventId())).thenReturn(Optional.of(freshEvent));

        outboxEventProcessor.processEvent(staleEvent);

        verify(rabbitOutboxPublisher, never()).publish(any(OutboxEvent.class));
        verify(outboxEventPort, never()).save(any(OutboxEvent.class));
    }

    @Test
    void shouldMarkAsFailedWhenPublishThrowsAndRetryNotExhausted() {
        OutboxEvent event = claimOneEvent();
        event.setAttemptCount(0);
        event.setMaxAttempts(3);

        when(outboxEventPort.findByEventId(event.getEventId())).thenReturn(Optional.of(event));
        when(outboxRetryPolicy.isExhausted(1, 3)).thenReturn(false);
        when(outboxRetryPolicy.nextRetryAt(1)).thenReturn(LocalDateTime.now().plusSeconds(10));
        doThrow(new RuntimeException("rabbit down")).when(rabbitOutboxPublisher).publish(event);

        outboxEventProcessor.processEvent(newOutboxEventWithEventId(event.getEventId(), OutboxStatus.NEW));

        assertEquals(OutboxStatus.FAILED, event.getStatus());
        assertEquals(1, event.getAttemptCount());
        assertNotNull(event.getNextRetryAt());
        assertNotNull(event.getLastError());
        assertEquals(null, event.getLockedBy());
    }

    @Test
    void shouldMarkAsDeadWhenRetryIsExhausted() {
        OutboxEvent event = claimOneEvent();
        event.setAttemptCount(0);
        event.setMaxAttempts(1);

        when(outboxEventPort.findByEventId(event.getEventId())).thenReturn(Optional.of(event));
        when(outboxRetryPolicy.isExhausted(1, 1)).thenReturn(true);
        doThrow(new RuntimeException("erro final")).when(rabbitOutboxPublisher).publish(event);

        outboxEventProcessor.processEvent(newOutboxEventWithEventId(event.getEventId(), OutboxStatus.NEW));

        assertEquals(OutboxStatus.DEAD, event.getStatus());
        assertEquals(1, event.getAttemptCount());
        assertNotNull(event.getLastError());
        assertEquals(null, event.getLockedBy());
    }

    private OutboxEvent claimOneEvent() {
        OutboxEvent event = newOutboxEvent(OutboxStatus.NEW);
        when(outboxEventPort.findPendingForRelay(anyList(), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(event));
        when(outboxEventPort.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        outboxEventProcessor.claimBatch(1);
        return event;
    }

    private OutboxEvent newOutboxEvent(OutboxStatus status) {
        return newOutboxEventWithEventId(UUID.randomUUID(), status);
    }

    private OutboxEvent newOutboxEventWithEventId(UUID eventId, OutboxStatus status) {
        return OutboxEvent.builder()
                .id(1L)
                .eventId(eventId)
                .correlationId(UUID.randomUUID())
                .exchangeName("humanizar.nucleo-relacionamento.event")
                .routingKey("ev.nucleo-relacionamento.test.v1")
                .aggregateType("nucleo-relacionamento")
                .aggregateId(UUID.randomUUID())
                .payload("{\"ok\":true}")
                .status(status)
                .attemptCount(0)
                .maxAttempts(3)
                .nextRetryAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
