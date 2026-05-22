package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rastreio", schema = "public")
public class RastreioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "webhook_id")
    private UUID webhookId;

    @Column(name = "id_tracking")
    private Long idTracking;

    @Column(name = "empresa_id", nullable = false)
    private Integer empresaId;

    private String plate;

    @Column(name = "date")
    private LocalDateTime date;

    private String event;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speed;

    @Column(name = "gps_valid")
    private Boolean gpsValid;

    private Boolean ignition;
    private BigDecimal odometer;

    @Column(name = "battery_voltage")
    private BigDecimal batteryVoltage;

    private String driver;
    private BigDecimal rpm;
    private BigDecimal tensao;

    @Column(name = "nivel_combustivel")
    private BigDecimal nivelCombustivel;

    private BigDecimal hodometro;
    private BigDecimal horimetro;

    @Column(name = "status_freio")
    private Boolean statusFreio;

    @Column(name = "evento_frenagem")
    private Boolean eventoFrenagem;

    @Column(name = "evento_aceleracao")
    private Boolean eventoAceleracao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> extra;

    @Column(name = "total_combustivel")
    private BigDecimal totalCombustivel;

    @Column(name = "temperatura_oleo")
    private BigDecimal temperaturaOleo;

    @Column(name = "posicao_pedal_acelerador")
    private BigDecimal posicaoPedalAcelerador;

    @Column(name = "posicao_pedal_freio")
    private BigDecimal posicaoPedalFreio;

    private Boolean loaded;
}