package de.danielkoellgen.srscsprodtestservice.domain.deck.application;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.DeckClient;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.DeckResponseDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeckSynchronizationService {

    private final DeckClient deckClient;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    private final UserSynchronizationService userSynchronizationService;

    private static final Logger logger = LoggerFactory.getLogger(DeckSynchronizationService.class);

    @Autowired
    public DeckSynchronizationService(DeckClient deckClient, UserRepository userRepository,
            DeckRepository deckRepository, UserSynchronizationService userSynchronizationService) {
        this.deckClient = deckClient;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.userSynchronizationService = userSynchronizationService;
    }

    public List<Deck> synchronizeAllUserDecks(@NotNull UUID userId) {
        return deckClient.fetchDeckByUser(userId)
                .stream()
                .map(this::synchronizeDeck)
                .toList();
    }

    public Deck synchronizeDeck(@NotNull DeckResponseDto remoteDeck) {
        logger.trace("Synchronizing local- w/ remote deck '{}'...", remoteDeck.deckId());
        Optional<Deck> optLocalDeck = deckRepository.findById(remoteDeck.deckId());

        if (optLocalDeck.isEmpty()) {
            logger.warn("Local deck does not exist.");
        }
        Deck syncedDeck = mergeRemoteAndLocalDeck(remoteDeck, optLocalDeck);
        deckRepository.save(syncedDeck);
        return syncedDeck;
    }

    private Deck mergeRemoteAndLocalDeck(@NotNull DeckResponseDto remoteDeck,
            @NotNull Optional<Deck> optLocalDeck) {
        if (optLocalDeck.isEmpty()) {
            Optional<User> optUser = userRepository.findById(remoteDeck.userId());
            User user;
            if (optUser.isEmpty()) {
                logger.warn("User locally not found. Attempting to synchronize user...");
                user = userSynchronizationService.synchronizeUser(remoteDeck.userId());
            } else {
                user = optUser.get();
            }
            logger.info("Local deck created from remote.");
            return Deck.makeFromDto(remoteDeck, user);
        }
        Deck localDeck = optLocalDeck.get();
        localDeck.update(remoteDeck);
        logger.info("Local deck updated from remote.");
        return localDeck;
    }
}
