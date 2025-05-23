package guru.springframework.spring6reactivewebclient.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
@Slf4j
class ActuatorInfoTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    BuildProperties buildProperties;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void actuatorInfoTest() {
        webTestClient.get().uri("/actuator/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String jsonResponse = new String(response.getResponseBody());
                    try {
                        Object json = objectMapper.readValue(jsonResponse, Object.class);
                        log.info("Response:\n{}", objectMapper.writeValueAsString(json));
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing JSON response", e);
                    }
                })
                .jsonPath("$.git.commit.id").isNotEmpty()
                .jsonPath("$.build.javaVersion").isEqualTo("21")
                .jsonPath("$.build.commit-id").isNotEmpty()
                .jsonPath("$.build.javaVendor").isNotEmpty()
                .jsonPath("$.build.artifact").isEqualTo(buildProperties.getArtifact())
                .jsonPath("$.build.group").isEqualTo(buildProperties.getGroup());

    }


    @Test
    void actuatorHealthTest() throws JsonProcessingException {
        EntityExchangeResult<byte[]> result = webTestClient.get().uri("/actuator/health/readiness")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .returnResult();

        String jsonResponse = new String(Objects.requireNonNull(result.getResponseBody()));
        Object json = objectMapper.readValue(jsonResponse, Object.class);
        log.info("Response:\n{}", objectMapper.writeValueAsString(json));
    }

}
