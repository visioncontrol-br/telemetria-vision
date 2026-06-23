package visioncontrol.mensageria.telemetria.infrastructure.partitionScheduler;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PartitionScheduler {

    private final EntityManager entityManager;

    /**
     * Roda automaticamente todo dia 25 de cada mês, às 03:00 da manhã.
     * Expressão Cron: Segundos Minutos Horas Dia Mês DiaDaSemana
     */
    @Scheduled(cron = "0 0 3 25 * ?")
    @Transactional
    public void criarParticaoDoProximoMes() {
        log.info("[PARTICIONAMENTO] Iniciando verificação e criação da partição do próximo mês...");

        // Pega a data de hoje e soma 1 mês
        LocalDate proximoMes = LocalDate.now().plusMonths(1);

        String anoMes = proximoMes.format(DateTimeFormatter.ofPattern("yyyy_MM"));
        String nomeTabelaFilha = "posicoes_veiculos_" + anoMes;

        // Pega o primeiro dia do próximo mês
        LocalDate inicio = proximoMes.withDayOfMonth(1);
        // Pega o primeiro dia do mês seguinte (para fechar o range)
        LocalDate fim = inicio.plusMonths(1);

        String dataInicio = inicio + " 00:00:00Z";
        String dataFim = fim + " 00:00:00Z";

        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s PARTITION OF public.posicoes_veiculos " +
                        "FOR VALUES FROM ('%s') TO ('%s');",
                nomeTabelaFilha, dataInicio, dataFim
        );

        try {
            entityManager.createNativeQuery(sql).executeUpdate();
            log.info("[PARTICIONAMENTO] Partição {} garantida com sucesso para o range {} até {}.",
                    nomeTabelaFilha, dataInicio, dataFim);
        } catch (Exception e) {
            log.error("[PARTICIONAMENTO CRÍTICO] Falha ao criar a partição {}. O banco pode travar no dia 1! Erro: {}",
                    nomeTabelaFilha, e.getMessage());
        }
    }

}
