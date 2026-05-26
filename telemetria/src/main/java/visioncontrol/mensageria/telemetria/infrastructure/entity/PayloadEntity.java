package visioncontrol.mensageria.telemetria.infrastructure.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payload_desconhecido", schema = "public")
public class PayloadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    // O SqlTypes.JSON obriga o Hibernate a converter qualquer JSON aleatório para a coluna JSONB do banco
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_brutos", columnDefinition = "jsonb", nullable = false)
    private JsonNode dadosBrutos;
}