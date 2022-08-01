package de.danielkoellgen.srscsprodtestservice.web.deckservice;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.CardType;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ReviewAction;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.*;
import de.danielkoellgen.srscsprodtestservice.web.userservice.UserClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
                deck.getDeckId(), CardTypeDto.fromCardType(cardType), null, null, null);

        logger.debug("Requesting Deck-Service to create a new Card. Address is POST {}",
                deckServiceAddress+"/cards");
        logger.trace("{}", requestDto);

        try {
            CardResponseDto responseDto = cardClient.post().uri(deckServiceAddress + "/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CardResponseDto.class)
                    .block();
            assert responseDto != null;
            logger.debug("Request successful.");
            logger.trace("{}", responseDto);
            return Optional.of(new Card(responseDto.cardId(), deck, responseDto.getIsActive()));

        } catch (WebClientResponseException e) {
            logger.error("Request failed. {}", e.getMessage());
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed. {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void reviewCard(@NotNull Card card, @NotNull ReviewAction reviewAction) {
        ReviewRequestDto requestDto = new ReviewRequestDto(ReviewActionDto.fromReviewAction(reviewAction));

        try {
            cardClient.post()
                    .uri(deckServiceAddress+"/cards/" + card.getCardId() + "/scheduler/activity/review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error));

        } catch (WebClientResponseException e) {
            return;

        } catch (Exception e) {
            return;
        }
    }

    public @NotNull Optional<Card> editCardAsEmpty(@NotNull Card rootCard, @NotNull CardType cardType) {
        CardRequestDto requestDto = new CardRequestDto(
                rootCard.getDeck().getDeckId(),
                CardTypeDto.fromCardType(cardType),
                null, null, null
        );

        try {
            CardResponseDto responseDto = cardClient.post().uri(deckServiceAddress + "/cards/" + rootCard.getCardId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CardResponseDto.class)
                    .block();
            assert responseDto != null;
            return Optional.of(new Card(responseDto.cardId(), rootCard.getDeck(), responseDto.getIsActive()));

        } catch (WebClientResponseException e) {
            return Optional.empty();

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public @NotNull Optional<Card> fetchCard(@NotNull UUID cardId) {
        try {
            CardResponseDto responseDto = cardClient.get().uri(deckServiceAddress + "/cards/" + cardId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CardResponseDto.class)
                    .block();
            assert responseDto != null;
            Deck deck = deckRepository.findById(responseDto.deckId()).get();
            return Optional.of(new Card(responseDto.cardId(), deck, responseDto.getIsActive()));

        } catch (WebClientResponseException e) {
            return Optional.empty();

        } catch (Exception e) {
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
