package com.humanizar.nucleorelacionamento.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoDTO;
import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.application.service.NucleoRelacionamentoService;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.infrastructure.controller.handler.NucleoRelacionamentoExceptionHandler;

@ExtendWith(MockitoExtension.class)
class NucleoRelacionamentoControllerTest {

    @Mock
    private NucleoRelacionamentoService nucleoRelacionamentoService;

    private MockMvc mockMvc;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        NucleoRelacionamentoController controller = new NucleoRelacionamentoController(nucleoRelacionamentoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new NucleoRelacionamentoExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn200WhenRetrieveSucceeds() throws Exception {
        UUID patientId = UUID.fromString("cbfe7dd5-5694-46d3-91fa-e62b4771ad6e");
        UUID nucleoPatientId = UUID.fromString("c3c61bf7-6b0e-4b6b-8eb4-f3a44c66c84f");
        UUID nucleoId = UUID.fromString("46be7d99-e696-463c-86e1-0572a2378787");
        UUID responsavelId = UUID.fromString("99999999-8888-7777-6666-555555555555");
        UUID abordagemId = UUID.fromString("abababab-1111-2222-3333-444444444444");

        NucleoRelacionamentoRetrieveDTO response = new NucleoRelacionamentoRetrieveDTO(
                patientId,
                List.of(new NucleoRelacionamentoDTO(
                        nucleoPatientId,
                        nucleoId,
                        List.of(new ResponsavelDTO(responsavelId, "COORDENADOR")),
                        List.of(abordagemId))));

        when(nucleoRelacionamentoService.findByPatientId(patientId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/nucleo-relacionamento/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.nucleoPatient[0].nucleoPatientId").value(nucleoPatientId.toString()))
                .andExpect(jsonPath("$.nucleoPatient[0].nucleoId").value(nucleoId.toString()))
                .andExpect(jsonPath("$.nucleoPatient[0].nucleoPatientResponsavel[0].responsavelId")
                        .value(responsavelId.toString()))
                .andExpect(jsonPath("$.nucleoPatient[0].nucleoPatientResponsavel[0].role")
                        .value("COORDENADOR"))
                .andExpect(jsonPath("$.nucleoPatient[0].abordagemId[0]").value(abordagemId.toString()));

        verify(nucleoRelacionamentoService).findByPatientId(patientId);
    }

    @Test
    void shouldReturn404WhenPatientHasNoLocalBindings() throws Exception {
        UUID patientId = UUID.fromString("cbfe7dd5-5694-46d3-91fa-e62b4771ad6e");

        when(nucleoRelacionamentoService.findByPatientId(patientId))
                .thenThrow(new NucleoRelacionamentoException(ReasonCode.PATIENT_NOT_FOUND, null));

        mockMvc.perform(get("/api/v1/nucleo-relacionamento/{patientId}", patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.reasonCode").value("PATIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ReasonCode.PATIENT_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.path").value("/api/v1/nucleo-relacionamento/" + patientId))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(nucleoRelacionamentoService).findByPatientId(patientId);
    }
}
