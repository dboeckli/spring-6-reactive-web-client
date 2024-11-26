package guru.springframework.spring6reactivewebclient.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
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
        AtomicReference<List<Map>> atomicListResponse = new AtomicReference<>(new ArrayList<>());
        
        beerClient.listBeerMap().subscribe(response -> {
            log.info("### Response: " + response);
            atomicListResponse.get().add(response);
            if (atomicListResponse.get().size() == 3) {
                atomicBoolean.set(true);
            }
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
        assertEquals(3, atomicListResponse.get().size());
    }

    @Test
    void testListBeerJsonNode() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<List<JsonNode>> atomicListResponse = new AtomicReference<>(new ArrayList<>());
        
        beerClient.listBeerJsonNode().subscribe(response -> {
            log.info("### Response: " + response);
            atomicListResponse.get().add(response);
            if (atomicListResponse.get().size() == 3) {
                atomicBoolean.set(true);
            }
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
        assertEquals(3, atomicListResponse.get().size());
    }

    @Test
    void testListBeerAsDtos() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<List<BeerDto>> atomicListResponse = new AtomicReference<>(new ArrayList<>());
        
        beerClient.listBeerAsDtos().subscribe(response -> {
            log.info("### Response: " + response);
            atomicListResponse.get().add(response);
            if (atomicListResponse.get().size() == 3) {
                atomicBoolean.set(true);
            }
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
        assertEquals(3, atomicListResponse.get().size());
    }
}
