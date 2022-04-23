package de.danielkoellgen.srscsprodtestservice.web.deckservice;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.DeckRequestDto;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.DeckResponseDto;
import de.danielkoellgen.srscsprodtestservice.web.userservice.UserClient;
import de.danielkoellgen.srscsprodtestservice.web.userservice.dto.UserResponseDto;
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
public class DeckClient {

    private final WebClient deckClient;

    private final Logger logger = LoggerFactory.getLogger(DeckClient.class);

    @Autowired
    public DeckClient(@Value("//${app.deckService.address}") String deckServiceAddress) {
        this.deckClient = WebClient.create(deckServiceAddress);
    }

    public @NotNull Optional<Deck> createDeck(User user, DeckName deckName) {
        DeckRequestDto requestDto = new DeckRequestDto(user.getUserId(), deckName.getName());

        try {
            DeckResponseDto responseDto = deckClient.post().uri("/decks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(DeckResponseDto.class)
                    .block();
            assert responseDto != null;
            return Optional.of(new Deck(responseDto.deckId(), user, responseDto.isActive()));

        } catch (WebClientResponseException e) {
            return Optional.empty();

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
