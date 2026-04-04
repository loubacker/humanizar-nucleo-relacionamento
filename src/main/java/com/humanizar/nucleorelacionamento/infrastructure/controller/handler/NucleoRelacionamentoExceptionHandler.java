package com.humanizar.nucleorelacionamento.infrastructure.controller.handler;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.infrastructure.controller.dto.NucleoRelacionamentoErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class NucleoRelacionamentoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(NucleoRelacionamentoExceptionHandler.class);

    @ExceptionHandler(NucleoRelacionamentoException.class)
    public ResponseEntity<NucleoRelacionamentoErrorResponseDTO> handleNucleoRelacionamentoException(
            NucleoRelacionamentoException exception,
            HttpServletRequest request) {
        ReasonCode reasonCode = exception.getReasonCode() != null
                ? exception.getReasonCode()
                : ReasonCode.VALIDATION_ERROR;
        int status = reasonCode.getStatusCode();
        String path = request != null ? request.getRequestURI() : null;

        logException(status, reasonCode.name(), path, exception);

        NucleoRelacionamentoErrorResponseDTO body = new NucleoRelacionamentoErrorResponseDTO(
                status,
                reasonCode.name(),
                exception.getMessage(),
                path,
                OffsetDateTime.now());

        return ResponseEntity.status(resolveStatus(status)).body(body);
    }

    private HttpStatus resolveStatus(int statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private void logException(
            int status,
            String reasonCode,
            String path,
            NucleoRelacionamentoException exception) {
        String message = "Erro no processamento HTTP. reasonCode={}, status={}, path={}, causa={}";
        String rootCauseMessage = rootCauseMessage(exception);

        if (status >= 500) {
            log.error(message, reasonCode, status, path, rootCauseMessage, exception);
            return;
        }

        log.warn(message, reasonCode, status, path, rootCauseMessage);
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current != null ? current.getMessage() : null;
    }
}
