package visioncontrol.mensageria.telemetria.business.consumer.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadRastreamentoDTO {

    @JsonDeserialize(using = StringDeserializer.class)
    private String date;
    // Confirmado como String
    private String event;
    private String plate;
    private BigDecimal speed;
    private Boolean gpsValid;
    private Boolean ignition;
    private BigDecimal odometer;
    private Long idTracking;
    private BigDecimal batteryVoltage;
    private String driver;
    private LatLongDTO latLong;
    private TelemetriaInternalDTO telemetria;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LatLongDTO {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelemetriaInternalDTO {
        private BigDecimal rpm;
        private BigDecimal tensao;
        private Boolean loaded;
        private BigDecimal hodometro;
        private BigDecimal horimetro;
        private Boolean statusFreio;
        private Boolean eventoFrenagem;
        private Boolean eventoAceleracao;
        private BigDecimal temperaturaOleo;
        private BigDecimal nivelCombustivel;
        private BigDecimal totalCombustivel;
        private BigDecimal posicaoPedalFreio;
        private BigDecimal posicaoPedalAcelerador;

        private Map<String, Object> any = new HashMap<>();

        @JsonAnySetter
        public void setAny(String key, Object value) {
            this.any.put(key, value);
        }

        public Map<String, Object> getAny() {
            return this.any;
        }
    }
}