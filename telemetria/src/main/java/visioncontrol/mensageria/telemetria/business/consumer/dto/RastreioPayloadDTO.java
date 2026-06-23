package visioncontrol.mensageria.telemetria.business.consumer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record RastreioPayloadDTO(
        // Dados Básicos
        String plate,
        String date,
        String event,
        @JsonProperty("id_tracking") Long idTracking,
        @JsonProperty("external_code") String externalCode,
        @JsonProperty("safe_area_name") String safeAreaName,

        // Posição
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal speed,
        Integer angulo,
        String proximidade,
        @JsonProperty("double_latitude") BigDecimal doubleLatitude,
        @JsonProperty("double_longitude") BigDecimal doubleLongitude,
        @JsonProperty("gps_valid") Boolean gpsValid,

        // Diagnóstico e Sensores
        Boolean ignition,
        BigDecimal rpm,
        BigDecimal hodometro,
        BigDecimal horimetro,
        @JsonProperty("battery_voltage") BigDecimal batteryVoltage,
        @JsonProperty("tel_luz") String telLuz,
        @JsonProperty("tel_chuva") Integer telChuva,
        @JsonProperty("tel_marcha") String telMarcha,
        @JsonProperty("tel_portas") Boolean telPortas,
        @JsonProperty("tel_carga_bateria") Integer telCargaBateria,
        @JsonProperty("tel_temperatura_cabine") Integer telTemperaturaCabine,
        @JsonProperty("tel_temperatura_externa") Integer telTemperaturaExterna,
        @JsonProperty("tel_temperatura_arrefecimento") Integer telTemperaturaArrefecimento,
        @JsonProperty("tel_nivel_combustivel") BigDecimal nivelCombustivel,
        @JsonProperty("tel_total_combustivel") BigDecimal totalCombustivel,
        @JsonProperty("tel_temperatura_oleo") BigDecimal temperaturaOleo,
        @JsonProperty("tel_peso_segundo_eixo") Integer telPesoSegundoEixo,
        @JsonProperty("tel_peso_terceiro_eixo") Integer telPesoTerceiroEixo,

        // Comportamento
        @JsonProperty("tel_status_freio") Boolean statusFreio,
        @JsonProperty("tel_posicao_pedal_freio") BigDecimal posicaoPedalFreio,
        @JsonProperty("tel_posicao_pedal_acelerador") BigDecimal posicaoPedalAcelerador,
        @JsonProperty("tel_evento_frenagem") Boolean telEventoFrenagem,
        @JsonProperty("tel_evento_aceleracao") Boolean telEventoAceleracao,
        @JsonProperty("tel_evento_rpm_in") Boolean telEventoRpmIn,
        @JsonProperty("tel_evento_rpm_out") Boolean telEventoRpmOut,
        @JsonProperty("tel_evento_speed_in") Boolean telEventoSpeedIn,
        @JsonProperty("tel_evento_speed_out") Boolean telEventoSpeedOut,
        @JsonProperty("tel_evento_ocioso_in") Boolean telEventoOciosoIn,
        @JsonProperty("tel_evento_ocioso_out") Boolean telEventoOciosoOut,
        @JsonProperty("tel_evento_banguela_in") Boolean telEventoBanguelaIn,
        @JsonProperty("tel_evento_banguela_out") Boolean telEventoBanguelaOut,
        @JsonProperty("tel_evento_chuva_speed_in") Boolean telEventoChuvaSpeedIn,
        @JsonProperty("tel_evento_chuva_speed_out") Boolean telEventoChuvaSpeedOut
) {}