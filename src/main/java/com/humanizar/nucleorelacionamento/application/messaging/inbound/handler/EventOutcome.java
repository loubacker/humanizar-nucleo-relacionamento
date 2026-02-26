package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler;

import com.humanizar.nucleorelacionamento.domain.model.enums.ProcessedResult;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

public record EventOutcome(
        ProcessedResult result,
        ReasonCode reasonCode,
        String errorMessage,
        boolean retryable
) {

    public static EventOutcome success() {
        return new EventOutcome(ProcessedResult.SUCCESS, null, null, false);
    }

    public static EventOutcome ignored(ReasonCode reasonCode) {
        return new EventOutcome(ProcessedResult.IGNORED, reasonCode,
                reasonCode.name().toLowerCase(), false);
    }

    public static EventOutcome failed(ReasonCode reasonCode) {
        return new EventOutcome(ProcessedResult.FAILED, reasonCode,
                reasonCode.name().toLowerCase(), reasonCode.isRetryable());
    }

    public static EventOutcome failed(ReasonCode reasonCode, String detail) {
        return new EventOutcome(ProcessedResult.FAILED, reasonCode,
                detail, reasonCode.isRetryable());
    }
}
