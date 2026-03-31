package com.humanizar.nucleorelacionamento.infrastructure.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.humanizar.nucleorelacionamento.application.dto.BlockedNucleoDTO;
import com.humanizar.nucleorelacionamento.application.dto.DeleteCheckResponseDTO;
import com.humanizar.nucleorelacionamento.application.service.DeleteCheckService;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.infrastructure.controller.handler.NucleoRelacionamentoExceptionHandler;

@ExtendWith(MockitoExtension.class)
class DeleteCheckControllerTest {

    @Mock
    private DeleteCheckService deleteCheckService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CheckController controller = new CheckController(deleteCheckService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new NucleoRelacionamentoExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn200WhenDeleteIsAllowed() throws Exception {
        UUID patientId = UUID.fromString("2b0943f2-4054-4c89-8703-f31fc9e8c4ff");
        DeleteCheckResponseDTO response = new DeleteCheckResponseDTO(true, null, null, List.of());

        when(deleteCheckService.checkDeleteStatusByPatientId(patientId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/nucleo-relacionamento/check/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canDelete").value(true))
                .andExpect(jsonPath("$.blockedNucleos").isArray())
                .andExpect(jsonPath("$.blockedNucleos").isEmpty());

        verify(deleteCheckService).checkDeleteStatusByPatientId(patientId);
    }

    @Test
    void shouldReturn200WhenDeleteIsBlocked() throws Exception {
        UUID patientId = UUID.fromString("113ec4db-4fa4-42d7-8fac-2eb2497e6d6c");
        UUID nucleoId = UUID.fromString("e37f8844-e4d2-4a86-933d-9ec410bdb89f");
        DeleteCheckResponseDTO response = new DeleteCheckResponseDTO(
                false,
                ReasonCode.HAS_ABORDAGEM.name(),
                ReasonCode.HAS_ABORDAGEM.getMessage(),
                List.of(new BlockedNucleoDTO(nucleoId, 2)));

        when(deleteCheckService.checkDeleteStatusByPatientId(patientId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/nucleo-relacionamento/check/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canDelete").value(false))
                .andExpect(jsonPath("$.reasonCode").value("HAS_ABORDAGEM"))
                .andExpect(jsonPath("$.message").value(ReasonCode.HAS_ABORDAGEM.getMessage()))
                .andExpect(jsonPath("$.blockedNucleos[0].nucleoId").value(nucleoId.toString()))
                .andExpect(jsonPath("$.blockedNucleos[0].abordagemCount").value(2));

        verify(deleteCheckService).checkDeleteStatusByPatientId(patientId);
    }

    @Test
    void shouldMapValidationErrorViaExceptionHandler() throws Exception {
        UUID patientId = UUID.fromString("a926c95b-8967-43eb-b528-60051295f7cc");

        when(deleteCheckService.checkDeleteStatusByPatientId(patientId))
                .thenThrow(new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR,
                        null,
                        "patientId e obrigatorio"));

        mockMvc.perform(get("/api/v1/nucleo-relacionamento/check/{patientId}", patientId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.reasonCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("patientId e obrigatorio"))
                .andExpect(jsonPath("$.path").value("/api/v1/nucleo-relacionamento/check/" + patientId))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(deleteCheckService).checkDeleteStatusByPatientId(patientId);
    }
}
