
package guru.springframework.spring6reactivewebclient.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthServerHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient webClient;
    private final String authServerUrl;
    private boolean wasDownLastCheck = false; // Initial-Wert auf false gesetzt

    public AuthServerHealthIndicator(WebClient.Builder webClientBuilder,
                                     @Value("${security.auth-server-health-url}") String authServerUrl) {
        this.webClient = webClientBuilder.build();
        this.authServerUrl = authServerUrl;
    }

    @Override
    public Mono<Health> health() {
        return checkAuthServerHealth()
            .map(status -> status ? Health.up().build() : Health.down().build())
            .doOnNext(health -> {
                boolean isUp = health.getStatus().equals(Health.up().build().getStatus());

                if (!isUp) {
                    wasDownLastCheck = true;
                    log.warn("Auth server ist nicht erreichbar unter {}", authServerUrl);
                } else if (wasDownLastCheck) {
                    // Nur wenn der vorherige Status "down" war, wird die "up" Meldung gesendet
                    log.info("Auth server ist wieder erreichbar unter {}", authServerUrl);
                    wasDownLastCheck = false;
                }
            });
    }

    private Mono<Boolean> checkAuthServerHealth() {
        return webClient.get()
            .uri(authServerUrl + "/actuator/health")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> response.contains("\"status\":\"UP\""))
            .onErrorResume(e -> Mono.just(false));
    }
}