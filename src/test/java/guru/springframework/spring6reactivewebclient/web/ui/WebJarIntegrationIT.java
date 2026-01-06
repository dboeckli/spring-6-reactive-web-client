package guru.springframework.spring6reactivewebclient.web.ui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("it")
class WebJarIntegrationIT {

    @Autowired
    private WebTestClient webTestClient;


    @Test
    void shouldResolveBootstrapCssWithoutVersion() {
        // Dieser Test simuliert einen Zugriff auf die CSS Datei OHNE Versionsnummer.
        // Dies stellt sicher, dass 'webjars-locator-lite' korrekt konfiguriert ist und funktioniert.
        webTestClient.get()
            .uri("/webjars/bootstrap/css/bootstrap.min.css")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.valueOf("text/css"));
    }

    @Test
    void shouldResolveBootstrapJsWithoutVersion() {
        webTestClient.get()
            .uri("/webjars/bootstrap/js/bootstrap.min.js")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.valueOf("text/javascript"));
    }
}
