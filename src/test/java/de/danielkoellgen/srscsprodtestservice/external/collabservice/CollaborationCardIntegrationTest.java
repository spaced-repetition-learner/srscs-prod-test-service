package de.danielkoellgen.srscsprodtestservice.external.collabservice;

import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.ParticipantStatus;
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
public class CollaborationCardIntegrationTest {

    private final UserService userService;
    private final DeckService deckService;
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
    public CollaborationCardIntegrationTest(UserService userService, DeckService deckService,
            CollaborationService collaborationService, CardService cardService,
            CollaborationSynchronizationService collaborationSynchronizationService,
            CardSynchronizationService cardSynchronizationService, CardRepository cardRepository,
            CollaborationRepository collaborationRepository,
            ParticipantRepository participantRepository, DeckRepository deckRepository,
            UserRepository userRepository) {
        this.userService = userService;
        this.deckService = deckService;
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
        collaborationRepository.deleteAll();
        participantRepository.deleteAll();
        cardRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    /*
        Given an active collaboration of two users,
        when user-1 adds two cards to his deck,
        then user-2 should also receive a copy of those two cards.
     */
    @Test
    public void shouldCloneCardsOnCardCreationWhenCollaborating() throws InterruptedException {
        // given
        Collaboration collaboration = externallyCreateCollaboration(2);
        Participant p1 = collaboration.getParticipants().get(0);
        Participant p2 = collaboration.getParticipants().get(1);

        // when
        IntStream.range(0, 2).forEach(x -> cardService
                .externallyCreateEmptyDefaultCard(p1.getDeck().getDeckId()));
        Thread.sleep(1000);

        // then
        List<Card> p2Cards = cardSynchronizationService
                .synchronizeCardsByDeck(p2.getDeck().getDeckId());
        assertThat(p2Cards)
                .hasSize(2);
    }

    /*
        Given an active collaboration of two users each having a copy of the same card,
        when user-1 updates his card,
        then user-2 should also receive that update for his card which can be verified by
                having two cards, one active and one disabled where the active card is the updated
                version.
     */
    @Test
    public void shouldCloneOverrideOnCardOverrideWhenCollaborating() throws InterruptedException {
        // given
        Collaboration collaboration = externallyCreateCollaboration(2);
        Participant p1 = collaboration.getParticipants().get(0);
        Participant p2 = collaboration.getParticipants().get(1);
        Card p1Card = cardService.externallyCreateEmptyDefaultCard(p1.getDeck().getDeckId());

        // when
//        Thread.sleep(500); // prevent race condition
        cardService.externallyOverrideCardAsEmptyDefaultCard(p1Card.getCardId());
        Thread.sleep(2000);

        // then
        List<Card> p2Cards = cardSynchronizationService
                .synchronizeCardsByDeck(p2.getDeck().getDeckId());
        List<Card> p2ActiveCards = p2Cards
                .stream()
                .filter(Card::getIsActive)
                .toList();
        List<Card> p2InactiveCards = p2Cards
                .stream()
                .filter(x -> !x.getIsActive())
                .toList();
        assertThat(p2Cards)
                .hasSize(2);
        assertThat(p2ActiveCards)
                .hasSize(1);
        assertThat(p2InactiveCards)
                .hasSize(1);
    }

    /*
        Given an active collaboration of two users,
        when user-2 LEAVES the collaboration and user-1 creates a new card,
        then user-2 should NOT receive that card.
     */
    @Test
    public void shouldNotReceiveClonedCardAfterCollaborationHasBeenLeft() throws InterruptedException {
        // given
        Collaboration collaboration = externallyCreateCollaboration(2);
        Participant p1 = collaboration.getParticipants().get(0);
        Participant p2 = collaboration.getParticipants().get(1);

        // when
        collaborationService.externallyEndCollaboration(collaboration.getCollaborationId(),
                p2.getUserId());
        cardService.externallyCreateEmptyDefaultCard(p1.getDeck().getDeckId());
        Thread.sleep(1000);

        // then
        List<Card> p1Cards = cardSynchronizationService
                .synchronizeCardsByDeck(p1.getDeck().getDeckId());
        assertThat(p1Cards)
                .hasSize(1);
        List<Card> p2Cards = cardSynchronizationService
                .synchronizeCardsByDeck(p2.getDeck().getDeckId());
        assertThat(p2Cards)
                .hasSize(0);
    }

    /*
        Given an active collaboration of two users,
        when user-2 disables his deck,
        then his participation in the collaboration should end.
     */
    @Test
    public void shouldEndParticipationWhenDeckIsDisabled() throws InterruptedException {
        // given
        Collaboration collaboration = externallyCreateCollaboration(2);
        Participant p1 = collaboration.getParticipants().get(0);
        Participant p2 = collaboration.getParticipants().get(1);
        Thread.sleep(250);

        // when
        deckService.externallyDisableDeck(p2.getDeck().getDeckId());
        Thread.sleep(1000);

        // then
        Collaboration updatedCollaboration = collaborationSynchronizationService
                .synchronizeCollaboration(collaboration.getCollaborationId());
        Participant p2Updated = updatedCollaboration.findParticipant(p2.getUserId()).orElseThrow();
        assertThat(p2Updated.getParticipantStatus())
                .isEqualTo(ParticipantStatus.TERMINATED);
    }
    

    private Collaboration externallyCreateCollaboration(Integer size) throws InterruptedException {
        List<UUID> usersById = IntStream.range(0, size)
                .mapToObj(i -> userService.externallyCreateUser(Username.makeRandomUsername(),
                        MailAddress.makeRandomMailAddress()))
                .map(User::getUserId)
                .toList();
        Collaboration collaboration = collaborationService.externallyCreateCollaboration(usersById);
        Thread.sleep(500); // TODO resolve race condition on server-side
        usersById.forEach(userId -> {
            collaborationService.externallyAcceptCollaboration(collaboration.getCollaborationId(),
                    userId);
        });
        /*
            Prevent user-sided race-condition.
         */
        Collaboration syncedCollaboration;
        do {
            Thread.sleep(500);
            syncedCollaboration = collaborationSynchronizationService
                    .synchronizeCollaboration(collaboration.getCollaborationId());
        } while (syncedCollaboration.getParticipants()
                .stream()
                .anyMatch(x -> x.getDeck() == null));
        return syncedCollaboration;
    }
}
