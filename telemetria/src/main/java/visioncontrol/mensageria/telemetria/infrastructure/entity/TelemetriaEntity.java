package visioncontrol.mensageria.telemetria.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "telemetria", schema = "public")
public class TelemetriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_evento")
    private LocalDateTime date;

    @Column(name = "empresa_id")
    private Integer empresaId;

    @Column(name = "veiculo_id")
    private Integer veiculoId;

    private String event;
    private String plate;
    private Integer speed;
    private String driver;

    @Embedded
    private LatLongEmbeddable latLong;

    private Boolean gpsValid;
    private Boolean ignition;
    private Long odometer;
    private Long idTracking;
    private String externalCode;
    private String safeAreaName;
    private Double batteryVoltage;

    @Column(name = "tel_luz")
    private String telLuz;

    @Column(name = "tel_rpm")
    private Integer telRpm;

    @Column(name = "tel_chuva")
    private Integer telChuva;

    @Column(name = "tel_speed")
    private Integer telSpeed;

    @Column(name = "tel_loaded")
    private Boolean telLoaded;

    @Column(name = "tel_marcha")
    private String telMarcha;

    @Column(name = "tel_portas")
    private Boolean telPortas;

    @Column(name = "tel_tensao")
    private Double telTensao;

    @Column(name = "tel_ignition")
    private Integer telIgnition;

    @Column(name = "tel_hodometro")
    private Long telHodometro;

    @Column(name = "tel_horimetro")
    private Long telHorimetro;

    @Column(name = "tel_id_empresa")
    private Long telIdEmpresa;

    @Column(name = "tel_id_veiculo")
    private Long telIdVeiculo;

    @Column(name = "tel_motorista")
    private String telMotorista;

    @Column(name = "tel_id_motorista")
    private Long telIdMotorista;

    @Column(name = "tel_status_freio")
    private Boolean telStatusFreio;

    @Column(name = "tel_carga_bateria")
    private Integer telCargaBateria;

    @Column(name = "tel_date_time_evento")
    private LocalDateTime telDateTimeEvento;

    @Column(name = "tel_temperatura_oleo")
    private Integer telTemperaturaOleo;

    @Column(name = "tel_nivel_combustivel")
    private Integer telNivelCombustivel;

    @Column(name = "tel_total_combustivel")
    private Long telTotalCombustivel;

    @Column(name = "tel_posicao_pedal_freio")
    private Integer telPosicaoPedalFreio;

    @Column(name = "tel_posicao_pedal_acelerador")
    private Integer telPosicaoPedalAcelerador;

    @Column(name = "tel_temperatura_cabine")
    private Integer telTemperaturaCabine;

    @Column(name = "tel_temperatura_externa")
    private Integer telTemperaturaExterna;

    @Column(name = "tel_temperatura_arrefecimento")
    private Integer telTemperaturaArrefecimento;

    // Flags de Eventos Comportamentais
    private Boolean telEventoRpmIn;
    private Boolean telEventoRpmOut;
    private Boolean telEventoSpeedIn;
    private Boolean telEventoSpeedOut;
    private Boolean telEventoFrenagem;
    private Boolean telEventoAceleracao;
    private Boolean telEventoOciosoIn;
    private Boolean telEventoOciosoOut;
    private Boolean telEventoBanguelaIn;
    private Boolean telEventoBanguelaOut;
    private Boolean telEventoChuvaSpeedIn;
    private Boolean telEventoChuvaSpeedOut;

    // Eixos de Carga
    private Integer telPesoSegundoEixo;
    private Integer telPesoTerceiroEixo;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}