package visioncontrol.mensageria.telemetria.business.consumer.pgmq;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PgmqService {
    private final JdbcTemplate jdbcTemplate;

    public void send(String queueName, String message) {
        String sql = "SELECT pgmq.send(?, ?)";
        jdbcTemplate.update(sql, queueName, message);
    }

    public List<Map<String, Object>> receive(String queueName, int vt, int batchSize) {
        String sql = "SELECT * FROM pgmq.read(?, ?, ?)";
        return jdbcTemplate.queryForList(sql, queueName, vt, batchSize);
    }

    public void delete(String queueName, long msgId) {
        String sql = "SELECT pgmq.delete(?, ?)";
        jdbcTemplate.update(sql, queueName, msgId);
    }
}