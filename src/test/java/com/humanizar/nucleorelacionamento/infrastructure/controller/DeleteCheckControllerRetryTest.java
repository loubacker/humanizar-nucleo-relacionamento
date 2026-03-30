package com.humanizar.nucleorelacionamento.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.humanizar.nucleorelacionamento.application.dto.DeleteCheckResponseDTO;
import com.humanizar.nucleorelacionamento.application.service.DeleteCheckService;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DeleteCheckControllerRetryTest.TestConfig.class)
class DeleteCheckControllerRetryTest {

    @Configuration(proxyBeanMethods = false)
    @EnableResilientMethods
    static class TestConfig {

        @Bean
        DeleteCheckService deleteCheckService() {
            return mock(DeleteCheckService.class);
        }

        @Bean
        CheckController deleteCheckController(DeleteCheckService service) {
            return new CheckController(service);
        }
    }

    @Autowired
    private CheckController controller;

    @Autowired
    private DeleteCheckService service;

    @Test
    void shouldReturn200WithoutRetryWhenSuccessOnFirstAttempt() {
        UUID patientId = UUID.fromString("1ec11c04-3453-4a8c-b174-dac37a3236cf");
        DeleteCheckResponseDTO payload = allowedPayload();

        when(service.checkDeleteStatusByPatientId(patientId)).thenReturn(payload);

        ResponseEntity<DeleteCheckResponseDTO> response = controller.checkDeleteStatus(patientId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(payload, response.getBody());
        verify(service, times(1)).checkDeleteStatusByPatientId(patientId);
    }

    @Test
    void shouldRetryTransientFailureAndSucceedOnThirdAttempt() {
        UUID patientId = UUID.fromString("23751f6b-c38f-473a-a32d-af4ebf1fbcdf");
        DeleteCheckResponseDTO payload = allowedPayload();

        when(service.checkDeleteStatusByPatientId(patientId))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 1"))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 2"))
                .thenReturn(payload);

        ResponseEntity<DeleteCheckResponseDTO> response = controller.checkDeleteStatus(patientId);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().canDelete());
        verify(service, times(3)).checkDeleteStatusByPatientId(patientId);
    }

    @Test
    void shouldNotRetryForDomainException() {
        UUID patientId = UUID.fromString("91f7868f-2408-4132-9c79-8c2ecbc6fe96");
        NucleoRelacionamentoException exception = new NucleoRelacionamentoException(
                ReasonCode.VALIDATION_ERROR,
                null,
                "patientId e obrigatorio");

        when(service.checkDeleteStatusByPatientId(patientId)).thenThrow(exception);

        NucleoRelacionamentoException thrown = assertThrows(
                NucleoRelacionamentoException.class,
                () -> controller.checkDeleteStatus(patientId));

        assertEquals(ReasonCode.VALIDATION_ERROR, thrown.getReasonCode());
        verify(service, times(1)).checkDeleteStatusByPatientId(patientId);
    }

    private DeleteCheckResponseDTO allowedPayload() {
        return new DeleteCheckResponseDTO(true, null, null, List.of());
    }
}
