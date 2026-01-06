package guru.springframework.spring6reactivewebclient.config.health;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
@ActiveProfiles(value = "it")
class RestReactiveMongoHealthIndicatorIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testAuthServerHealthCheck() {
        webTestClient.get().uri("/actuator/health/restReactiveMongo")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP");
    }

}