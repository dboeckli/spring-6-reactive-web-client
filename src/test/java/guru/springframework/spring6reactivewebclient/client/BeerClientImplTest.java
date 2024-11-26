package guru.springframework.spring6reactivewebclient.client;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log
class BeerClientImplTest {
    
    @Autowired
    BeerClient beerClient;

    @Test
    void testListBeer() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<String> atomicResponse = new AtomicReference<>();

        beerClient.listBeer().subscribe(response -> {
            log.info("### Response: " + response);
            atomicResponse.set(response);
            atomicBoolean.set(true);
        });
        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertNotNull(atomicResponse);

    }

    @Test
    void listBeerMap() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        beerClient.listBeerMap().subscribe(response -> {
            log.info("### Response: " + response);
            atomicBoolean.set(true);
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
    }

    @Test
    void testListBeerJsonNode() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        beerClient.listBeerJsonNode().subscribe(response -> {
            log.info("### Response: " + response);
            atomicBoolean.set(true);
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
    }

    @Test
    void testListBeerAsDtos() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        beerClient.listBeerAsDtos().subscribe(response -> {
            log.info("### Response: " + response);
            atomicBoolean.set(true);
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
    }
}
