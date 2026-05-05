package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dados-telemetria")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelemetriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    private LocalDateTime recebidoEm;
}
