package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ReviewAction;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.LoopActions;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.ParticipantStatus;
import de.danielkoellgen.srscsprodtestservice.domain.participant.repository.ParticipantRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Component
public class LoopIterationService {

    private final UserService userService;
    private final DeckService deckService;
    private final CardService cardService;
    private final CollaborationService collaborationService;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CollaborationRepository collaborationRepository;
    private final ParticipantRepository participantRepository;

    @Value("${app.loop.iteration.sleep-per-action-in-ms}")
    private Integer sleepPerAction;
    @Value("${app.loop.collab.user-count}")
    private Integer collabSize;
    @Value("${app.loop.iteration.user-count}")
    private Integer newUserCount;
    @Value("${app.loop.iteration.deck-count}")
    private Integer newDeckCount;
    @Value("${app.loop.iteration.card-count}")
    private Integer newCardCount;
    @Value("${app.loop.iteration.edit-count}")
    private Integer editCardCount;
    @Value("${app.loop.iteration.review-count}")
    private Integer reviewCardCount;
    @Value("${app.loop.iteration.start-collab-count}")
    private Integer newCollabCount;
    @Value("${app.loop.iteration.accept-collab-count}")
    private Integer acceptCollabCount;

    @Autowired
    public LoopIterationService(UserService userService, DeckService deckService, CardService cardService,
            CollaborationService collaborationService, UserRepository userRepository, DeckRepository deckRepository,
            ParticipantRepository participantRepository, CollaborationRepository collaborationRepository,
            CardRepository cardRepository) {
        this.userService = userService;
        this.deckService = deckService;
        this.cardService = cardService;
        this.collaborationService = collaborationService;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.collaborationRepository = collaborationRepository;
        this.participantRepository = participantRepository;
    }

    public void runIteration() {
        List<LoopActions> actions = generateRandomActions();
        executeActions(actions);
    }

    private @NotNull List<LoopActions> generateRandomActions() {
        Random random = new Random();
        List<LoopActions> actions = new ArrayList<>();
        IntStream.range(0, random.nextInt(newUserCount))
                .forEach(x -> actions.add(LoopActions.CREATE_USER));
        IntStream.range(0, random.nextInt(newDeckCount))
                .forEach(x -> actions.add(LoopActions.CREATE_DECK));
        IntStream.range(0, random.nextInt(newCardCount))
                .forEach(x -> actions.add(LoopActions.CREATE_CARD));
        IntStream.range(0, random.nextInt(editCardCount))
                .forEach(x -> actions.add(LoopActions.OVERRIDE_CARD));
        IntStream.range(0, random.nextInt(reviewCardCount))
                .forEach(x -> actions.add(LoopActions.REVIEW_CARD));
        IntStream.range(0, random.nextInt(newCollabCount))
                .forEach(x -> actions.add(LoopActions.START_COLLABORATION));
        IntStream.range(0, random.nextInt(acceptCollabCount))
                .forEach(x -> actions.add(LoopActions.ACCEPT_COLLABORATION));
        return actions;
    }

    private void executeActions(@NotNull List<LoopActions> actions) {
        Collections.shuffle(actions);
        actions.forEach(this::executeAction);
    }

    private void executeAction(@NotNull LoopActions action) {
        switch (action) {
            case CREATE_USER -> createNewUser();
            case CREATE_DECK -> createDeckWithRandomUser();
            case CREATE_CARD -> createCardWithRandomDeck();
            case OVERRIDE_CARD -> overrideRandomCard();
            case REVIEW_CARD -> reviewRandomCard();
            case START_COLLABORATION -> startCollaborationWithRandomUsers();
            case ACCEPT_COLLABORATION -> acceptCollaborationForRandomParticipate();
        }
        try {
            Thread.sleep(sleepPerAction);
        } catch (Exception e) {
            throw new RuntimeException("Thread.sleep failed. " + e.getMessage());
        }
    }

    private void createNewUser() {
        userService.externallyCreateUser(
                Username.makeRandomUsername(), MailAddress.makeRandomMailAddress()
        );
    }

    private void createDeckWithRandomUser() {
        User randomUser = pickRandomUser();
        deckService.externallyCreateDeck(randomUser);
    }

    private void createCardWithRandomDeck() {
        Deck randomDeck = pickRandomDeck();
        cardService.externallyCreateNewDefaultCard(randomDeck);
    }

    private void startCollaborationWithRandomUsers() {
        List<User> uniqueUsers = pickUniqueRandomUsers(collabSize);
        collaborationService.externallyStartCollaboration(uniqueUsers);
    }

    private void acceptCollaborationForRandomParticipate() {
        @Nullable Pair<Collaboration, Participant> randomParticipant = pickRandomInvitedParticipant();
        if (randomParticipant == null) {
            return;
        }
        collaborationService.externallyAcceptCollaboration(randomParticipant.getFirst(), randomParticipant.getSecond());
    }

    private void overrideRandomCard() {
        Card randomCard = pickRandomCard();
        cardService.externallyEditCardAsDefaultCard(randomCard.getCardId());
    }

    private void reviewRandomCard() {
        Card card = pickRandomCard();
        cardService.reviewCard(card.getCardId(), ReviewAction.NORMAL);
    }

    private @NotNull User pickRandomUser() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(users::add);

        Random random = new Random();
        return users.get(random.nextInt(users.size() - 1));
    }

    private @NotNull List<User> pickUniqueRandomUsers(Integer count) {
        List<User> users = new ArrayList<>();
        while (users.size() < count) {
            User randomUser = pickRandomUser();
            if (users.stream().map(User::getUserId).noneMatch(x -> x.equals(randomUser.getUserId())) ){
                users.add(randomUser);
            }
        }
        return users;
    }

    private @NotNull Deck pickRandomDeck() {
        List<Deck> decks = new ArrayList<>();
        deckRepository.findAll().iterator().forEachRemaining(decks::add);

        Random random = new Random();
        return decks.get(random.nextInt(decks.size() - 1));
    }

    private @NotNull Card pickRandomCard() {
        List<Card> cards = new ArrayList<>();
        cardRepository.findByIsActive(true).iterator().forEachRemaining(cards::add);

        Random random = new Random();
        while(true) {
            Card card = cardService.synchronizeCard(
                    cards.get(random.nextInt(cards.size() - 1)).getCardId()
            );
            if (card != null && card.getIsActive()) {
                return card;
            }
        }
    }

    private @Nullable Pair<Collaboration, Participant> pickRandomInvitedParticipant() {
        List<Participant> participants = new ArrayList<>();
        participantRepository.findAllByParticipantStatus(ParticipantStatus.INVITED).iterator().forEachRemaining(participants::add);

        if (participants.isEmpty()) {
            return null;
        }
        Random random = new Random();
        Participant randomParticipant = participants.get(random.nextInt(participants.size() - 1));
        Collaboration correspondingCollaboration = collaborationRepository
                .findByParticipants_ParticipantId(randomParticipant.getParticipantId());
        assert correspondingCollaboration != null;
        return Pair.of(correspondingCollaboration, randomParticipant);
    }
}
