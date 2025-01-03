package guru.springframework.spring6reactivewebclient.web;

import guru.springframework.spring6reactivewebclient.client.BeerClient;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("it")
class BeerWebControllerIT {

    @Autowired
    BeerWebController controller;

    @Autowired
    BeerClient beerClient;

    @Test
    void testGetBeers() {
        Mono<Rendering> result = controller.getBeers();

        StepVerifier.create(result)
            .assertNext(rendering -> {
                Map<String, Object> model = rendering.modelAttributes();
                assertTrue(model.containsKey("beers"));
                List<BeerDto> beers = (List<BeerDto>) model.get("beers");
                assertFalse(beers.isEmpty());
                beers.forEach(beer -> {
                    assertNotNull(beer.getId());
                    assertNotNull(beer.getBeerName());
                });
            })
            .verifyComplete();
    }

    @Test
    void testGetBeerById() {
        // First, get a list of beers
        List<BeerDto> beers = beerClient.listBeerAsDtos().collectList().block();
        assertFalse(beers.isEmpty());

        // Take the first beer's ID
        String beerId = beers.getFirst().getId();

        Mono<Rendering> result = controller.getBeerById(beerId);

        StepVerifier.create(result)
            .assertNext(rendering -> {
                Map<String, Object> model = rendering.modelAttributes();
                assertTrue(model.containsKey("beer"));
                BeerDto returnedBeer = (BeerDto) model.get("beer");
                assertEquals(beerId, returnedBeer.getId());
                assertNotNull(returnedBeer.getBeerName());
            })
            .verifyComplete();
    }
}
