package de.danielkoellgen.srscsprodtestservice.domain.deck.application;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.DeckClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DeckService {

    private final DeckClient deckClient;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    private final Logger logger = LoggerFactory.getLogger(DeckService.class);

    @Autowired
    public DeckService(DeckClient deckClient, UserRepository userRepository, DeckRepository deckRepository) {
        this.deckClient = deckClient;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    public @NotNull Deck externallyCreateDeck(@NotNull UUID userId) {
        logger.trace("Externally creating Deck...");
        logger.trace("Fetching User by id {}.", userId);
        User user = userRepository.findById(userId).orElseThrow();
        logger.trace("User '{}' fetched.", user.getUsername().getUsername());

        Optional<Deck> optionalDeck = deckClient.createDeck(user, DeckName.makeRandomDeckName());
        if (optionalDeck.isEmpty()) {
            throw new RuntimeException("Failed to externally create Deck.");
        }
        Deck newDeck = optionalDeck.get();
        logger.info("Deck externally created for '{}'.", user.getUsername().getUsername());
        logger.debug("{}", newDeck);

        deckRepository.save(newDeck);
        logger.trace("New Deck saved.");
        return newDeck;
    }

    public Deck addExternallyCreatedDeck(@NotNull UUID deckId, @NotNull UUID userId) {
        logger.trace("Adding externally created Deck...");

        logger.trace("Check if Deck already exists...");
        Optional<Deck> optExistingDeck = deckRepository.findById(deckId);
        if (optExistingDeck.isPresent()) {
            logger.debug("Deck already exists by id {}.", deckId);
            return optExistingDeck.get();
        }

        logger.trace("Deck does not exist. Continuing...");
        logger.trace("Fetching User by id {}...", userId);
        User user = userRepository.findById(userId).orElseThrow();
        logger.trace("User '{}' fetched.", user.getUsername().getUsername());

        Deck newDeck = new Deck(deckId, user, true);
        deckRepository.save(newDeck);
        logger.trace("Deck saved.");
        logger.info("Added externally created Deck for '{}'.", user.getUsername().getUsername());
        logger.debug("{}", newDeck);

        return newDeck;
    }

    public void disableExternallyDisabledDeck(@NotNull UUID deckId) {
        logger.trace("Disabling externally disabled Deck...");
        logger.trace("Fetching Deck by id {}...", deckId);
        Deck deck = deckRepository.findById(deckId).orElseThrow();
        logger.trace("Deck fetched.");

        deck.disableDeck();
        deckRepository.save(deck);
        logger.trace("Disabled Deck saved.");
        logger.info("Deck disabled.");
    }
}
