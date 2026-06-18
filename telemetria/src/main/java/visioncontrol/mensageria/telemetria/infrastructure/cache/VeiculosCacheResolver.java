package visioncontrol.mensageria.telemetria.infrastructure.cache;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import visioncontrol.mensageria.telemetria.business.consumer.dto.VeiculoCacheDTO;
import visioncontrol.mensageria.telemetria.business.consumer.service.RastreioService;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.VeiculosRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class VeiculosCacheResolver {

    private final VeiculosRepository veiculoRepository;

    @Cacheable(value = "veiculosCache", key = "#placa", unless = "#result == null")
    public VeiculoCacheDTO buscarEstruturaVeiculo(String placa) {
        log.info("[CAFFEINE MISS] Indo ao banco mapear veículo: {}", placa);

        VeiculosEntity v = veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado para a placa: " + placa));

        return new VeiculoCacheDTO(
                v.getId(),
                v.getEmpresa().getId(),
                v.getMotorista() != null ? v.getMotorista().getId() : null,
                v.getPlaca(),
                v.getMotorista() != null ? v.getMotorista().getNomeCompleto() : null
        );
    }

}
