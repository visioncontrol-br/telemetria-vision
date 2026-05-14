package visioncontrol.mensageria.telemetria;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class TelemetriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelemetriaApplication.class, args);
	}

}
