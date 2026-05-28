package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posicao_veiculo")
public class PosicoesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_evento")
    private LocalDateTime date;

    private String event;
    private String plate;
    private Integer speed;
    private String driver;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "angulo", column = @Column(name = "gps_angulo")),
            @AttributeOverride(name = "latitude", column = @Column(name = "gps_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "gps_longitude")),
            @AttributeOverride(name = "proximidade", column = @Column(name = "gps_proximidade")),
            @AttributeOverride(name = "doubleLatitude", column = @Column(name = "gps_double_latitude")),
            @AttributeOverride(name = "doubleLongitude", column = @Column(name = "gps_double_longitude"))
    })
    private LatLongEmbeddable latLong;

    private Boolean gpsValid;
    private Boolean ignition;
    private Long odometer;
    private Long idTracking;
    private String externalCode;
    private String safeAreaName;
    private Double batteryVoltage;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}