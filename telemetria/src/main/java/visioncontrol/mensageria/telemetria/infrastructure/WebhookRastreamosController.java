package visioncontrol.mensageria.telemetria.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookRastreamosController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    // Defina uma senha forte aqui ou no seu application.yml
    private static final String TOKEN_SECRETO = "V1s10nC0ntr0l_R@str3am0s_2026";

    @PostMapping("/rastreamos")
    public ResponseEntity<String> receberDadosRastreamos(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody String payloadJson) {

        // 1. Validação de Segurança Exigida
        if (token == null || !token.replace("Bearer ", "").equals(TOKEN_SECRETO)) {
            log.warn("[SEGURANÇA] Tentativa de injeção bloqueada. Token inválido.");
            return ResponseEntity.status(401).body("Não autorizado");
        }

        try {
            // 2. Enfileiramento ultra-rápido no RabbitMQ (Resolve a regra dos 2 segundos)
            rabbitTemplate.convertAndSend(exchange, routingKey, payloadJson);

            return ResponseEntity.ok("Recebido");

        } catch (Exception e) {
            log.error("Erro ao processar payload da Rastreamos", e);
            return ResponseEntity.internalServerError().body("Erro interno");
        }
    }
}