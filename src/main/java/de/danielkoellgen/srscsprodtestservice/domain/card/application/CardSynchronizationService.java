package de.danielkoellgen.srscsprodtestservice.domain.card.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.CardClient;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.CardResponseDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardSynchronizationService {

    private final CardClient cardClient;

    private final DeckSynchronizationService deckSynchronizationService;

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;

    private static final Logger logger = LoggerFactory.getLogger(CardSynchronizationService.class);

    @Autowired
    public CardSynchronizationService(CardClient cardClient,
            DeckSynchronizationService deckSynchronizationService, DeckRepository deckRepository,
            CardRepository cardRepository) {
        this.cardClient = cardClient;
        this.deckSynchronizationService = deckSynchronizationService;
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
    }

    public List<Card> synchronizeCardsByDeck(@NotNull UUID deckId) {
        return cardClient.fetchCardsByDeck(deckId).stream()
                .map(this::synchronizeCard)
                .toList();
    }

    public Card synchronizeCard(@NotNull CardResponseDto remoteCard) {
        Optional<Card> optLocalCard = cardRepository.findById(remoteCard.cardId());
        if (optLocalCard.isEmpty()) {
            logger.warn("Local card does not exist.");
        }

        Card syncedCard = mergeRemoteAndLocalCard(remoteCard, optLocalCard);
        cardRepository.save(syncedCard);
        return syncedCard;
    }

    private Card mergeRemoteAndLocalCard(@NotNull CardResponseDto remoteCard, @NotNull Optional<Card> localCard) {
        if (localCard.isEmpty()) {
            Deck deck = deckRepository.findById(remoteCard.deckId())
                    .orElseGet(() -> {
                        logger.warn("Deck locally not found. Attempting to synchronize...");
                        return deckSynchronizationService
                                .synchronizeDeck(remoteCard.deckId());
                    });
            Card card = Card.makeFromDto(remoteCard, deck);
            logger.info("Local card created from remote.");
            return card;
        }
        localCard.get().update(remoteCard);
        logger.info("Local card updated with remote.");
        return localCard.get();
    }
}
