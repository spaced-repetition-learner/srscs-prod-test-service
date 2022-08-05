package de.danielkoellgen.srscsprodtestservice.external.deckservice;

import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.DeckClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DeckIntegrationTest {

    private final UserService userService;
    private final DeckService deckService;

    private final DeckClient deckClient;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    @Autowired
    public DeckIntegrationTest(UserService userService, DeckService deckService, DeckClient deckClient,
            UserRepository userRepository, DeckRepository deckRepository) {
        this.userService = userService;
        this.deckService = deckService;
        this.deckClient = deckClient;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void cleanUp() {
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    /*
        Given a user,
        when a new deck for this user is created,
        then the response should verify the creation of the particular deck.
     */
    @Test
    public void shouldAllowToExternallyCreateDecks() throws Exception {
        // given
        User user = userService.externallyCreateUser(
                Username.makeRandomUsername(), MailAddress.makeRandomMailAddress());
        DeckName deckName = new DeckName("anyName");

        // when
        Deck deck = deckClient.createDeck(user, deckName).orElseThrow();

        // then
        assertThat(deck.getIsActive())
                .isTrue();
        assertThat(deck.getUser())
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    /*
        Given an active deck,
        when it's disabled,
        then the response should verify that.
     */
    @Test
    public void shouldAllowToExternallyDisableDecks() {
        // given
        User user = userService.externallyCreateUser(Username.makeRandomUsername(),
                MailAddress.makeRandomMailAddress());
        Deck deck = deckService.externallyCreateDeck(user.getUserId());

        // when
        Boolean response = deckClient.disableDeck(deck.getDeckId());

        // then
        assertThat(response)
                .isTrue();
    }
}
