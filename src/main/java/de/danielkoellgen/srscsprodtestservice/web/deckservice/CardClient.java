package de.danielkoellgen.srscsprodtestservice.web.deckservice;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.CardType;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.CardRequestDto;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.CardResponseDto;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.CardTypeDto;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.DeckResponseDto;
import de.danielkoellgen.srscsprodtestservice.web.userservice.UserClient;
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

import java.util.Optional;

@Component
@Scope("singleton")
public class CardClient {

    private final WebClient cardClient;

    private final Logger logger = LoggerFactory.getLogger(UserClient.class);

    @Autowired
    public CardClient(@Value("//${app.userService.address}") String deckServiceAddress) {
        this.cardClient = WebClient.create(deckServiceAddress);
    }

    public @NotNull Optional<Card> createEmptyDefaultCard(@NotNull Deck deck, @NotNull CardType cardType) {
        CardRequestDto requestDto = new CardRequestDto(
                deck.getDeckId(), CardTypeDto.fromCardType(CardType.DEFAULT), null, null, null);

        try {
            CardResponseDto responseDto = cardClient.post().uri("/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CardResponseDto.class)
                    .block();
            assert responseDto != null;
            return Optional.of(new Card(responseDto.cardId(), deck, responseDto.getIsActive()));

        } catch (WebClientResponseException e) {
            return Optional.empty();

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
