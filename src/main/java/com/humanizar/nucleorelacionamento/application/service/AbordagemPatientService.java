package com.humanizar.nucleorelacionamento.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoAbordagemCommand;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;

@Service
public class AbordagemPatientService {

    private static final Logger log = LoggerFactory.getLogger(AbordagemPatientService.class);

    private final AbordagemPatientPort abordagemPatientPort;
    private final NucleoPatientPort nucleoPatientPort;

    public AbordagemPatientService(AbordagemPatientPort abordagemPatientPort,
            NucleoPatientPort nucleoPatientPort) {
        this.abordagemPatientPort = abordagemPatientPort;
        this.nucleoPatientPort = nucleoPatientPort;
    }

    @Transactional
    public void createAbordagens(List<NucleoAbordagemCommand> nucleoAbordagemCommands, UUID correlationId) {
        String corrId = correlationId != null ? correlationId.toString() : null;

        List<AbordagemPatient> toSave = new ArrayList<>();

        for (NucleoAbordagemCommand nucleoAbordagemCommand : nucleoAbordagemCommands) {
            if (!nucleoPatientPort.existsById(nucleoAbordagemCommand.nucleoPatientId())) {
                throw new NucleoRelacionamentoException(ReasonCode.NUCLEO_PATIENT_NOT_FOUND, corrId,
                        "NucleoPatient nao encontrado: " + nucleoAbordagemCommand.nucleoPatientId());
            }

            List<AbordagemPatient> existing = abordagemPatientPort
                    .findByNucleoPatientId(nucleoAbordagemCommand.nucleoPatientId());
            Set<UUID> existingAbordagemIds = existing.stream()
                    .map(AbordagemPatient::getAbordagemId)
                    .collect(Collectors.toSet());

            for (UUID abordagemId : nucleoAbordagemCommand.abordagemId()) {
                if (existingAbordagemIds.contains(abordagemId)) {
                    throw new NucleoRelacionamentoException(ReasonCode.ABORDAGEM_DUPLICATED, corrId);
                }
                toSave.add(new AbordagemPatient(null, nucleoAbordagemCommand.nucleoPatientId(), abordagemId));
            }
        }

        if (!toSave.isEmpty()) {
            abordagemPatientPort.saveAll(toSave);
            log.info("Abordagens criadas. total={}, correlationId={}", toSave.size(), correlationId);
        }
    }

    @Transactional
    public void reconcileAbordagens(List<NucleoAbordagemCommand> nucleoAbordagemCommands, UUID correlationId) {
        String corrId = correlationId != null ? correlationId.toString() : null;

        for (NucleoAbordagemCommand nucleoAbordagemCommand : nucleoAbordagemCommands) {
            if (!nucleoPatientPort.existsById(nucleoAbordagemCommand.nucleoPatientId())) {
                throw new NucleoRelacionamentoException(ReasonCode.NUCLEO_PATIENT_NOT_FOUND, corrId,
                        "NucleoPatient nao encontrado: " + nucleoAbordagemCommand.nucleoPatientId());
            }

            List<AbordagemPatient> current = abordagemPatientPort
                    .findByNucleoPatientId(nucleoAbordagemCommand.nucleoPatientId());
            Set<UUID> currentIds = current.stream()
                    .map(AbordagemPatient::getAbordagemId)
                    .collect(Collectors.toSet());
            Set<UUID> incomingIds = Set.copyOf(nucleoAbordagemCommand.abordagemId());

            // Novas abordagens
            List<AbordagemPatient> toAdd = incomingIds.stream()
                    .filter(id -> !currentIds.contains(id))
                    .map(id -> new AbordagemPatient(null, nucleoAbordagemCommand.nucleoPatientId(), id))
                    .toList();
            if (!toAdd.isEmpty()) {
                abordagemPatientPort.saveAll(toAdd);
            }

            // Abordagens removidas — delete all e re-save as que ficam
            boolean hasRemovals = currentIds.stream().anyMatch(id -> !incomingIds.contains(id));
            if (hasRemovals) {
                abordagemPatientPort.deleteByNucleoPatientId(nucleoAbordagemCommand.nucleoPatientId());
                List<AbordagemPatient> remaining = incomingIds.stream()
                        .map(id -> new AbordagemPatient(null, nucleoAbordagemCommand.nucleoPatientId(), id))
                        .toList();
                if (!remaining.isEmpty()) {
                    abordagemPatientPort.saveAll(remaining);
                }
            }
        }

        log.info("Abordagens reconciliadas. groups={}, correlationId={}", nucleoAbordagemCommands.size(),
                correlationId);
    }

    @Transactional
    public void deleteAllAbordagensByPatientId(UUID patientId, UUID correlationId) {
        List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);
        int totalDeleted = 0;

        for (NucleoPatient nucleo : nucleos) {
            List<AbordagemPatient> abordagens = abordagemPatientPort.findByNucleoPatientId(nucleo.getId());
            if (!abordagens.isEmpty()) {
                abordagemPatientPort.deleteByNucleoPatientId(nucleo.getId());
                totalDeleted += abordagens.size();
            }
        }

        log.info("Abordagens removidas para patientId={}. total={}, correlationId={}",
                patientId, totalDeleted, correlationId);
    }

}
