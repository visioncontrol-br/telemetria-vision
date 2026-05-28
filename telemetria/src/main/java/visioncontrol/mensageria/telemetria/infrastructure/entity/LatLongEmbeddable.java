package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class LatLongEmbeddable {
    private Integer angulo;
    private Double latitude;
    private Double longitude;
    private String proximidade;
    private Double doubleLatitude;
    private Double doubleLongitude;
}