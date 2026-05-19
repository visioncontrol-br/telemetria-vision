package visioncontrol.mensageria.telemetria.business.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadRastreamentoDTO {

    private String date;
    private String event;
    private String plate;
    private BigDecimal speed;
    private Boolean gpsValid;
    private Boolean ignition;
    private BigDecimal odometer;
    private Long idTracking;
    private BigDecimal batteryVoltage;
    private LatLongDTO latLong;

    // Mapeia o objeto interno do JSON
    private TelemetriaInternalDTO telemetria;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LatLongDTO {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Data
    @Getter
    @Setter
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

        // Esse método faz o Jackson jogar tudo o que "sobrou" do JSON dentro do mapa!
        @JsonAnySetter
        public void setAny(String key, Object value) {
            this.any.put(key, value);
        }

        public Map<String, Object> getAny() {
            return this.any;
        }

    }

}
