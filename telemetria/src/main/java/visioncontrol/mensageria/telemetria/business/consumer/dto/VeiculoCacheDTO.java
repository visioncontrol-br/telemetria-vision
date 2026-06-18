package visioncontrol.mensageria.telemetria.business.consumer.dto;

import java.util.UUID;

public record VeiculoCacheDTO(
        UUID id,
        UUID empresaId,
        UUID motoristaId,
        String placaText,
        String motoristaNome
) {
}
