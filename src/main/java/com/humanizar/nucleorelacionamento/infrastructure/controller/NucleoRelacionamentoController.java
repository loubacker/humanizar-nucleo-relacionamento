package com.humanizar.nucleorelacionamento.infrastructure.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.application.service.NucleoRelacionamentoService;
import com.humanizar.nucleorelacionamento.infrastructure.config.ResilientMethodsConfig.Retry;

@RestController
@RequestMapping("/api/v1/nucleo-relacionamento")
public class NucleoRelacionamentoController {

    private static final Logger log = LoggerFactory.getLogger(NucleoRelacionamentoController.class);

    private final NucleoRelacionamentoService nucleoRelacionamentoService;

    public NucleoRelacionamentoController(NucleoRelacionamentoService nucleoRelacionamentoService) {
        this.nucleoRelacionamentoService = nucleoRelacionamentoService;
    }

    @Retry
    @GetMapping("/{patientId}")
    public ResponseEntity<NucleoRelacionamentoRetrieveDTO> retrieve(@PathVariable UUID patientId) {
        log.info("Recebido GET /api/v1/nucleo-relacionamento/{}. operacao=RETRIEVE", patientId);
        NucleoRelacionamentoRetrieveDTO response = nucleoRelacionamentoService.findByPatientId(patientId);
        int totalNucleos = response.nucleoPatient() != null ? response.nucleoPatient().size() : 0;
        log.info(
                "GET /api/v1/nucleo-relacionamento/{} concluido. totalNucleos={}",
                patientId,
                totalNucleos);
        return ResponseEntity.ok(response);
    }
}
