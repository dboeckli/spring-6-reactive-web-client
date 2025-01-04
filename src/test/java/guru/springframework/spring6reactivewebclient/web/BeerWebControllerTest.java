package guru.springframework.spring6reactivewebclient.web;

import guru.springframework.spring6reactivewebclient.client.BeerClient;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BeerWebControllerTest {

    @Mock
    BeerClient beerClient;

    BeerWebController controller;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BeerWebController(beerClient);
    }

    @Test
    void testGetBeers() {
        // Given
        BeerDto beer1 = BeerDto.builder().id("1").beerName("Beer1").build();
        BeerDto beer2 = BeerDto.builder().id("2").beerName("Beer2").build();
        when(beerClient.listBeerAsDtos()).thenReturn(Flux.just(beer1, beer2));

        // When
        Mono<Rendering> result = controller.getBeers();

        // Then
        StepVerifier.create(result)
            .assertNext(rendering -> {
                Map<String, Object> model = rendering.modelAttributes();
                assertTrue(model.containsKey("beers"));
                List<BeerDto> beers = (List<BeerDto>) model.get("beers");
                assertEquals(2, beers.size());
                assertEquals("Beer1", beers.get(0).getBeerName());
                assertEquals("Beer2", beers.get(1).getBeerName());
            })
            .verifyComplete();

        verify(beerClient, times(1)).listBeerAsDtos();
    }

    @Test
    void testGetBeerById() {
        // Given
        String beerId = "testId";
        BeerDto beer = BeerDto.builder().id(beerId).beerName("TestBeer").build();
        when(beerClient.getBeerById(beerId)).thenReturn(Mono.just(beer));

        // When
        Mono<Rendering> result = controller.getBeerById(beerId);

        // Then
        StepVerifier.create(result)
            .assertNext(rendering -> {
                Map<String, Object> model = rendering.modelAttributes();
                assertTrue(model.containsKey("beer"));
                BeerDto returnedBeer = (BeerDto) model.get("beer");
                assertEquals(beerId, returnedBeer.getId());
                assertEquals("TestBeer", returnedBeer.getBeerName());
            })
            .verifyComplete();

        verify(beerClient, times(1)).getBeerById(beerId);
    }
}
