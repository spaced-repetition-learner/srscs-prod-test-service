package de.danielkoellgen.srscsprodtestservice.domain.card.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.CardType;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ReviewAction;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.CardClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CardService {

    private final CardClient cardClient;

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;

    private final Logger logger = LoggerFactory.getLogger(CardService.class);

    @Autowired
    public CardService(CardClient cardClient, DeckRepository deckRepository, CardRepository cardRepository) {
        this.cardClient = cardClient;
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
    }

    public @NotNull Card externallyCreateEmptyDefaultCard(@NotNull UUID deckId) {
        logger.trace("Externally creating an empty Default-Card.");
        logger.trace("Fetching Deck by id {}...", deckId);
        Deck deck = deckRepository.findById(deckId).orElseThrow();
        logger.debug("{}", deck);

        if (!deck.getIsActive()) {
            throw new RuntimeException("Not allowed to add Cards to a disabled Deck.");
        }

        Optional<Card> optionalCard = cardClient.createEmptyCard(deck, CardType.DEFAULT);
        if (optionalCard.isEmpty()) {
            throw new RuntimeException("Failed to externally create Card.");
        }
        Card newCard = optionalCard.get();
        logger.info("Empty Default-Card externally created.");
        logger.debug("{}", newCard);

        cardRepository.save(newCard);
        logger.trace("New Default-Card saved.");
        return newCard;
    }

    public @NotNull Card externallyOverrideCardAsEmptyDefaultCard(@NotNull UUID rootCardId) {
        logger.trace("Externally overriding Root-Card as an empty Default-Card...");
        logger.trace("Fetching Root-Card by id {}...", rootCardId);
        Card rootCard = cardRepository.findById(rootCardId).orElseThrow();
        logger.debug("{}", rootCard);

        Optional<Card> newCard = cardClient.overrideCardAsEmptyCard(rootCard, CardType.DEFAULT);
        if (newCard.isEmpty()) {
            throw new RuntimeException("Failed to externally edit Card.");
        }
        logger.info("Root-Card externally overridden as an empty Default-Card.");
        logger.debug("{}", newCard.get());
        cardRepository.save(newCard.get());
        logger.trace("New Card saved.");

        rootCard.disableCard();
        cardRepository.save(rootCard);
        logger.trace("Disabled Root-Card saved.");

        return newCard.get();
    }

    public void reviewCard(@NotNull UUID cardId, @NotNull ReviewAction reviewAction) {
        logger.trace("Reviewing Card as {}...", reviewAction);
        logger.trace("Fetching Card by id {}...", cardId);
        Card card = cardRepository.findById(cardId).orElseThrow();
        logger.debug("{}", card);

        cardClient.reviewCard(card, reviewAction);
        logger.info("Card reviewed as {}.", reviewAction);
    }

    public @Nullable Card synchronizeCard(@NotNull UUID cardId) {
        logger.trace("Synchronizing Card...");
        Optional<Card> optionalCard = cardClient.fetchCard(cardId);

        if (optionalCard.isEmpty()) {
            throw new RuntimeException("Failed to fetch Card while synchronizing.");
        }
        Card card = optionalCard.get();
        cardRepository.save(card);
        logger.info("Card synchronized.");
        logger.debug("{}", card);
        logger.trace("Card saved.");

        return card;
    }
}
