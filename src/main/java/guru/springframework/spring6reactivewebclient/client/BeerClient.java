package guru.springframework.spring6reactivewebclient.client;

import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

import java.util.Map;

public interface BeerClient {
    
    Mono<BeerDto> getBeerById(String beerId);

    Flux<BeerDto> getBeerByBeerStyle(String beerStyle);
    
    Flux<String> listBeer();

    Flux<Map> listBeerMap();

    Flux<JsonNode> listBeerJsonNode();

    Flux<BeerDto> listBeerAsDtos();
    
    Mono<BeerDto> createBeer(BeerDto beerDto);

    Mono<BeerDto> updateBeer(BeerDto beerDto);

    Mono<BeerDto> patchBeer(BeerDto beerDto);

    Mono<Void> deleteBeer(String beerId);
    
}
