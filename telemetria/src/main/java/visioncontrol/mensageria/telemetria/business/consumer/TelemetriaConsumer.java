package visioncontrol.mensageria.telemetria.business.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetriaConsumer {

    @Autowired
   public TelemetriaRepository telemetriaRepository;

   @RabbitListener(queues = "dados-telemetria")
    public void receber(Message message){

       String payload = new String(message.getBody());
       log.info("Dados recebidos: {}", payload);

       TelemetriaEntity telemetria = new TelemetriaEntity();
       telemetria.setPayload(payload);

       telemetriaRepository.save(telemetria);
       log.info("Salvo no Supabase com sucesso!");

   }

}