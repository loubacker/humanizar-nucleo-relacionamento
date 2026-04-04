package com.humanizar.nucleorelacionamento.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.BlockedNucleoDTO;
import com.humanizar.nucleorelacionamento.application.dto.CheckResponseDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class CheckMapper {

    public CheckResponseDTO toAllowed() {
        return new CheckResponseDTO(
                true,
                null,
                null,
                List.of());
    }

    public CheckResponseDTO toBlocked(List<BlockedNucleoDTO> blockedNucleos) {
        validateBlockedNucleos(blockedNucleos);
        return new CheckResponseDTO(
                false,
                ReasonCode.HAS_ABORDAGEM.name(),
                ReasonCode.HAS_ABORDAGEM.getMessage(),
                blockedNucleos);
    }

    private void validateBlockedNucleos(List<BlockedNucleoDTO> blockedNucleos) {
        if (blockedNucleos == null || blockedNucleos.isEmpty()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    null,
                    "blockedNucleos obrigatório quando canDelete=false");
        }

        for (BlockedNucleoDTO item : blockedNucleos) {
            if (item == null || item.nucleoId() == null) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR,
                        null,
                        "blockedNucleos.nucleoId obrigatorio");
            }
            if (item.abordagemCount() == null || item.abordagemCount() < 1) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR,
                        null,
                        "blockedNucleos.abordagemCount deve ser >= 1");
            }
        }
    }
}
