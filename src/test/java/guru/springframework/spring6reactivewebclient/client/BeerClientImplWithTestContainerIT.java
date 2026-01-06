package guru.springframework.spring6reactivewebclient.client;

import guru.springframework.spring6reactivewebclient.dto.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("testcontainer")
@Slf4j
class BeerClientImplWithTestContainerIT {

    @Autowired
    Environment environment;

    private static final String DOCKER_IMAGE_PREFIX = "domboeckli";

    private static final String MONGO_VERSION = "8.2.3";
    private static final String AUTH_SERVER_VERSION = "0.0.5-SNAPSHOT";
    private static final String REACTIVE_MONGO_VERSION = "0.0.1-SNAPSHOT";
    private static final String GATEWAY_VERSION = "0.0.3-SNAPSHOT";

    static final int REST_REACTIVE_MONGO_PORT = TestSocketUtils.findAvailableTcpPort();
    static final int AUTH_SERVER_PORT = TestSocketUtils.findAvailableTcpPort();
    static final int REST_GATEWAY_PORT = TestSocketUtils.findAvailableTcpPort();

    static final Network sharedNetwork = Network.newNetwork();

    @TestConfiguration
    static class WebClientLoggingTestConfig {

        @Bean
        WebClientCustomizer webClientLoggingCustomizer() {
            return builder -> builder
                .filter(logRequest())
                .filter(logResponse());
        }

        private static ExchangeFilterFunction logRequest() {
            return ExchangeFilterFunction.ofRequestProcessor(req -> {
                log.info("### [WebClient] -> {} {}", req.method(), req.url());
                return Mono.just(req);
            });
        }

        private static ExchangeFilterFunction logResponse() {
            return ExchangeFilterFunction.ofResponseProcessor(resp -> {
                log.info("### [WebClient] <- status={}", resp.statusCode());
                return Mono.just(resp);
            });
        }
    }

    @Container
    // The MongoDBContainer provided by mongo testcontainer does not work with user env variables:
    // See: https://github.com/testcontainers/testcontainers-java/issues/4695
    static GenericContainer<?> mongoDBContainer = new GenericContainer<>("mongo:" + MONGO_VERSION)
        .withNetworkAliases("mongodb")
        .withNetwork(sharedNetwork)
        .withExposedPorts(27017)

