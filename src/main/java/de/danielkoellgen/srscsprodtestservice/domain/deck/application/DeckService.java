package de.danielkoellgen.srscsprodtestservice.domain.deck.application;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.DeckClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeckService {

    private final DeckClient deckClient;

    private final DeckRepository deckRepository;

    @Autowired
    public DeckService(DeckClient deckClient, DeckRepository deckRepository) {
        this.deckClient = deckClient;
        this.deckRepository = deckRepository;
    }

    public @NotNull Deck externallyCreateDeck(@NotNull User user) {
        Optional<Deck> optionalDeck = deckClient.createDeck(user, DeckName.makeRandomDeckName());
        if (optionalDeck.isEmpty()) {
            throw new RuntimeException("Failed to externally create Deck.");
        }
        Deck newDeck = optionalDeck.get();
        deckRepository.save(newDeck);
        return newDeck;
    }
}
