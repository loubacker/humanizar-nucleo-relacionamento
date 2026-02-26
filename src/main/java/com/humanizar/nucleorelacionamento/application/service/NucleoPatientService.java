package com.humanizar.nucleorelacionamento.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.ResponsavelCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoPatientCommand;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.event.NucleoPatientPayload;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.event.ResponsavelDesvinculadoEvent;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.event.ResponsavelPayload;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.event.ResponsavelVinculadoEvent;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.OutboxEventPublisher;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.model.enums.ResponsavelRole;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientResponsavelPort;

@Service
public class NucleoPatientService {

        private static final Logger log = LoggerFactory.getLogger(NucleoPatientService.class);

        private final NucleoPatientPort nucleoPatientPort;
        private final NucleoPatientResponsavelPort responsavelPort;
        private final AbordagemPatientPort abordagemPatientPort;
        private final OutboxEventPublisher outboxEventPublisher;

        public NucleoPatientService(NucleoPatientPort nucleoPatientPort,
                        NucleoPatientResponsavelPort responsavelPort,
                        AbordagemPatientPort abordagemPatientPort,
                        OutboxEventPublisher outboxEventPublisher) {
                this.nucleoPatientPort = nucleoPatientPort;
                this.responsavelPort = responsavelPort;
                this.abordagemPatientPort = abordagemPatientPort;
                this.outboxEventPublisher = outboxEventPublisher;
        }

        @Transactional
        public void createNucleoPatient(UUID patientId, UUID nucleoId,
                        List<ResponsavelCommand> responsaveis,
                        UUID correlationId,
                        UUID actorId, String userAgent, String originIp) {
                String corrId = correlationId != null ? correlationId.toString() : null;

                if (responsaveis == null || responsaveis.isEmpty()) {
                        throw new NucleoRelacionamentoException(ReasonCode.RESPONSAVEL_REQUIRED, corrId);
                }

                if (nucleoPatientPort.findByPatientIdAndNucleoId(patientId, nucleoId).isPresent()) {
                        log.info("NucleoPatient ja existe para patientId={}, nucleoId={}. No-op.", patientId, nucleoId);
                        return;
                }

                NucleoPatient nucleo = NucleoPatient.builder()
                                .patientId(patientId)
                                .nucleoId(nucleoId)
                                .build();
                NucleoPatient saved = nucleoPatientPort.save(nucleo);

                List<NucleoPatientResponsavel> responsaveisDomain = responsaveis.stream()
                                .map(responsavelCommand -> new NucleoPatientResponsavel(
                                                null, saved.getId(), responsavelCommand.responsavelId(),
                                                parseRole(responsavelCommand.role(), corrId)))
                                .toList();
                responsavelPort.saveAll(responsaveisDomain);

                publishVinculado(patientId, nucleoId, responsaveisDomain, saved.getId(), correlationId,
                                actorId, userAgent, originIp);

                log.info("NucleoPatient criado. id={}, patientId={}, nucleoId={}, responsaveis={}",
                                saved.getId(), patientId, nucleoId, responsaveisDomain.size());
        }

        @Transactional
        public void reconcileNucleoPatients(UUID patientId, List<NucleoPatientCommand> incomingNucleoCommands,
                        UUID correlationId,
                        UUID actorId, String userAgent, String originIp) {
                String corrId = correlationId != null ? correlationId.toString() : null;

                List<NucleoPatient> currentNucleos = nucleoPatientPort.findAllByPatientId(patientId);
                Set<UUID> incomingNucleoIds = incomingNucleoCommands.stream()
                                .map(NucleoPatientCommand::nucleoId)
                                .collect(Collectors.toSet());
                Map<UUID, NucleoPatient> currentByNucleoId = currentNucleos.stream()
                                .collect(Collectors.toMap(NucleoPatient::getNucleoId, n -> n));

                for (NucleoPatient current : currentNucleos) {
                        if (!incomingNucleoIds.contains(current.getNucleoId())) {
                                deleteNucleo(current, patientId, correlationId,
                                                actorId, userAgent, originIp, corrId);
                        }
                }

                for (NucleoPatientCommand incomingNucleoCommand : incomingNucleoCommands) {
                        NucleoPatient existing = currentByNucleoId.get(incomingNucleoCommand.nucleoId());
                        if (existing == null) {
                                createNucleoPatient(patientId, incomingNucleoCommand.nucleoId(),
                                                incomingNucleoCommand.nucleoPatientResponsavel(), correlationId,
                                                actorId, userAgent, originIp);
                        } else {
                                reconcileResponsaveis(existing, incomingNucleoCommand.nucleoPatientResponsavel(),
                                                patientId, correlationId,
                                                actorId, userAgent, originIp);
                        }
                }
        }

        @Transactional
        public void deleteAllNucleosByPatientId(UUID patientId, UUID correlationId,
                        UUID actorId, String userAgent, String originIp) {
                String corrId = correlationId != null ? correlationId.toString() : null;
                List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);

                for (NucleoPatient nucleo : nucleos) {
                        deleteNucleo(nucleo, patientId, correlationId,
                                        actorId, userAgent, originIp, corrId);
                }

