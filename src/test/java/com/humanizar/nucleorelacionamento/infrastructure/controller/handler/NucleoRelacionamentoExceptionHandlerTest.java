package com.humanizar.nucleorelacionamento.infrastructure.controller.handler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class NucleoRelacionamentoExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new NucleoRelacionamentoExceptionHandler())
                .build();
    }

    @Test
    void shouldMapNucleoRelacionamentoExceptionToStandardJson() throws Exception {
        mockMvc.perform(get("/test/advice"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.reasonCode").value("HAS_ABORDAGEM"))
                .andExpect(jsonPath("$.message").value("Nao permitido remover nucleo"))
                .andExpect(jsonPath("$.path").value("/test/advice"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFallbackToValidationErrorWhenReasonCodeIsNull() throws Exception {
        mockMvc.perform(get("/test/advice/fallback"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.reasonCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Falha sem reason code"))
                .andExpect(jsonPath("$.path").value("/test/advice/fallback"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/advice")
        @SuppressWarnings("unused")
        String fail() {
            throw new NucleoRelacionamentoException(
                    ReasonCode.HAS_ABORDAGEM,
                    null,
                    "Nao permitido remover nucleo");
        }

        @GetMapping("/test/advice/fallback")
        @SuppressWarnings("unused")
        String failFallback() {
            throw new NucleoRelacionamentoException(
                    null,
                    null,
                    "Falha sem reason code");
        }
    }
}
