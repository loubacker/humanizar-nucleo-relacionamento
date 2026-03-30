package com.humanizar.nucleorelacionamento.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.humanizar.nucleorelacionamento.application.dto.BlockedNucleoDTO;
import com.humanizar.nucleorelacionamento.application.dto.DeleteCheckResponseDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class DeleteCheckMapperTest {

    private final DeleteCheckMapper mapper = new DeleteCheckMapper();

    @Test
    void toAllowedShouldReturnCanDeleteTrue() {
        DeleteCheckResponseDTO response = mapper.toAllowed();

        assertEquals(true, response.canDelete());
        assertEquals(null, response.reasonCode());
        assertEquals(null, response.message());
        assertEquals(List.of(), response.blockedNucleos());
    }

    @Test
    void toBlockedShouldReturnHasAbordagemPayload() {
        UUID nucleoId = UUID.randomUUID();
        List<BlockedNucleoDTO> blocked = List.of(new BlockedNucleoDTO(nucleoId, 2));

        DeleteCheckResponseDTO response = mapper.toBlocked(blocked);

        assertEquals(false, response.canDelete());
        assertEquals(ReasonCode.HAS_ABORDAGEM.name(), response.reasonCode());
        assertEquals(ReasonCode.HAS_ABORDAGEM.getMessage(), response.message());
        assertEquals(1, response.blockedNucleos().size());
        assertEquals(nucleoId, response.blockedNucleos().get(0).nucleoId());
        assertEquals(2, response.blockedNucleos().get(0).abordagemCount());
    }

    @Test
    void toBlockedShouldThrowWhenBlockedListIsNull() {
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toBlocked(null));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    @Test
    void toBlockedShouldThrowWhenBlockedListIsEmpty() {
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toBlocked(List.of()));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    @Test
    void toBlockedShouldThrowWhenAbordagemCountIsLessThanOne() {
        UUID nucleoId = UUID.randomUUID();
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toBlocked(List.of(new BlockedNucleoDTO(nucleoId, 0))));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }
}
