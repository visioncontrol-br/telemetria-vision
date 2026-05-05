package visioncontrol.mensageria.telemetria.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import visioncontrol.mensageria.telemetria.business.consumer.TelemetriaConsumer;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/telemetria")
public class TelemetriaController {

    public final RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<Void> enviaDados(@RequestBody Map<String, Object> dados) {
        // Envia para a exchange que você configurou
        rabbitTemplate.convertAndSend("telemetria.exchange", "telemetria.routing.key", dados);
        return ResponseEntity.ok().build();
    }

}
