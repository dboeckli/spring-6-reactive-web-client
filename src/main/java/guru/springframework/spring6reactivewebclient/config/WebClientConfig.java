package guru.springframework.spring6reactivewebclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.webflux.LogbookExchangeFilterFunction;

@Configuration
public class WebClientConfig implements WebClientCustomizer {

    @Value("${spring.security.oauth2.client.registration.springauth.client-name}")
    String clientRegistrationId;
    
    private final String rootUrl;
    
    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    public WebClientConfig(@Value("${webclient.reactive-mongo-url}") String rootUrl, ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        this.rootUrl = rootUrl;
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId(clientRegistrationId);

        LogbookExchangeFilterFunction logbookExchangeFilterFunction = new LogbookExchangeFilterFunction(Logbook.builder().build());
        
        webClientBuilder.filter(oauth).filter(logbookExchangeFilterFunction).baseUrl(rootUrl);
    }
}
