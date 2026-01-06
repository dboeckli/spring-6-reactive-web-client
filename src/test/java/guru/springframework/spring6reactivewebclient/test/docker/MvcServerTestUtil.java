package guru.springframework.spring6reactivewebclient.test.docker;


import guru.springframework.spring6reactivewebclient.client.BeerClient;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@UtilityClass
public class MvcServerTestUtil {

    public static void checkDatabaseInitDone(BeerClient beerClient) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<List<BeerDto>> beerListResponse = new AtomicReference<>();
        AtomicReference<Throwable> lastError = new AtomicReference<>();

        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            beerClient.listBeerAsDtos()
                .collectList()
                .doOnSubscribe(s -> log.info("### [INIT] subscribe listBeerAsDtos()"))
                .doOnSuccess(beers -> log.info("### [INIT] onSuccess listBeerAsDtos(): size={}", beers.size()))
                .doOnError(e -> log.error("### [INIT] onError listBeerAsDtos()", e))
                .doFinally(signal -> log.info("### [INIT] finally listBeerAsDtos(): signal={}", signal))
                .subscribe(beers -> {
                    beerListResponse.set(beers);
                    atomicBoolean.set(beers.size() == 3);
                }, e -> {
                    lastError.set(e);
                    atomicBoolean.set(false);
                });

            if (lastError.get() != null) {
                // sofort sichtbar machen, dass es ein Fehler ist und kein “hängt”
                log.warn("### [INIT] letzter Fehler beim Polling: {}", lastError.get().toString());
            }
            return atomicBoolean.get();
        });

        List<BeerDto> beers = beerListResponse.get();
        assertNotNull(beers, "Beer list should not be null");
        assertEquals(3, beers.size(), "Expected 3 beers in the database");

        log.info("Database is fully initialized with 3 beers.");
        beers.forEach(beer -> log.info("Beer: {}", beer));

        log.info("Database is fully initialized with 3 beers.");
    }
}