                log.info("Todos nucleos removidos para patientId={}. total={}", patientId, nucleos.size());
        }

        private void deleteNucleo(NucleoPatient nucleo, UUID patientId,
                        UUID correlationId,
                        UUID actorId, String userAgent, String originIp,
                        String corrId) {
                if (!abordagemPatientPort.findByNucleoPatientId(nucleo.getId()).isEmpty()) {
                        throw new NucleoRelacionamentoException(ReasonCode.HAS_ABORDAGEM, corrId);
                }

                List<NucleoPatientResponsavel> responsaveis = responsavelPort.findByNucleoPatientId(nucleo.getId());
                responsavelPort.deleteByNucleoPatientId(nucleo.getId());
                nucleoPatientPort.deleteByPatientIdAndNucleoId(patientId, nucleo.getNucleoId());

                if (!responsaveis.isEmpty()) {
                        publishDesvinculado(patientId, nucleo.getNucleoId(), responsaveis,
                                        nucleo.getId(), correlationId,
                                        actorId, userAgent, originIp);
                }
        }

        private void reconcileResponsaveis(NucleoPatient existing,
                        List<ResponsavelCommand> incomingResponsaveis,
                        UUID patientId, UUID correlationId,
                        UUID actorId, String userAgent, String originIp) {
                List<NucleoPatientResponsavel> currentResponsaveis = responsavelPort
                                .findByNucleoPatientId(existing.getId());

                Set<UUID> currentIds = currentResponsaveis.stream()
                                .map(NucleoPatientResponsavel::getResponsavelId)
                                .collect(Collectors.toSet());
                Set<UUID> incomingIds = incomingResponsaveis.stream()
                                .map(ResponsavelCommand::responsavelId)
                                .collect(Collectors.toSet());

                String corrId = correlationId != null ? correlationId.toString() : null;

                List<NucleoPatientResponsavel> added = incomingResponsaveis.stream()
                                .filter(responsavelCommand -> !currentIds.contains(responsavelCommand.responsavelId()))
                                .map(responsavelCommand -> new NucleoPatientResponsavel(
                                                null, existing.getId(), responsavelCommand.responsavelId(),
                                                parseRole(responsavelCommand.role(), corrId)))
                                .toList();

                List<NucleoPatientResponsavel> removed = currentResponsaveis.stream()
                                .filter(r -> !incomingIds.contains(r.getResponsavelId()))
                                .toList();

                if (!added.isEmpty() || !removed.isEmpty()) {
                        // Regrava o estado final para garantir consistencia quando houver add/remove no
                        // mesmo update.
                        responsavelPort.deleteByNucleoPatientId(existing.getId());
                        List<NucleoPatientResponsavel> finalState = incomingResponsaveis.stream()
                                        .map(responsavelCommand -> new NucleoPatientResponsavel(
                                                        null, existing.getId(), responsavelCommand.responsavelId(),
                                                        parseRole(responsavelCommand.role(), corrId)))
                                        .toList();
                        responsavelPort.saveAll(finalState);
                }

                if (!added.isEmpty()) {
                        publishVinculado(patientId, existing.getNucleoId(), added,
                                        existing.getId(), correlationId,
                                        actorId, userAgent, originIp);
                }

                if (!removed.isEmpty()) {
                        publishDesvinculado(patientId, existing.getNucleoId(), removed,
                                        existing.getId(), correlationId,
                                        actorId, userAgent, originIp);
                }
        }

        private void publishVinculado(UUID patientId, UUID nucleoId,
                        List<NucleoPatientResponsavel> responsaveis,
                        UUID aggregateId, UUID correlationId,
                        UUID actorId, String userAgent, String originIp) {
                List<ResponsavelPayload> payloads = responsaveis.stream()
                                .map(r -> new ResponsavelPayload(r.getResponsavelId(), r.getRole().name()))
                                .toList();
                NucleoPatientPayload nucleoPayload = new NucleoPatientPayload(nucleoId, payloads);
                ResponsavelVinculadoEvent event = new ResponsavelVinculadoEvent(
                                patientId, List.of(nucleoPayload));

                outboxEventPublisher.publish(RoutingKeyCatalog.RESPONSAVEL_VINCULADO_V1,
                                aggregateId, correlationId, event,
                                actorId, userAgent, originIp);
        }

        private ResponsavelRole parseRole(String role, String correlationId) {
                try {
                        return ResponsavelRole.valueOf(role);
                } catch (IllegalArgumentException ex) {
                        throw new NucleoRelacionamentoException(
                                        ReasonCode.VALIDATION_ERROR, correlationId,
                                        "Role invalida: " + role);
                }
        }

        private void publishDesvinculado(UUID patientId, UUID nucleoId,
                        List<NucleoPatientResponsavel> responsaveis,
                        UUID aggregateId, UUID correlationId,
                        UUID actorId, String userAgent, String originIp) {
                List<ResponsavelPayload> payloads = responsaveis.stream()
                                .map(r -> new ResponsavelPayload(r.getResponsavelId(), r.getRole().name()))
                                .toList();
                NucleoPatientPayload nucleoPayload = new NucleoPatientPayload(nucleoId, payloads);
                ResponsavelDesvinculadoEvent event = new ResponsavelDesvinculadoEvent(
                                patientId, List.of(nucleoPayload));

                outboxEventPublisher.publish(RoutingKeyCatalog.RESPONSAVEL_DESVINCULADO_V1,
                                aggregateId, correlationId, event,
                                actorId, userAgent, originIp);
        }
}
