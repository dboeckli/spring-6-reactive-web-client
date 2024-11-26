package guru.springframework.spring6reactivewebclient.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class BeerClientImpl implements BeerClient {
    
    private static final String BEER_PATH = "/api/v3/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    private final WebClient webClient;

    public BeerClientImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<BeerDto> getBeerById(String beerId) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder.path(BEER_PATH_ID).build(beerId))
            .retrieve().bodyToMono(BeerDto.class);
    }

    @Override
    public Flux<BeerDto> getBeerByBeerStyle(String beerStyle) {
        return webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_PATH)
                .queryParam("beerStyle", beerStyle).build())
            .retrieve()
            .bodyToFlux(BeerDto.class);
    }

    @Override
    public Flux<String> listBeer() {
        return webClient.get().uri(BEER_PATH)
            .retrieve().bodyToFlux(String.class);
    }

    @Override
    public Flux<Map> listBeerMap() {
        return webClient.get().uri(BEER_PATH)
            .retrieve().bodyToFlux(Map.class);
    }

    @Override
    public Flux<JsonNode> listBeerJsonNode() {
        return webClient.get().uri(BEER_PATH)
            .retrieve().bodyToFlux(JsonNode.class);
    }

    @Override
    public Flux<BeerDto> listBeerAsDtos() {
        return webClient.get().uri(BEER_PATH)
            .retrieve().bodyToFlux(BeerDto.class);
    }
}
