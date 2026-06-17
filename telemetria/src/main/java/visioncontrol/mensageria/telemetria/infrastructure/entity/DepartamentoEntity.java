package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "departamentos", schema = "public")
public class DepartamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "rota_tela")
    private String rotaTela;

    @Column(name = "ativo")
    private Boolean ativo;

    @Column(name = "criado_em", updatable = false, insertable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false)
    private Instant updatedAt;

}
