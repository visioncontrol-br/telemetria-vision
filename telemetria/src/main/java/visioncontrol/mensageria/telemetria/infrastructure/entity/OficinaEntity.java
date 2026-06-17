package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "oficina", schema = "public")
public class OficinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private VeiculosEntity veiculo;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "prioridade")
    private String prioridade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private PerfilEntity responsavel;

    @Column(name = "data_registro")
    private LocalDate dataRegistro;

    @Column(name = "status")
    private String status;

    @Column(name = "criado_em", updatable = false, insertable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false)
    private Instant atualizadoEm;

}
