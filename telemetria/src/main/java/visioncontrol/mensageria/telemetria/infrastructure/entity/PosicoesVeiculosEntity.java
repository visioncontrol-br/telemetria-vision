package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "posicoes_veiculos", schema = "public")
public class PosicoesVeiculosEntity {

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

    @Column(name = "evento")
    private String evento;

    @Column(name = "gps_valido")
    private Boolean gpsValido;

    @Column(name = "id_rastreamento")
    private Long idRastreamento;

    @Column(name = "ignicao", nullable = false)
    private Boolean ignicao;

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude;

    @Column(name = "velocidade", nullable = false)
    private BigDecimal velocidade;

    @Column(name = "tensao_bateria")
    private BigDecimal tensaoBateria;

    @Column(name = "hodometro")
    private BigDecimal hodometro;

    @Column(name = "placa_snapshot")
    private String placaSnapshot;

    @Column(name = "motorista_snapshot")
    private String motoristaSnapshot;

    @Column(name = "codigo_externo")
    private String codigoExterno;

    @Column(name = "nome_area_segura")
    private String nomeAreaSegura;

    @Column(name = "data_evento", nullable = false)
    private Instant dataEvento;

    @Column(name = "inserido_em", updatable = false, insertable = false)
    private Instant inseridoEm;

}
