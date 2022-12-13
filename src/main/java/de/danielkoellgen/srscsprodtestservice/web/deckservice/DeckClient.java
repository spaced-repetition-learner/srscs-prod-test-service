package de.danielkoellgen.srscsprodtestservice.web.deckservice;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.DeckRequestDto;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.DeckResponseDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope("singleton")
public class DeckClient {

    private final WebClient deckClient;

    private final String deckServiceAddress;

    private final Integer retryAttempts;
    private final Integer retryDelay;
    private final Double retryJitter;

    private final Logger logger = LoggerFactory.getLogger(DeckClient.class);

    @Autowired
    public DeckClient(@Value("${app.deckService.address}") String deckServiceAddress,
            @Value("${web.retry.attempts}") Integer retries,
            @Value("${web.retry.delay}") Integer delay,
            @Value("${web.retry.jitter}") Double jitter,
            WebClient webClient) {
        this.deckClient = webClient;
        this.retryAttempts = retries;
        this.retryDelay = delay;
        this.retryJitter = jitter;
        this.deckServiceAddress = deckServiceAddress;
    }

    public @NotNull Optional<Deck> createDeck(User user, DeckName deckName) {
        DeckRequestDto requestDto = new DeckRequestDto(user.getUserId(), deckName.getName());
        String uri = deckServiceAddress + "/decks";

        logger.trace("Calling POST {} to create a new Deck for '{}'...", uri,
                user.getUsername().getUsername());
        logger.debug("{}", requestDto);

        try {
            DeckResponseDto responseDto = deckClient.post().uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(DeckResponseDto.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. Deck created.");
            logger.debug("{}", responseDto);

            return Optional.of(new Deck(responseDto.deckId(), user, responseDto.isActive()));

        } catch (WebClientResponseException e) {
            logger.warn("Request failed externally. {}: {}.", e.getStatusCode(), e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public @NotNull List<DeckResponseDto> fetchDeckByUser(@NotNull UUID userId) {
        String uri = deckServiceAddress + "/decks?user-id=" + userId;

        logger.trace("Calling GET {} to fetch all User's Decks...", uri);

        try {
            List<DeckResponseDto> deckResponseDtos = deckClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToFlux(DeckResponseDto.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .collectList()
                    .block();
            assert deckResponseDtos != null;

            logger.trace("Request successful. {} Decks fetched for User {}.",
                    deckResponseDtos.size(), userId);
            return deckResponseDtos;

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);
            return List.of();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return List.of();
        }
    }

    public @NotNull Optional<DeckResponseDto> fetchDeck(@NotNull UUID deckId) {
        String uri = deckServiceAddress + "/decks/" + deckId;
        logger.trace("Calling GET {}...", uri);

        try {
            DeckResponseDto responseDto = deckClient
                    .get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(DeckResponseDto.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .block();
            assert responseDto != null;
            logger.debug("Request successful. {}", responseDto);
            return Optional.of(responseDto);

        } catch (WebClientResponseException e) {
            logger.warn("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public @NotNull Boolean disableDeck(@NotNull UUID deckId) {
        String uri = deckServiceAddress + "/decks/" + deckId;

        try {
            HttpStatus response = deckClient
                    .delete()
                    .uri(uri)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode() == HttpStatus.OK) {
                            return clientResponse.bodyToMono(HttpStatus.class);
                        } else {
                            return clientResponse.createException().flatMap(Mono::error);
                        }
                    })
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .block();
            return true;

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getStatusCode(),
                    e.getMessage(), e);
            return false;

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return false;
        }
    }
}
