package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "veiculos", schema = "public")
public class VeiculosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @Column(name = "placa", nullable = false)
    private String placa;

    @Column(name = "numero_frota")
    private String numeroFrota;

    @Column(name = "categoria")
    private String categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id")
    private PerfilEntity motorista;

    @Column(name = "status")
    private String status;

    @Column(name = "consumo_km_l")
    private BigDecimal consumoKmL;

    @Column(name = "vel_max_kmh")
    private BigDecimal velMaxKmh;

    @Column(name = "combustivel_pct")
    private Integer combustivelPct;

    @Column(name = "combustivel_gasto_l")
    private BigDecimal combustivelGastoL;

    @Column(name = "km_rodados")
    private BigDecimal kmRodados;

    @Column(name = "telemetria")
    private Boolean telemetria;

    @Column(name = "meta_verde")
    private BigDecimal metaVerde;

    @Column(name = "meta_amarelo")
    private BigDecimal metaAmarelo;

    @Column(name = "meta_vermelho")
    private BigDecimal metaVermelho;

    @Column(name = "capacidade_tanque_litros")
    private BigDecimal capacidadeTanqueLitros;

    @Column(name = "litragem_cam")
    private Boolean litragemCam;

    @Column(name = "combustivel_divisor")
    private BigDecimal combustivelDivisor;

    @Column(name = "criado_em", updatable = false, insertable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false)
    private Instant atualizadoEm;

}
