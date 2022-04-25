package de.danielkoellgen.srscsprodtestservice.domain.deck.application;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.DeckClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DeckService {

    private final DeckClient deckClient;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    @Autowired
    public DeckService(DeckClient deckClient, UserRepository userRepository, DeckRepository deckRepository) {
        this.deckClient = deckClient;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    public @NotNull Deck externallyCreateDeck(@NotNull UUID userId) {
        User user = userRepository.findById(userId).get();
        Optional<Deck> optionalDeck = deckClient.createDeck(user, DeckName.makeRandomDeckName());
        if (optionalDeck.isEmpty()) {
            throw new RuntimeException("Failed to externally create Deck.");
        }
        Deck newDeck = optionalDeck.get();
        deckRepository.save(newDeck);
        return newDeck;
    }

    public void addExternallyCreatedDeck(@NotNull UUID deckId, @NotNull UUID userId) {
        try {
            User user = userRepository.findById(userId).get();
            Deck newDeck = new Deck(deckId, user, true);
            deckRepository.save(newDeck);

        } catch (Exception ignored) {
            //TODO Log this
        }
    }

    public void disableExternallyDisabledDeck(@NotNull UUID deckId) {
        try {
            Deck deck = deckRepository.findById(deckId).get();
            deck.disableDeck();
            deckRepository.save(deck);

        } catch (Exception ignored) {

        }
    }
}
