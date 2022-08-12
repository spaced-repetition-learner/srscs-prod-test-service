package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.loop.repository.LoopRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class LoopStartUpService {

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

//    private final ApplicationContext context;

    private static final Logger log = LoggerFactory.getLogger(LoopStartUpService.class);

    @Autowired
    public LoopStartUpService(UserService userService, DeckService deckService,
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

    public void startLoop(Integer size, Long sleepTime) {
        cleanPersistentData();
        List<User> users = externallyCreateUsers(size);
        log.info("{} Users externally created. {}", size,
                users.stream().map(User::getUsername).toList());

        users.forEach(user -> {
//            LoopExecutionService executionService = context.getBean(LoopExecutionService.class);
            LoopExecutionService executionService = makeNewThreadService();
            executionService.initializeWorker(user, sleepTime);

            Thread thread = new Thread(executionService);
            thread.setDaemon(false);
            thread.start();
        });
    }

    private void cleanPersistentData() {
        cardRepository.deleteAll();
        collaborationRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
        log.info("Local Data fully purged.");
    }

    private List<User> externallyCreateUsers(Integer amount) {
        return IntStream.range(0, amount)
                .mapToObj(x -> userService.externallyCreateUser(
                        Username.makeRandomUsername(), MailAddress.makeRandomMailAddress()))
                .toList();
    }

    private LoopExecutionService makeNewThreadService() {
        return new LoopExecutionService(userService, deckService, cardService, collaborationService,
                userSynchronizationService, deckSynchronizationService, cardSynchronizationService,
                collaborationSynchronizationService, userRepository, deckRepository, cardRepository,
                collaborationRepository, loopRepository);
    }
}
