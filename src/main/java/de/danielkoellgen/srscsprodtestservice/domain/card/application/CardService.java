package de.danielkoellgen.srscsprodtestservice.domain.card.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.CardType;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.CardClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardService {

    private final CardClient cardClient;

    private final CardRepository cardRepository;

    @Autowired
    public CardService(CardClient cardClient, CardRepository cardRepository) {
        this.cardClient = cardClient;
        this.cardRepository = cardRepository;
    }

    public @NotNull Card externallyCreateNewDefaultCard(@NotNull Deck deck) {
        Optional<Card> optionalCard = cardClient.createEmptyCard(deck, CardType.DEFAULT);
        if (optionalCard.isEmpty()) {
            throw new RuntimeException("Failed to externally create Card.");
        }
        Card newCard = optionalCard.get();
        cardRepository.save(newCard);
        return newCard;
    }
}
