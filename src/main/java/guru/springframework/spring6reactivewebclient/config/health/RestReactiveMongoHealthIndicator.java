
package guru.springframework.spring6reactivewebclient.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class RestReactiveMongoHealthIndicator implements HealthIndicator {

    private final RestClient restClient;
    private final String restReactiveMongoUrl;
    private boolean wasDownLastCheck = false;

    public RestReactiveMongoHealthIndicator(@Value("${webclient.reactive-mongo-url}") String restReactiveMongoUrl) {
        this.restClient = RestClient.create();
        this.restReactiveMongoUrl = restReactiveMongoUrl;
    }

    @Override
    public Health health() {
        try {
            String response = restClient.get()
                .uri(restReactiveMongoUrl + "/actuator/health")
                .retrieve()
                .body(String.class);

            if (response != null && response.contains("\"status\":\"UP\"")) {
                if (wasDownLastCheck) {
                    log.info("Reactive Mongo Server ist wieder erreichbar unter {}", restReactiveMongoUrl);
                    wasDownLastCheck = false;
                }
                return Health.up().build();
            } else {
                wasDownLastCheck = true;
                log.warn("Reactive Mongo Server meldet keinen UP-Status unter {}", restReactiveMongoUrl);
                return Health.down().build();
            }
        } catch (Exception e) {
            wasDownLastCheck = true;
            log.warn("Reactive Mongo Server ist nicht erreichbar unter {}", restReactiveMongoUrl, e);
            return Health.down(e).build();
        }
    }
}