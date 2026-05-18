package visioncontrol.mensageria.telemetria.business.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesClaudEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PosicoesClaudRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.VeiculosRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TelemetriaService {

    private final VeiculosRepository veiculoRepository;

    private final PosicoesClaudRepository posicaoClaudRepository;

    @Transactional
    public void processarPayload(String payloadCru) {
        // Exemplo hipotético de quebra de String (Substitua pela lógica real do seu protocolo)
        // Supondo formato: PLACA;EVENTO;IGNICAO;LATITUDE;LONGITUDE;VELOCIDADE
        String[] dados = payloadCru.split(";");

        String placa = dados[0];
        String evento = dados[1];
        Integer ignicao = Integer.parseInt(dados[2]);
        BigDecimal lat = new BigDecimal(dados[3]);
        BigDecimal lon = new BigDecimal(dados[4]);
        BigDecimal vel = new BigDecimal(dados[5]);

        // 1. Identificar a empresa dona do veículo através da placa
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placa)
                .orElseThrow(() -> new IllegalArgumentException("Veículo com a placa " + placa + " não encontrado no sistema."));

        // 2. Montar o objeto correspondente à tabela posicoes_claud
        PosicoesClaudEntity posicao = new PosicoesClaudEntity();
        posicao.setCompanyId(veiculo.getEmpresaId()); // Herdado do veículo encontrado
        posicao.setPlate(placa);
        posicao.setEvent(evento);
        posicao.setIgnition(ignicao);
        posicao.setLatitude(lat);
        posicao.setLongitude(lon);
        posicao.setSpeed(vel);
        posicao.setDate(OffsetDateTime.now());

        // Os demais dados opcionais (odômetro, voltagem) podem ser populados se existirem no payload

        posicaoClaudRepository.save(posicao);
    }
}