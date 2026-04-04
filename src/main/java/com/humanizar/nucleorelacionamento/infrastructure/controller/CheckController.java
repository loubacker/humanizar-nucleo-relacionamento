package com.humanizar.nucleorelacionamento.infrastructure.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.nucleorelacionamento.application.dto.CheckResponseDTO;
import com.humanizar.nucleorelacionamento.application.service.CheckService;
import com.humanizar.nucleorelacionamento.infrastructure.config.ResilientMethodsConfig.Retry;

@RestController
@RequestMapping("/api/v1/nucleo-relacionamento")
public class CheckController {

    private static final Logger log = LoggerFactory.getLogger(CheckController.class);

    private final CheckService checkService;

    public CheckController(CheckService checkService) {
        this.checkService = checkService;
    }

    @Retry
    @GetMapping("/check/{patientId}")
    public ResponseEntity<CheckResponseDTO> checkDeleteStatus(@PathVariable UUID patientId) {
        log.info("Recebido GET /api/v1/nucleo-relacionamento/check/{}. operacao=DELETE_CHECK", patientId);
        CheckResponseDTO response = checkService.checkDeleteStatusByPatientId(patientId);
        log.info(
                "GET /api/v1/nucleo-relacionamento/check/{} concluido. canDelete={}, blockedNucleos={}",
                patientId,
                response.canDelete(),
                response.blockedNucleos() != null ? response.blockedNucleos().size() : 0);
        return ResponseEntity.ok(response);
    }
}
