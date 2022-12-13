package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ReviewAction;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.LoopStatus;
import de.danielkoellgen.srscsprodtestservice.domain.loop.repository.LoopRepository;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.ParticipantStatus;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class LoopExecutionService implements Repositories, Services, Runnable {

    private final UserService userService;
    private final DeckService deckService;
    private final CardService cardService;
    private final CollaborationService collaborationService;

    private final UserSynchronizationService userSynchronizationService;
    private final DeckSynchronizationService deckSynchronizationService;
    private final CardSynchronizationService cardSynchronizationService;
    private final CollaborationSynchronizationService collaborationSynchronizationService;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CollaborationRepository collaborationRepository;
    private final LoopRepository loopRepository;

    private User user;
    private Long sleep;

    private static final Logger log = LoggerFactory.getLogger(LoopExecutionService.class);

    @Autowired
    public LoopExecutionService(UserService userService, DeckService deckService,
            CardService cardService, CollaborationService collaborationService,
            UserSynchronizationService userSynchronizationService,
            DeckSynchronizationService deckSynchronizationService,
            CardSynchronizationService cardSynchronizationService,
            CollaborationSynchronizationService collaborationSynchronizationService,
            UserRepository userRepository, DeckRepository deckRepository,
            CardRepository cardRepository, CollaborationRepository collaborationRepository,
            LoopRepository loopRepository) {
        this.userService = userService;
        this.deckService = deckService;
        this.cardService = cardService;
        this.collaborationService = collaborationService;
        this.userSynchronizationService = userSynchronizationService;
        this.deckSynchronizationService = deckSynchronizationService;
        this.cardSynchronizationService = cardSynchronizationService;
        this.collaborationSynchronizationService = collaborationSynchronizationService;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.collaborationRepository = collaborationRepository;
        this.loopRepository = loopRepository;
    }

    @Override
    public void run() {
        log.info("Worker started for '{}'...", user.getUsername());
        makeInitialDeck(user.getUserId());
        runLoop(user.getUserId(), sleep);
    }

    public void initializeWorker(User user, Long sleep) {
        this.user = user;
        this.sleep = sleep;
    }

    private void makeInitialDeck(UUID userId) {
        deckService.externallyCreateDeck(userId);
    }

    private void runLoop(UUID userId, Long sleep) {
        while (checkLoopIsActive()) {
            log.trace("Loop started... [username={}]", userId);
            synchronizeCollaborations(userId);
            takeAction(userId);
            acceptPendingCollaborations(userId);
            try {
                Thread.sleep(sleep);
            } catch (Exception ignored) { }
            log.trace("Loop completed. [username={}]", user.getUsername());
        }
    }

    private void synchronizeCollaborations(UUID userId) {
        List<UUID> collaborationIds = collaborationRepository
                .findAllByParticipants_UserId(userId)
                .stream()
                .map(x -> collaborationSynchronizationService.synchronizeCollaboration(x.getCollaborationId()))
                .map(Collaboration::getCollaborationId)
                .toList();
        log.info("All {} Collaborations synchronized.", collaborationIds.size());
        log.debug("Synchronized Collaborations: {}", collaborationIds);
    }

    private void acceptPendingCollaborations(UUID userId) {
        List<UUID> collaborationIds = collaborationRepository
                .findAllByParticipants_UserIdAndParticipants_ParticipantStatus(
                        userId, ParticipantStatus.INVITED)
                .stream()
                .map(Collaboration::getCollaborationId)
                .peek(x -> collaborationService.externallyAcceptCollaboration(x, userId))
                .toList();
        log.info("{} Invitations accepted for '{}'.", collaborationIds.size(), user.getUsername());
        log.debug("Accepted Collaborations: [username={}], {}", user.getUsername(), collaborationIds);
    }

    private Boolean checkLoopIsActive() {
        Boolean response = loopRepository.findById(LoopService.loopId)
                .map(x -> x.getLoopStatus().equals(LoopStatus.STARTED))
                .orElse(false);
        if (!response) {
            log.info("Loop ended for worker [username={}]", user.getUsername());
        }
        return response;
    }

    private void takeAction(UUID userId) {
        ActionCategory randomCategory = ActionCategory.chooseRandom();
        switch (randomCategory) {
            case DECK           -> DeckActions.chooseRandom()
                    .action
                    .apply(this)
                    .apply(this)
                    .accept(userId);
            case CARD           -> CardActions.chooseRandom()
                    .action
                    .apply(this)
                    .apply(this)
                    .accept(userId);
            case COLLABORATION  -> CollaborationActions.chooseRandom()
                    .action
                    .apply(this)
                    .apply(this)
                    .accept(userId);
        }
    }

    static <A extends Percentage> A chooseRandomByChance(List<A> options) {
        int rnd = new Random().nextInt(100);
        int counter = 0;
        for (A action: options) {
            counter = counter + action.getPercentage();
            if (rnd <= counter) {
                return action;
            }
        }
        return options.get(0);
    }

    static <A> Optional<A> chooseRandom(List<A> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(new Random().nextInt(list.size())));
        }
    }

    private enum ActionCategory implements Percentage {
        DECK(5),
        CARD(90),
        COLLABORATION(5);

        public final Integer percentage;

        ActionCategory(Integer percentage) {
            this.percentage = percentage;
        }

        public static ActionCategory chooseRandom() {
            return LoopExecutionService.chooseRandomByChance(
                    Arrays.stream(ActionCategory.values()).toList());
        }

        @Override
        public Integer getPercentage() {
            return percentage;
        }
    }

    static Function<Services, Function<Repositories, Consumer<UUID>>> disableRndDeck() {
        return services -> repositories -> userId -> {
            log.trace("Disabling random Deck with worker '{}'...", userId);
            List<Deck> availableDecks = repositories
                    .getDeckRepository()
                    .findAllByUser_UserIdAndIsActive(userId, true);
            chooseRandom(availableDecks).ifPresent(rndDeck -> services.getDeckService()
                        .externallyDisableDeck(rndDeck.getDeckId()));
        };
    }

    static Function<Services, Function<Repositories, Consumer<UUID>>> createRndDeck() {
        return services -> repositories -> userId -> {
            log.trace("Creating random Deck with worker '{}'...", userId);
            services.getDeckService()
                    .externallyCreateDeck(userId);
        };
    }

    private enum DeckActions implements Percentage {
        DISABLE(30, disableRndDeck()),
        CREATE(70, createRndDeck());

        public final Integer percentage;

        public final Function<Services, Function<Repositories, Consumer<UUID>>> action;

        DeckActions(Integer percentage, Function<Services, Function<Repositories,
                Consumer<UUID>>> action) {
            this.percentage = percentage;
            this.action = action;
        }

        public static DeckActions chooseRandom() {
            return LoopExecutionService.chooseRandomByChance(
                    Arrays.stream(DeckActions.values()).toList());
        }

        @Override
        public Integer getPercentage() {
            return percentage;
        }
    }

    static Function<Services, Function<Repositories, Consumer<UUID>>> createRndCard() {
        return services -> repositories -> userId -> {
            log.trace("Creating random Card with worker '{}'...", userId);
            List<UUID> availableDecks = repositories
                    .getDeckRepository()
                    .findAllByUser_UserIdAndIsActive(userId, true)
                    .stream()
                    .map(Deck::getDeckId)
                    .toList();
            chooseRandom(availableDecks).ifPresent(rndDeckId -> services.getCardService()
                    .externallyCreateEmptyDefaultCard(rndDeckId));
        };
    }

    static Function<Services, Function<Repositories, Consumer<UUID>>> overrideRndCard() {
        return services -> repositories -> userId -> {
            log.trace("Overriding random Card with worker '{}'...", userId);
            List<UUID> availableCards = repositories
                    .getCardRepository()
                    .findAllByIsActiveAndDeck_User_UserIdAndDeck_IsActive(true, userId, true)
                    .stream()
                    .map(Card::getCardId)
                    .toList();
            chooseRandom(availableCards).ifPresent(rndCardId -> services.getCardService()
                    .externallyOverrideCardAsEmptyDefaultCard(rndCardId));
        };
    }

    static Function<Services, Function<Repositories, Consumer<UUID>>> reviewRndCard() {
        return services -> repositories -> userId -> {
            log.trace("Reviewing random Card with worker '{}'...", userId);
            List<UUID> availableCards = repositories
                    .getCardRepository()
                    .findAllByIsActiveAndDeck_User_UserIdAndDeck_IsActive(true, userId, true)
                    .stream()
                    .map(Card::getCardId)
                    .toList();
            chooseRandom(availableCards).ifPresent(rndCardId -> {
                ReviewAction rndAction = chooseRandom(Arrays.stream(ReviewAction.values()).toList())
                        .orElse(ReviewAction.NORMAL);
                services.getCardService()
                        .reviewCard(rndCardId, rndAction);
            });
        };
    }

    private enum CardActions implements Percentage {
        CREATE(30, createRndCard()),
        OVERRIDE(10, overrideRndCard()),
        REVIEW(60, reviewRndCard());

        public final Integer percentage;

        public final Function<Services, Function<Repositories, Consumer<UUID>>> action;

        CardActions(Integer percentage, Function<Services, Function<Repositories,
                Consumer<UUID>>> action) {
            this.percentage = percentage;
            this.action = action;
        }

        public static CardActions chooseRandom() {
            return LoopExecutionService.chooseRandomByChance(
                    Arrays.stream(CardActions.values()).toList());
        }

        @Override
        public Integer getPercentage() {
            return percentage;
        }
    }

    static Function<Services, Function<Repositories, Consumer<UUID>>> startCollaboration() {
        return services -> repositories -> userId -> {
            log.trace("Starting random Collaboration with worker '{}'...", userId);
            List<UUID> availableUsers = repositories
                    .getUserRepository()
                    .findAllByIsActive(true)
                    .stream()
                    .map(User::getUserId)
                    .toList();
            services.getCollaborationService()
                    .externallyCreateCollaboration(availableUsers);
        };
    }

    static Function<Services, Function<Repositories, Consumer<UUID>>> endParticipation() {
        return services -> repositories -> userId -> {
            log.trace("Ending random Participation for with worker '{}'...", userId);
            List<UUID> availableCollaborations = repositories
                    .getCollaborationRepository()
                    .findAllByParticipants_UserIdAndParticipants_ParticipantStatus(
                            userId, ParticipantStatus.INVITATION_ACCEPTED)
                    .stream()
                    .map(Collaboration::getCollaborationId)
                    .toList();
            chooseRandom(availableCollaborations).ifPresent(rndCollaborationId -> services
                    .getCollaborationService()
                    .externallyEndCollaboration(rndCollaborationId, userId));
        };
    }

    private enum CollaborationActions implements Percentage {
        CREATE(80, startCollaboration()),
        DISABLE(20, endParticipation());

        public final Integer percentage;

        public final Function<Services, Function<Repositories, Consumer<UUID>>> action;

        CollaborationActions(Integer percentage, Function<Services, Function<Repositories,
                Consumer<UUID>>> action) {
            this.percentage = percentage;
            this.action = action;
        }

        public static CollaborationActions chooseRandom() {
            return LoopExecutionService.chooseRandomByChance(
                    Arrays.stream(CollaborationActions.values()).toList());
        }

        @Override
        public Integer getPercentage() {
            return percentage;
        }
    }

    interface Percentage {

        Integer getPercentage();
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    public DeckRepository getDeckRepository() {
        return deckRepository;
    }

    @Override
    public CardRepository getCardRepository() {
        return cardRepository;
    }

    @Override
    public CollaborationRepository getCollaborationRepository() {
        return collaborationRepository;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public DeckService getDeckService() {
        return deckService;
    }

    @Override
    public CardService getCardService() {
        return cardService;
    }

    @Override
    public CollaborationService getCollaborationService() {
        return collaborationService;
    }

    @Override
    public UserSynchronizationService getUserSynchronizationService() {
        return userSynchronizationService;
    }

    @Override
    public DeckSynchronizationService getDeckSynchronizationService() {
        return deckSynchronizationService;
    }

    @Override
    public CardSynchronizationService getCardSynchronizationService() {
        return cardSynchronizationService;
    }

    @Override
    public CollaborationSynchronizationService getCollaborationSynchronizationService() {
        return collaborationSynchronizationService;
    }
}
