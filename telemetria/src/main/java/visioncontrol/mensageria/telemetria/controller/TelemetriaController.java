package visioncontrol.mensageria.telemetria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class TelemetriaController {

    public final RabbitTemplate rabbitTemplate;

    @PostMapping("/rastreamos")
    public ResponseEntity<String> receberWebHook (@RequestBody String payload) {
        log.info("Webhook recebido, publicando na fila...");

        rabbitTemplate.convertAndSend("rastreamos.entrada", payload);

        log.info("Dado publicado na fila!");
        return ResponseEntity.ok("recebido");
    }

}
