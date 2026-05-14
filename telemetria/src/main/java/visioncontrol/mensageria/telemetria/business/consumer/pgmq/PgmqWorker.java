package visioncontrol.mensageria.telemetria.business.consumer.pgmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class PgmqWorker {

    private final PgmqService pgmqService;
    private final TelemetriaRepository telemetriaRepository;
    private final ObjectMapper objectMapper;
    private static final String QUEUE = "processamento_telemetria";

    @Scheduled(fixedDelay = 5000)
    public void processar() {
        var msgs = pgmqService.receive(QUEUE, 30, 10);
        for (var msg : msgs) {
            Long msgId = (Long) msg.get("msg_id");
            String entityId = (String) msg.get("message"); // Já é String, não converter
            try {
                TelemetriaEntity bruto = telemetriaRepository.findById(entityId).orElse(null);
                if (bruto == null) {
                    log.warn("Registro {} não encontrado no banco", entityId);
                    pgmqService.delete(QUEUE, msgId); // remove da fila para não travar
                    continue;
                }

                // Agora faz o parsing do payload bruto
                JsonNode root = objectMapper.readTree(bruto.getPayload());
                // Extrai latitude, longitude, etc.
                // Salva na tabela estruturada (ex: eventos_veiculo)

                // Após sucesso, deleta da fila
                pgmqService.delete(QUEUE, msgId);
                log.info("Mensagem {} processada com sucesso", msgId);

            } catch (Exception e) {
                log.error("Erro ao processar msgId {} para entityId {}: {}", msgId, entityId, e.getMessage());
                // Não deleta – a mensagem retornará à fila após visibility timeout
            }
        }
    }
}