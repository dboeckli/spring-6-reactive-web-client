package guru.springframework.spring6reactivewebclient.web;

import guru.springframework.spring6reactivewebclient.client.BeerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class BeerWebController {

    private final BeerClient beerClient;

    @GetMapping("/beers")
    public Mono<Rendering> getBeers() {
        return beerClient.listBeerAsDtos()
            .collectList()
            .map(beers -> Rendering.view("beers")
                .modelAttribute("beers", beers)
                .build());
    }

    @GetMapping("/beer/{id}")
    public Mono<Rendering> getBeerById(@PathVariable("id") String beerId) {
        return beerClient.getBeerById(beerId)
            .map(beer -> Rendering.view("beer")
                .modelAttribute("beer", beer)
                .build());
    }
}
