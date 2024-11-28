package guru.springframework.spring6reactivewebclient.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("TODO: all those tests are failing in the github pipeline, because the server part (project: spring-6-reactive-mongo) is not running. and it requires auth server as well.")
class BeerClientImplTest {
    
    @Autowired
    BeerClient beerClient;

    @Test
    @Order(0)
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
    @Order(0)
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
    @Order(0)
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
    @Order(0)
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
    @Order(0)
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
    @Order(0)
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
    @Order(1)
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
    @Order(2)
    void updateBeer() {
        BeerDto beerToUpdate = beerClient.listBeerAsDtos().blockFirst();
        beerToUpdate.setBeerName("Updated Beer");
        
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDto> updatedBeer = new AtomicReference<>();

        beerClient.updateBeer(beerToUpdate)
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
    @Order(3)
    void patchBeer() {
        BeerDto beerToPatch = beerClient.listBeerAsDtos().blockFirst();
        beerToPatch.setBeerName("Patched Beer");
        String beerStyleBeforePatch = beerToPatch.getBeerStyle();
        beerToPatch.setBeerStyle(null);
        BigDecimal priceBeforePatch = beerToPatch.getPrice();
        beerToPatch.setPrice(null);
        String upcBeforePatch = beerToPatch.getUpc();
        beerToPatch.setUpc(null);
        Integer quantityBeforePatch = beerToPatch.getQuantityOnHand();
        beerToPatch.setQuantityOnHand(null);

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDto> patchedBeer = new AtomicReference<>();

        beerClient.patchBeer(beerToPatch)
            .subscribe(dto -> {
                log.info("### Response: " + dto);
                atomicBoolean.set(true);
                patchedBeer.set(dto);
            });

        await().untilTrue(atomicBoolean);

        assertTrue(atomicBoolean.get());
        assertEquals("Patched Beer", patchedBeer.get().getBeerName());
        assertEquals(beerStyleBeforePatch, patchedBeer.get().getBeerStyle());
        assertEquals(priceBeforePatch, patchedBeer.get().getPrice());
        assertEquals(upcBeforePatch, patchedBeer.get().getUpc());
        assertEquals(quantityBeforePatch, patchedBeer.get().getQuantityOnHand());
    }

    @Test
    @Order(4)
    void deleteBeer() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDto> beerToDelete = new AtomicReference<>();

        beerClient.listBeerAsDtos()
            .next()
            .flatMap(dto -> {
                beerToDelete.set(dto);
                return beerClient.deleteBeer(dto.getId());
            })
            .doOnSuccess(mt -> {
                atomicBoolean.set(true);
            })
            .subscribe();

        await().untilTrue(atomicBoolean);
        
        try {
            beerClient.getBeerById(beerToDelete.get().getId()).block();
            fail("Beer not deleted");
        } catch (WebClientResponseException ex) {
            log.info("### Beer successful: " + ex.getMessage());
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertTrue(true);
        }
    }
}
