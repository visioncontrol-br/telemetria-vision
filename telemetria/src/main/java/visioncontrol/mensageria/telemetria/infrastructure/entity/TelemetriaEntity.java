package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "telemetria", schema = "public")
public class TelemetriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payload_id")
    private PayloadEntity payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private VeiculosEntity veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id")
    private PerfilEntity motorista;

    @Column(name = "id_rastreamento")
    private Long idRastreamento;

    @Column(name = "evento")
    private String evento;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "velocidade")
    private BigDecimal velocidad;

    @Column(name = "gps_valido")
    private Boolean gpsValido;

    @Column(name = "ignicao", nullable = false)
    private Boolean ignicao;

    @Column(name = "rpm")
    private BigDecimal rpm;

    @Column(name = "tensao_bateria")
    private BigDecimal tensaoBateria;

    @Column(name = "nivel_combustivel")
    private BigDecimal nivelCombustivel;

    @Column(name = "hodometro")
    private BigDecimal hodometro;

    @Column(name = "horimetro")
    private BigDecimal horimetro;

    @Column(name = "status_freio")
    private Boolean statusFreio;

    @Column(name = "evento_frenagem")
    private Boolean eventoFrenagem;

    @Column(name = "evento_aceleracao")
    private Boolean eventoAceleracao;

    @Column(name = "total_combustivel")
    private BigDecimal totalCombustivel;

    @Column(name = "temperatura_oleo")
    private BigDecimal temperaturaOleo;

    @Column(name = "posicao_pedal_acelerador")
    private BigDecimal posicaoPedalAcelerador;

    @Column(name = "posicao_pedal_freio")
    private BigDecimal posicaoPedalFreio;

    @Column(name = "loaded")
    private Boolean loaded;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra", columnDefinition = "jsonb")
    private String extra;

    @Column(name = "placa_snapshot")
    private String placaSnapshot;

    @Column(name = "motorista_snapshot")
    private String motoristaSnapshot;

    @Column(name = "data_evento", nullable = false)
    private Instant dataEvento;

    @Column(name = "criado_em", updatable = false, insertable = false)
    private Instant criadoEm;

}
