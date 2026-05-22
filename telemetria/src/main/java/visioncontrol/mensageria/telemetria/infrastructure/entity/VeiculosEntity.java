package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "veiculos", schema = "public")
@Getter
@Setter
public class VeiculosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "empresa_id", nullable = false)
    private Integer empresaId;

    @Column(name = "placa", nullable = false, unique = true)
    private String plate;

    private String modelo;
}