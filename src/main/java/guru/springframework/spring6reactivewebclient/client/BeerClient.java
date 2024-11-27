package guru.springframework.spring6reactivewebclient.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface BeerClient {
    
    Mono<BeerDto> getBeerById(String beerId);

    Flux<BeerDto> getBeerByBeerStyle(String beerStyle);
    
    Flux<String> listBeer();

    Flux<Map> listBeerMap();

    Flux<JsonNode> listBeerJsonNode();

    Flux<BeerDto> listBeerAsDtos();
    
    Mono<BeerDto> createBeer(BeerDto beerDto);

    Mono<BeerDto> updateBeer(String beerId, BeerDto beerDto);

    Mono<BeerDto> patchBeer(String beerId, BeerDto beerDto);

    Mono<Void> deleteBeer(String beerId);
    
}
