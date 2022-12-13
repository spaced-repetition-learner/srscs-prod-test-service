package de.danielkoellgen.srscsprodtestservice.external.collabservice;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.participant.repository.ParticipantRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.CollabClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CollabIntegrationTest {

    private final UserService userService;
    private final DeckService deckService;

    private final CollabClient collabClient;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CollaborationRepository collaborationRepository;
    private final ParticipantRepository participantRepository;

    @Autowired
    public CollabIntegrationTest(UserService userService, DeckService deckService,
            CollabClient collabClient, UserRepository userRepository, DeckRepository deckRepository,
            CollaborationRepository collaborationRepository,
            ParticipantRepository participantRepository) {
        this.userService = userService;
        this.deckService = deckService;
        this.collabClient = collabClient;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.collaborationRepository = collaborationRepository;
        this.participantRepository = participantRepository;
    }

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void cleanUp() {
        collaborationRepository.deleteAll();
        participantRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    /*
        Given four users,
        when a new collaboration is started with those four users,
        then the response should contain those four users as invited participants.
     */
    @Test
    public void shouldAllowToInviteUsersToCollaborate() {
        // given
        List<User> users = IntStream.range(0, 4)
                .mapToObj(i -> userService.externallyCreateUser(Username.makeRandomUsername(),
                        MailAddress.makeRandomMailAddress()))
                .toList();
        DeckName deckName = DeckName.makeRandomDeckName();

        // when
        Collaboration collaboration = collabClient.createNewCollaboration(users, deckName)
                .orElseThrow();

        // then
        assertThat(collaboration.getParticipants())
                .hasSize(4);
        assertThat(collaboration.getParticipants()
                        .stream()
                        .map(Participant::getUserId)
                        .toList())
                .containsAll(users
                        .stream()
                        .map(User::getUserId)
                        .toList());
    }

    /*
        Given a collaboration with four pending users,
        when each user accepts,
        then each response should be true.
     */
    @Test
    public void shouldAllowToAcceptCollaborations() {
        // given
        List<User> users = IntStream.range(0, 4)
                .mapToObj(i -> userService.externallyCreateUser(Username.makeRandomUsername(),
                        MailAddress.makeRandomMailAddress()))
                .toList();
        DeckName deckName = DeckName.makeRandomDeckName();
        Collaboration collaboration = collabClient.createNewCollaboration(users, deckName)
                .orElseThrow();

        // when
        List<Boolean> response = users
                .stream()
                .map(user -> collabClient.acceptCollaboration(collaboration,
                        collaboration.findParticipant(user.getUserId()).orElseThrow()))
                .toList();

        // then
        assertThat(response)
                .doesNotContain(false);
    }
}
