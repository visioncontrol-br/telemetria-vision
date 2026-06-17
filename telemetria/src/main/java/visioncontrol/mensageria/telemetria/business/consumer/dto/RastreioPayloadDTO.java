package visioncontrol.mensageria.telemetria.business.consumer.dto;

import java.math.BigDecimal;

    public record RastreioPayloadDTO(
            String event,
            Boolean gpsValid,
            Long idTracking,
            Integer ignition,
            LatLongDTO latLong,
            BigDecimal speed,
            BigDecimal batteryVoltage,
            BigDecimal odometer,
            String plate,
            String externalCode,
            String safeAreaName,
            String date
    ) {
        public record LatLongDTO(
                BigDecimal latitude,
                BigDecimal longitude
        ) {}

}
