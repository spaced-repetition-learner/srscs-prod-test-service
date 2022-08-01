package de.danielkoellgen.srscsprodtestservice.domain.collaboration;

import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.participant.repository.ParticipantRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CollaborationServiceIntegrationTest {

    private final UserService userService;
    private final CardService cardService;
    private final CollaborationService collaborationService;

    private final CardSynchronizationService cardSynchronizationService;
    private final CollaborationSynchronizationService collaborationSynchronizationService;

    private final CardRepository cardRepository;
    private final CollaborationRepository collaborationRepository;
    private final ParticipantRepository participantRepository;

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;


    @Autowired
    public CollaborationServiceIntegrationTest(UserService userService,
            CollaborationService collaborationService, CardService cardService,
            CollaborationSynchronizationService collaborationSynchronizationService,
            CardSynchronizationService cardSynchronizationService, CardRepository cardRepository,
            CollaborationRepository collaborationRepository,
            ParticipantRepository participantRepository, DeckRepository deckRepository,
            UserRepository userRepository) {
        this.userService = userService;
        this.cardService = cardService;
        this.collaborationService = collaborationService;

        this.cardSynchronizationService = cardSynchronizationService;
        this.collaborationSynchronizationService = collaborationSynchronizationService;

        this.cardRepository = cardRepository;
        this.collaborationRepository = collaborationRepository;
        this.participantRepository = participantRepository;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() {}

    @AfterEach
    public void cleanUp() {
        cardRepository.deleteAll();
        participantRepository.deleteAll();
        collaborationRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    /*
        Creates new users which are then used to start a new collaboration. Afterwards the
                collaboration is accepted by each individual user.

        If successful, each user should have a unique deck which is linked to the collaboration.
     */
    @Test
    public void shouldAllowToCreateAndAcceptNewCollaborations() throws InterruptedException {
        // given
        List<UUID> usersById = IntStream.range(0, 3)
                .mapToObj(i -> userService.externallyCreateUser(Username.makeRandomUsername(),
                        MailAddress.makeRandomMailAddress()))
                .map(User::getUserId)
                .toList();

        // when
        Thread.sleep(600);
        Collaboration collaboration = collaborationService.externallyCreateCollaboration(usersById);
        usersById.forEach(userId -> {
            collaborationService.externallyAcceptCollaboration(collaboration.getCollaborationId(),
                    userId);
        });

        // then
        Thread.sleep(1000);
        Collaboration updatedCollaboration = collaborationSynchronizationService
                .synchronizeCollaboration(collaboration.getCollaborationId());
        assertThat(updatedCollaboration.getParticipants())
                .hasSize(2);
        assertThat(updatedCollaboration.getParticipants().get(0).getDeck())
                .isNotNull();
        assertThat(updatedCollaboration.getParticipants().get(1).getDeck())
                .isNotNull();
    }

    @Test
    public void shouldAllowToExternallyAcceptParticipations() throws InterruptedException {
        // given
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            users.add(userService.externallyCreateUser(
                    Username.makeRandomUsername(), MailAddress.makeRandomMailAddress())
            );
        }
        Thread.sleep(50);
        Collaboration collaboration = collaborationService.externallyStartCollaboration(users.stream()
                .map(User::getUserId)
                .toList()
        );
        Thread.sleep(50);

        // when
        collaborationService.externallyAcceptCollaboration(
                collaboration.getCollaborationId(), collaboration.getParticipants().get(0).getParticipantId()
        );

    @Test
    public void shouldCloneOverrideOnCardOverrideWhenCollaborating() throws InterruptedException {
        // given
        Collaboration collaboration = externallyCreateCollaboration(2);

        // when
        Card initialCard = cardService.externallyCreateEmptyDefaultCard(
                collaboration.getParticipants().get(0).getDeck().getDeckId());
        Thread.sleep(500);
        Card overrideCard = cardService.externallyOverrideCardAsEmptyDefaultCard(
                initialCard.getCardId());
        Thread.sleep(1000);

        // then
        List<Card> clonedCards = cardSynchronizationService.synchronizeCardsByDeck(
                collaboration.getParticipants().get(1).getDeck().getDeckId());
        BiFunction<List<Card>, Boolean, List<Card>> filteredByStatus = (l, b) -> l.stream()
                .filter(x -> x.getIsActive().equals(b))
                .toList();
        assertThat(clonedCards)
                .hasSize(2);
        assertThat(filteredByStatus.apply(clonedCards, true))
                .hasSize(1);
        assertThat(filteredByStatus.apply(clonedCards, false))
                .hasSize(1);

    }

    private Collaboration externallyCreateCollaboration(Integer size) {
        List<UUID> usersById = IntStream.range(0, size)
                .mapToObj(i -> userService.externallyCreateUser(Username.makeRandomUsername(),
                        MailAddress.makeRandomMailAddress()))
                .map(User::getUserId)
                .toList();
        Collaboration collaboration = collaborationService.externallyCreateCollaboration(usersById);
        usersById.forEach(userId -> {
            collaborationService.externallyAcceptCollaboration(collaboration.getCollaborationId(),
                    userId);
        });
        return collaboration;
    }
}
