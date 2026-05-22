package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "posicoes_claud")
@Getter
@Setter
public class PosicoesClaudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private Integer empresaId;

    @Column(nullable = false)
    private String plate;

    private String event;

    @Column(name = "gps_valid")
    private Boolean gpsValid;

    @Column(name = "id_tracking")
    private Long idTracking;

    private Integer ignition;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speed;

    @Column(name = "battery_voltage")
    private BigDecimal batteryVoltage;

    private BigDecimal odometer;
    private OffsetDateTime date;

    @Column(name = "inserted_at", insertable = false, updatable = false)
    private OffsetDateTime insertedAt;

}