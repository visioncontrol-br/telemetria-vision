package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "perfis", schema = "public")
public class PerfilEntity {

    @Id
    private UUID id; // Chave primária manual vinculada ao ID do Auth Provider

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "cargo")
    private String cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private DepartamentoEntity departamento;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "ativo")
    private Boolean ativo;

    @Column(name = "criado_em", updatable = false, insertable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false)
    private Instant atualizadoEm;

}
