package com.humanizar.nucleorelacionamento.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.application.service.NucleoRelacionamentoService;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = NucleoRelacionamentoControllerRetryTest.TestConfig.class)
class NucleoRelacionamentoControllerRetryTest {

    @Configuration(proxyBeanMethods = false)
    @EnableResilientMethods
    static class TestConfig {

        @Bean
        @SuppressWarnings("unused")
        NucleoRelacionamentoService nucleoRelacionamentoService() {
            return mock(NucleoRelacionamentoService.class);
        }

        @Bean
        @SuppressWarnings("unused")
        NucleoRelacionamentoController nucleoRelacionamentoController(NucleoRelacionamentoService service) {
            return new NucleoRelacionamentoController(service);
        }
    }

    @Autowired
    private NucleoRelacionamentoController controller;

    @Autowired
    private NucleoRelacionamentoService service;

    @Test
    void shouldReturn200WithoutRetryWhenSuccessOnFirstAttempt() {
        UUID patientId = UUID.fromString("1ec11c04-3453-4a8c-b174-dac37a3236cf");
        NucleoRelacionamentoRetrieveDTO payload = retrievePayload(patientId);

        when(service.findByPatientId(patientId)).thenReturn(payload);

        ResponseEntity<NucleoRelacionamentoRetrieveDTO> response = controller.retrieve(patientId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(payload, response.getBody());
        verify(service, times(1)).findByPatientId(patientId);
    }

    @Test
    void shouldRetryTransientFailureAndSucceedOnThirdAttempt() {
        UUID patientId = UUID.fromString("23751f6b-c38f-473a-a32d-af4ebf1fbcdf");
        NucleoRelacionamentoRetrieveDTO payload = retrievePayload(patientId);

        when(service.findByPatientId(patientId))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 1"))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 2"))
                .thenReturn(payload);

        ResponseEntity<NucleoRelacionamentoRetrieveDTO> response = controller.retrieve(patientId);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().nucleoPatient().isEmpty());
        verify(service, times(3)).findByPatientId(patientId);
    }

    @Test
    void shouldNotRetryForDomainException() {
        UUID patientId = UUID.fromString("91f7868f-2408-4132-9c79-8c2ecbc6fe96");
        NucleoRelacionamentoException exception = new NucleoRelacionamentoException(
                ReasonCode.PATIENT_NOT_FOUND,
                null);

        when(service.findByPatientId(patientId)).thenThrow(exception);

        NucleoRelacionamentoException thrown = assertThrows(
                NucleoRelacionamentoException.class,
                () -> controller.retrieve(patientId));

        assertEquals(ReasonCode.PATIENT_NOT_FOUND, thrown.getReasonCode());
        verify(service, times(1)).findByPatientId(patientId);
    }

    private NucleoRelacionamentoRetrieveDTO retrievePayload(UUID patientId) {
        return new NucleoRelacionamentoRetrieveDTO(patientId, List.of());
    }
}
