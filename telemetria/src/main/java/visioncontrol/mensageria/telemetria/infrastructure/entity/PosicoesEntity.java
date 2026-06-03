package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "posicao_veiculo", schema = "public")
public class PosicoesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "data_evento")
    private LocalDateTime dataEvento;

    @Column(name = "empresa_id")
    private Integer empresaId;

    @Column(name = "veiculo_id")
    private Integer veiculoId;

    @Column(name = "event")
    private String event;

    @Column(name = "plate")
    private String plate;

    @Column(name = "speed")
    private Integer speed;

    @Column(name = "driver")
    private String driver;

    @Column(name = "gps_valid")
    private Boolean gpsValid;

    @Column(name = "ignition")
    private Boolean ignition;

    @Column(name = "odometer")
    private Long odometer;

    @Column(name = "id_tracking")
    private Long idTracking;

    @Column(name = "battery_voltage")
    private Double batteryVoltage;

    @Column(name = "gps_angulo")
    private Integer angulo;

    @Column(name = "gps_latitude")
    private Double latitude;

    @Column(name = "gps_longitude")
    private Double longitude;

    @Column(name = "gps_proximidade")
    private String proximidade;

    @Column(name = "gps_double_latitude")
    private Double doubleLatitude;

    @Column(name = "gps_double_longitude")
    private Double doubleLongitude;

    @Column(name = "external_code")
    private String externalCode;

    @Column(name = "safe_area_name")
    private String safeAreaName;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }
}