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
@Table(name = "posicoes", schema = "public")
public class PosicoesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_evento")
    private LocalDateTime date;

    @Column(name = "empresa_id")
    private Integer empresaId;

    @Column(name = "veiculo_id")
    private Integer veiculoId;

    private String event;
    private String plate;
    private Integer speed;
    private String driver;

    @Embedded
    private LatLongEmbeddable latLong;

    @Column(name = "gps_valid")
    private Boolean gpsValid;

    private Boolean ignition;
    private Long odometer;

    @Column(name = "id_tracking")
    private Long idTracking;

    @Column(name = "battery_voltage")
    private Double batteryVoltage;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}