        .withEnv("MONGO_INITDB_DATABASE", "sfg")
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "secret")

        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("mongodb")))

        .waitingFor(new WaitAllStrategy()
            .withStartupTimeout(Duration.ofSeconds(90))
            .withStrategy(Wait.forListeningPort())
            .withStrategy(Wait.forSuccessfulCommand(
                "mongosh --quiet --username root --password secret --authenticationDatabase admin " +
                    "--eval \"db.adminCommand('ping').ok\""
            ))
        );

    @Container
    static GenericContainer<?> authServer = new GenericContainer<>(DOCKER_IMAGE_PREFIX + "/spring-6-auth-server:" + AUTH_SERVER_VERSION)
        .withNetworkAliases("auth-server")
        .withNetwork(sharedNetwork)

        .withEnv("SERVER_PORT", String.valueOf(AUTH_SERVER_PORT))
        .withEnv("SPRING_SECURITY_OAUTH2_AUTHORIZATION_SERVER_ISSUER", "http://auth-server:" + AUTH_SERVER_PORT)

        .withExposedPorts(AUTH_SERVER_PORT)
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("auth-server")))
        .waitingFor(Wait.forHttp("/actuator/health/readiness")
            .forStatusCode(200)
            .forResponsePredicate(response ->
                response.contains("\"status\":\"UP\"")
            )
        )
        .waitingFor(new WaitAllStrategy()
            .withStartupTimeout(Duration.ofSeconds(90))
            .withStrategy(Wait.forHttp("/actuator/health/readiness")
                .forStatusCode(200)
                .forResponsePredicate(response -> response.contains("\"status\":\"UP\""))
            )
            .withStrategy(Wait.forHttp("/actuator/health")
                .forStatusCode(200)
                .forResponsePredicate(response -> {
                    log.info("####################################################################################");
                    log.info("AuthServer /actuator/health response: {}", response);
                    log.info("####################################################################################");
                    return true;
                })
            )
        );

    @Container
    static GenericContainer<?> restReactiveMongo = new GenericContainer<>(DOCKER_IMAGE_PREFIX + "/spring-6-reactive-mongo:" + REACTIVE_MONGO_VERSION)
        .withNetworkAliases("reactive-mongo")
        .withExposedPorts(REST_REACTIVE_MONGO_PORT)
        .withNetwork(sharedNetwork)

        .withEnv("SERVER_PORT", String.valueOf(REST_REACTIVE_MONGO_PORT))

        .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI", "http://auth-server:" + AUTH_SERVER_PORT)
        .withEnv("SECURITY_AUTH_SERVER_HEALTH_URL", "http://auth-server:" + AUTH_SERVER_PORT)

        .withEnv("SPRING_MONGODB_URI", "mongodb://root:secret@mongodb:27017/sfg?authSource=admin")
        .withEnv("SPRING_MONGODB_USERNAME", "root")
        .withEnv("SPRING_MONGODB_PASSWORD", "secret")

        .withEnv("MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED", "true")
        .withEnv("MANAGEMENT_ENDPOINT_HEALTH_GROUP_READINESS_INCLUDE", "mongo")
        .withEnv("MANAGEMENT_HEALTH_MONGO_ENABLED", "true")

        .dependsOn(mongoDBContainer, authServer)
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("reactive-mongo")))
        .waitingFor(new WaitAllStrategy()
            .withStartupTimeout(Duration.ofSeconds(90))
            .withStrategy(Wait.forHttp("/actuator/health/readiness")
                .forStatusCode(200)
                .forResponsePredicate(r -> r.contains("\"status\":\"UP\""))
            )
            .withStrategy(Wait.forHttp("/actuator/health")
                .forStatusCode(200)
                .forResponsePredicate(r -> {
                    boolean ok = r.contains("\"mongo\"") && r.contains("\"status\":\"UP\"");
                    if (!ok) {
                        String shortBody = r.length() > 800 ? r.substring(0, 800) + " …(truncated)" : r;
                        log.info("reactive-mongo Health noch nicht ready (warte auf mongo UP). Response: {}", shortBody);
                    } else {
                        String shortBody = r.length() > 800 ? r.substring(0, 800) + " …(truncated)" : r;
                        log.info("reactive-mongo Health ist UP. Response: {}", shortBody);
                    }
                    return ok;
                })
            )
        );

    @Container
    static GenericContainer<?> restGateway = new GenericContainer<>(DOCKER_IMAGE_PREFIX + "/spring-6-gateway:" + GATEWAY_VERSION)
        .withExposedPorts(REST_GATEWAY_PORT)
        .withNetwork(sharedNetwork)

        .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI", "http://auth-server:" + AUTH_SERVER_PORT)
        .withEnv("SERVER_PORT", String.valueOf(REST_GATEWAY_PORT))
        .withEnv("SPRING_CLOUD_GATEWAY_HTTPCLIENT_WIRETAP", "true")

        .withEnv("SECURITY_AUTH_SERVER_HEALTH_URL", "http://auth-server:" + AUTH_SERVER_PORT)
        .withEnv("SECURITY_REACTIVEMONGO_HEALTH_URL", "http://reactive-mongo:" + REST_REACTIVE_MONGO_PORT)
        .withEnv("SECURITY_MVC_HEALTH_URL", "http://reactive-mongo:" + REST_REACTIVE_MONGO_PORT) // by intention, we are the reactive-mongo route. This is a workaround to get up status
        .withEnv("SECURITY_REACTIVE_HEALTH_URL", "http://reactive-mongo:" + REST_REACTIVE_MONGO_PORT) // by intention, we are the reactive-mongo route. This is a workaround to get up status
        .withEnv("SECURITY_DATAREST_HEALTH_URL", "http://reactive-mongo:" + REST_REACTIVE_MONGO_PORT) // by intention, we are the reactive-mongo route. This is a workaround to get up status

        // Route for spring-6-reactive-mongo
        .withEnv("SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES[0]_ID", "mvc_route")
        .withEnv("SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES[0]_URI", "http://reactive-mongo:" + REST_REACTIVE_MONGO_PORT)
        .withEnv("SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES[0]_PREDICATES[0]", "Path=/api/v3/**")

        // Route for spring-6-auth-server
        .withEnv("SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES[1]_ID", "auth_route")
        .withEnv("SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES[1]_URI", "http://auth-server:" + AUTH_SERVER_PORT)
        .withEnv("SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES[1]_PREDICATES[0]", "Path=/oauth2/**")

        .withEnv("LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_GATEWAY", "INFO") // SET TRACE for detailed logs
        .withEnv("LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_HTTP_SERVER_REACTIVE", "INFO") // SET DEBUG for detailed logs  
        .withEnv("LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB_REACTIVE", "INFO") // SET DEBUG for detailed logs
        .withEnv("LOGGING_LEVEL_REACTOR_IPC_NETTY", "INFO") // SET DEBUG for detailed logs
        .withEnv("LOGGING_LEVEL_REACTOR_NETTY", "INFO") // SET DEBUG for detailed logs

        .dependsOn(authServer, restReactiveMongo)
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("gateway")))
        .waitingFor(new WaitAllStrategy()
            .withStartupTimeout(Duration.ofSeconds(90))
            .withStrategy(Wait.forHttp("/actuator/health/readiness")
                .forStatusCode(200)
                .forResponsePredicate(response -> response.contains("\"status\":\"UP\""))
            )
            .withStrategy(Wait.forHttp("/actuator/health")
                .forStatusCode(200)
                .forResponsePredicate(response -> {
                    log.info("####################################################################################");
                    log.info("Gateway /actuator/health response: {}", response);
                    log.info("####################################################################################");
                    return true;
                })
            )
        );

    @Autowired
    BeerClient beerClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String gatewayServerUrl = "http://" + restGateway.getHost() + ":" + restGateway.getFirstMappedPort();
        log.info("### Rest Gateway Server URL: " + gatewayServerUrl);
        registry.add("webclient.reactive-mongo-url", () -> gatewayServerUrl);


        String authServerBaseUrl = "http://" + authServer.getHost() + ":" + authServer.getFirstMappedPort();

        registry.add("spring.security.oauth2.client.provider.springauth.authorization-uri", () -> authServerBaseUrl + "/auth2/authorize");
        registry.add("spring.security.oauth2.client.provider.springauth.token-uri", () -> authServerBaseUrl + "/oauth2/token");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> authServerBaseUrl);
    }

    @BeforeAll
    static void setUp() {
        log.info("#### auth server listening on port {} and host: {} and port {}", AUTH_SERVER_PORT, authServer.getHost(), authServer.getFirstMappedPort());
        log.info("#### gateway server  listening on port {} and host: {} and port {}", REST_GATEWAY_PORT, restGateway.getHost(), restGateway.getFirstMappedPort());
        log.info("#### reactive-mongo server listening on port {} and host: {} and port {}", REST_REACTIVE_MONGO_PORT, restReactiveMongo.getHost(), restReactiveMongo.getFirstMappedPort());
    }

    @Test
    @Order(0)
    void testGetBeerByBeerStyle() {
        AtomicReference<List<BeerDto>> beerResponse = new AtomicReference<>(new ArrayList<>());

        beerClient.getBeerByBeerStyle("Pale Ale").subscribe(response -> {
            log.info("### Response: " + response);
            beerResponse.get().add(response);
        });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals(2, beerResponse.get().size()));
    }

    @Test
    @Order(0)
    void testGetBeerById() {
        AtomicReference<BeerDto> beerResponse = new AtomicReference<>();

        beerClient.listBeerAsDtos().take(1)
            .flatMap(beerDto -> beerClient.getBeerById(beerDto.getId()))
            .subscribe(beerDto -> {
                log.info("### Response: " + beerDto);
                beerResponse.set(beerDto);
            });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> assertNotNull(beerResponse.get()));
    }

    @Test
    @Order(0)
    void testListBeer() {
        AtomicReference<String> beerListResponse = new AtomicReference<>();

        beerClient.listBeer().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.set(response);
        });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> assertNotNull(beerListResponse));
    }

    @Test
    @Order(0)
    void testListBeerMap() {
        AtomicReference<List<Map<String, Object>>> beerListResponse = new AtomicReference<>(new ArrayList<>());

        beerClient.listBeerMap().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.get().add(response);
        });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals(3, beerListResponse.get().size()));
    }

    @Test
    @Order(0)
    void testListBeerJsonNode() {
        AtomicReference<List<JsonNode>> beerListResponse = new AtomicReference<>(new ArrayList<>());

        beerClient.listBeerJsonNode().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.get().add(response);
        });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals(3, beerListResponse.get().size()));
    }

    @Test
    @Order(0)
    void testListBeerAsDtos() {
        AtomicReference<List<BeerDto>> beerListResponse = new AtomicReference<>(new ArrayList<>());

        beerClient.listBeerAsDtos().subscribe(response -> {
            log.info("### Response: " + response);
            beerListResponse.get().add(response);
        });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals(3, beerListResponse.get().size()));
    }

    @Test
    @Order(1)
    void testCreateBeer() {
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
                createdBeer.set(dto);
            });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertNotNull(createdBeer.get());
                assertEquals("Created Beer", createdBeer.get().getBeerName());
                assertNotNull(createdBeer.get().getId());
            });
    }

    @Test
    @Order(2)
    void updateBeer() {
        BeerDto beerToUpdate = beerClient.listBeerAsDtos().blockFirst();
        beerToUpdate.setBeerName("Updated Beer");
        AtomicReference<BeerDto> updatedBeer = new AtomicReference<>();

        beerClient.updateBeer(beerToUpdate)
            .subscribe(dto -> {
                log.info("### Response: " + dto);
                updatedBeer.set(dto);
            });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertNotNull(updatedBeer.get());
                assertEquals("Updated Beer", updatedBeer.get().getBeerName());
            });
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

        AtomicReference<BeerDto> patchedBeer = new AtomicReference<>();
        beerClient.patchBeer(beerToPatch)
            .subscribe(dto -> {
                log.info("### Response: " + dto);
                patchedBeer.set(dto);
            });

        await()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertNotNull(patchedBeer.get());
                assertEquals("Patched Beer", patchedBeer.get().getBeerName());
                assertEquals(beerStyleBeforePatch, patchedBeer.get().getBeerStyle());
                assertEquals(priceBeforePatch, patchedBeer.get().getPrice());
                assertEquals(upcBeforePatch, patchedBeer.get().getUpc());
                assertEquals(quantityBeforePatch, patchedBeer.get().getQuantityOnHand());
            });
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
            .doOnSuccess(mt -> atomicBoolean.set(true))
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
