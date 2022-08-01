package de.danielkoellgen.srscsprodtestservice.web.deckservice;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.CardType;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ReviewAction;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.*;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope("singleton")
public class CardClient {

    private final WebClient cardClient;

    private final DeckRepository deckRepository;

    private final String deckServiceAddress;

    private final Logger logger = LoggerFactory.getLogger(CardClient.class);

    @Autowired
    public CardClient(
            @Value("${app.deckService.address}") String deckServiceAddress, WebClient webclient,
            DeckRepository deckRepository) {
        this.cardClient = webclient;
        this.deckRepository = deckRepository;
        this.deckServiceAddress = deckServiceAddress;
    }

    public @NotNull Optional<Card> createEmptyCard(@NotNull Deck deck, @NotNull CardType cardType) {
        CardRequestDto requestDto = new CardRequestDto(
                deck.getDeckId(), CardTypeDto.fromCardType(cardType), null, null, null
        );
        String uri = deckServiceAddress + "/cards";

        logger.trace("Calling POST {} to create an empty {}-Card...", uri, cardType);
        logger.debug("{}", requestDto);

        try {
            CardResponseDto responseDto = cardClient.post().uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CardResponseDto.class)
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. Empty {}-Card created.", cardType);
            logger.debug("{}", responseDto);

            return Optional.of(new Card(responseDto.cardId(), deck, responseDto.getIsActive()));

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(), e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public @NotNull Optional<Card> overrideCardAsEmptyCard(@NotNull Card rootCard,
            @NotNull CardType cardType) {
        CardRequestDto requestDto = new CardRequestDto(
                rootCard.getDeck().getDeckId(),
                CardTypeDto.fromCardType(cardType),
                null, null, null
        );
        String uri = deckServiceAddress + "/cards/" + rootCard.getCardId();

        logger.trace("Calling POST {} to override as an empty {}-Card.", uri, cardType);
        logger.debug("{}", requestDto);

        try {
            CardResponseDto responseDto = cardClient.post().uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CardResponseDto.class)
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. Card overridden.");
            logger.debug("{}", responseDto);

            return Optional.of(new Card(responseDto.cardId(), rootCard.getDeck(),
                    responseDto.getIsActive()));

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public void reviewCard(@NotNull Card card, @NotNull ReviewAction reviewAction) {
        ReviewRequestDto requestDto = new ReviewRequestDto(
                ReviewActionDto.fromReviewAction(reviewAction));
        String uri = deckServiceAddress+"/cards/" + card.getCardId() + "/scheduler/activity/review";

        logger.trace("Calling POST {} to review as {}.", uri, reviewAction);
        logger.debug("{}", requestDto);

        try {
            cardClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error));

            logger.trace("Request successful. Card reviewed.");

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
        }
    }

    public @NotNull Optional<Card> fetchCard(@NotNull UUID cardId) {
        String uri = deckServiceAddress + "/cards/" + cardId;

        logger.trace("Calling GET {}...", uri);

        try {
            CardResponseDto responseDto = cardClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CardResponseDto.class)
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. Card fetched.");
            logger.debug("{}", responseDto);

            logger.trace("Fetching Deck by id to map from CardResponseDTO to Card...");
            Deck deck = deckRepository.findById(responseDto.deckId()).orElseThrow();

            return Optional.of(new Card(responseDto.cardId(), deck, responseDto.getIsActive()));

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public @NotNull List<CardResponseDto> fetchCardsByDeck(@NotNull UUID deckId) {
        String uri = deckServiceAddress + "/cards?deck-id=" + deckId;

        logger.trace("Calling GET {} to fetch all Deck's Cards...", uri);

        try {
            List<CardResponseDto> responseDtos = cardClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToFlux(CardResponseDto.class)
                    .collectList()
                    .block();
            assert responseDtos != null;

            logger.trace("Request successful. {} Cards fetched for Deck {}.", responseDtos.size(), deckId);
            return responseDtos;

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(), e.getMessage(), e);
            return List.of();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return List.of();
        }
    }
}
