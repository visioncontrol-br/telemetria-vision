package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "excesso_velocidade", schema = "public")
public class ExcessoVelocidadeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private VeiculosEntity veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id")
    private PerfilEntity motorista;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia;

    @Column(name = "inicio_excesso_dia", nullable = false)
    private Instant inicioExcessoDia;

    @Column(name = "fim_excesso_dia", nullable = false)
    private Instant fimExcessoDia;

    @Column(name = "velocidade_maxima_atingida", nullable = false)
    private BigDecimal velocidadeMaximaAtingida;

    @Column(name = "limite_velocidade_configurado")
    private BigDecimal limiteVelocidadeConfigurado;

    @Column(name = "quantidade_de_excessos")
    private Integer quantidadeDeExcessos;

    @Column(name = "lat_excesso_maximo")
    private BigDecimal latExcessoMaximo;

    @Column(name = "lon_excesso_maximo")
    private BigDecimal lonExcessoMaximo;

    @Column(name = "placa_snapshot")
    private String placaSnapshot;

    @Column(name = "frota_snapshot")
    private String frotaSnapshot;

    @Column(name = "categoria_snapshot")
    private String categoriaSnapshot;

    @Column(name = "motorista_nome_snapshot")
    private String motoristaNomeSnapshot;

    // Coluna gerada no banco: marcamos como não inserível nem atualizável
    @Column(name = "duracao_total_minutos", insertable = false, updatable = false)
    private BigDecimal duracaoTotalMinutos;

    @Column(name = "criado_em", updatable = false, insertable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false)
    private Instant atualizadoEm;

}
