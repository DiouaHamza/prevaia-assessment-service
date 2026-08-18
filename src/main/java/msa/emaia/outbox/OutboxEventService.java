package msa.emaia.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    public void publish(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        outboxEventRepository.save(new OutboxEvent(aggregateType, aggregateId, eventType, payload));
    }
}
