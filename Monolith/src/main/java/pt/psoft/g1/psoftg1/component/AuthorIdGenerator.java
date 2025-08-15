package pt.psoft.g1.psoftg1.component;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AuthorIdGenerator {

    private final AtomicLong incrementalCounter = new AtomicLong(1); // Replace with DB or Redis in production

    public String generateHexId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24);
    }

    public String generateBusinessId(String input) {
        String hash = DigestUtils.sha256Hex(input);
        return hash.replaceAll("[^a-zA-Z0-9]", "").substring(0, 20);
    }

    public Long generateCustomIncrementalId() {
        return incrementalCounter.getAndIncrement();
    }
}
