package de.danielkoellgen.srscsprodtestservice.domain.deck;

import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DeckServiceIntegrationTest {

    private final UserService userService;
    private final DeckService deckService;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    @Autowired
    public DeckServiceIntegrationTest(UserService userService, DeckService deckService,
            UserRepository userRepository, DeckRepository deckRepository) {
        this.userService = userService;
        this.deckService = deckService;
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

    @Test
    public void shouldAllowToExternallyCreateDecks() throws InterruptedException {
        // given
        User user = userService.externallyCreateUser(
                Username.makeRandomUsername(), MailAddress.makeRandomMailAddress());

        // when
        Thread.sleep(1000);     // waiting for 'UserCreated'-Event to arrive
        Deck deck = deckService.externallyCreateDeck(user.getUserId());

        // then
        assertThat(deck.getUser())
                .usingRecursiveComparison()
                .isEqualTo(user);

        // and then
        Deck localDeck = deckRepository.findById(deck.getDeckId()).orElseThrow();
        assertThat(localDeck)
                .usingRecursiveComparison()
                .isEqualTo(deck);
    }

    @Test
    public void shouldAllowToExternallyCreateDecks_WithOccurringRaceCondition() {
        // given
        User newUser = userService.externallyCreateUser(
                Username.makeRandomUsername(), MailAddress.makeRandomMailAddress());

        // when
        Deck deck = deckService.externallyCreateDeck(newUser.getUserId());
    }
}
