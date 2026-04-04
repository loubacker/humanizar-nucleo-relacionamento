package com.humanizar.nucleorelacionamento.infrastructure.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.humanizar.nucleorelacionamento.application.dto.BlockedNucleoDTO;
import com.humanizar.nucleorelacionamento.application.dto.CheckResponseDTO;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoUpdatedDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoDTO;
import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.CallbackDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundNucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundNucleoResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundResponsavelDesvinculadoDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundResponsavelVinculadoDTO;
import com.humanizar.nucleorelacionamento.infrastructure.controller.dto.NucleoRelacionamentoErrorResponseDTO;

@Configuration
@ImportRuntimeHints(ObjectMapperConfig.ObjectMapperRuntimeHints.class)
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static class ObjectMapperRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            registerJsonBinding(hints, InboundEnvelopeDTO.class);
            registerJsonBinding(hints, NucleoPatientDTO.class);
            registerJsonBinding(hints, ResponsavelDTO.class);
            registerJsonBinding(hints, BlockedNucleoDTO.class);
            registerJsonBinding(hints, CheckResponseDTO.class);
            registerJsonBinding(hints, AcolhimentoCreatedDTO.class);
            registerJsonBinding(hints, AcolhimentoUpdatedDTO.class);
            registerJsonBinding(hints, AcolhimentoDeletedDTO.class);
            registerJsonBinding(hints, ProgramaDTO.class);
            registerJsonBinding(hints, ProgramaDeletedDTO.class);
            registerJsonBinding(hints, NucleoRelacionamentoDTO.class);
            registerJsonBinding(hints, NucleoRelacionamentoRetrieveDTO.class);
            registerJsonBinding(hints, NucleoRelacionamentoErrorResponseDTO.class);
            registerJsonBinding(hints, CallbackDTO.class);
            registerJsonBinding(hints, OutboundEnvelopeDTO.class);
            registerJsonBinding(hints, OutboundNucleoPatientDTO.class);
            registerJsonBinding(hints, OutboundNucleoResponsavelDTO.class);
            registerJsonBinding(hints, OutboundResponsavelVinculadoDTO.class);
            registerJsonBinding(hints, OutboundResponsavelDesvinculadoDTO.class);
        }

        private void registerJsonBinding(RuntimeHints hints, Class<?> type) {
            hints.reflection().registerType(type, MemberCategory.values());
        }
    }
}
