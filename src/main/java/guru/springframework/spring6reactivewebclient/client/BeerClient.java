package guru.springframework.spring6reactivewebclient.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import reactor.core.publisher.Flux;

import java.util.Map;

public interface BeerClient {
    
    Flux<String> listBeer();

    Flux<Map> listBeerMap();

    Flux<JsonNode> listBeerJsonNode();

    Flux<BeerDto> listBeerAsDtos();
    
}
