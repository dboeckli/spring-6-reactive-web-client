package guru.springframework.spring6reactivewebclient.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
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
@Disabled("TODO: all those tests are failing in the github pipeline, because the server part (project: spring-6-reactive-mongo) is not running.")
class BeerClientImplTest {
    
    @Autowired
    BeerClient beerClient;

    @Test
    void testGetBeerByBeerStyle() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<List<BeerDto>> beerResponse = new AtomicReference<>(new ArrayList<>());

        beerClient.getBeerByBeerStyle("Pale Ale").subscribe(response -> {
            log.info("### Response: " + response);
            beerResponse.get().add(response);
            if (beerResponse.get().size() == 2) {
                atomicBoolean.set(true);
            }
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
        assertEquals(2, beerResponse.get().size());
    }
    
    @Test
    void testGetBeerById() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDto> beerResponse = new AtomicReference<>();

        beerClient.listBeerAsDtos().take(1)
            .flatMap(beerDto -> beerClient.getBeerById(beerDto.getId()))
            .subscribe(beerDto -> {
                log.info("### Response: " + beerDto);
                atomicBoolean.set(true);
                beerResponse.set(beerDto);
            });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertNotNull(beerResponse.get());
    }

    @Test
    void testListBeer() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<String> beerListResponse = new AtomicReference<>();

        beerClient.listBeer().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.set(response);
            atomicBoolean.set(true);
        });
        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertNotNull(beerListResponse);

    }

    @Test
    void listBeerMap() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<List<Map>> beerListResponse = new AtomicReference<>(new ArrayList<>());
        
        beerClient.listBeerMap().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.get().add(response);
            if (beerListResponse.get().size() == 3) {
                atomicBoolean.set(true);
            }
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
        assertEquals(3, beerListResponse.get().size());
    }

    @Test
    void testListBeerJsonNode() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<List<JsonNode>> beerListResponse = new AtomicReference<>(new ArrayList<>());
        
        beerClient.listBeerJsonNode().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.get().add(response);
            if (beerListResponse.get().size() == 3) {
                atomicBoolean.set(true);
            }
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
        assertEquals(3, beerListResponse.get().size());
    }

    @Test
    void testListBeerAsDtos() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<List<BeerDto>> beerListResponse = new AtomicReference<>(new ArrayList<>());
        
        beerClient.listBeerAsDtos().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.get().add(response);
            if (beerListResponse.get().size() == 3) {
                atomicBoolean.set(true);
            }
        });

        await().atMost(5, TimeUnit.SECONDS).untilTrue(atomicBoolean);
        assertTrue(atomicBoolean.get());
        assertEquals(3, beerListResponse.get().size());
    }

    @Test
    void testCreateBeer() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDto> createdBeer = new AtomicReference<>();

        BeerDto newDto = BeerDto.builder()
            .price(new BigDecimal("10.99"))
            .beerName("Created Beer")
            .beerStyle("IPA")
            .quantityOnHand(500)
            .upc("123245")
            .build();

        beerClient.createBeer(newDto)
            .subscribe(dto -> {
                log.info("### Response: " + dto);
                atomicBoolean.set(true);
                createdBeer.set(dto);
            });

        await().untilTrue(atomicBoolean);

        assertTrue(atomicBoolean.get());
        assertEquals("Created Beer", createdBeer.get().getBeerName());
        assertNotNull(createdBeer.get().getId());
    }

    @Test
    void updateBeer() {
        BeerDto beerToUpdate = beerClient.listBeerAsDtos().blockFirst();
        beerToUpdate.setBeerName("Updated Beer");
        
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDto> updatedBeer = new AtomicReference<>();

        beerClient.updateBeer(beerToUpdate.getId(), beerToUpdate)
            .subscribe(dto -> {
                log.info("### Response: " + dto);
                atomicBoolean.set(true);
                updatedBeer.set(dto);
            });

        await().untilTrue(atomicBoolean);

        assertTrue(atomicBoolean.get());
        assertEquals("Updated Beer", updatedBeer.get().getBeerName());
    }

    @Test
    void patchBeer() {
        // TODO: Implement this test
    }

    @Test
    void deleteBeer() {
        // TODO: Implement this test
    }
}
