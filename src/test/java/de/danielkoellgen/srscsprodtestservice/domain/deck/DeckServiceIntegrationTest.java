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

    private User user1;

    @Autowired
    public DeckServiceIntegrationTest(UserService userService, DeckService deckService, UserRepository userRepository,
            DeckRepository deckRepository) {
        this.userService = userService;
        this.deckService = deckService;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    @BeforeEach
    public void setUp() {
        user1 = userService.externallyCreateUser(Username.makeRandomUsername(), MailAddress.makeRandomMailAddress());
    }

    @AfterEach
    public void cleanUp() {
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldAllowToExternallyCreateDecks() throws InterruptedException {
        Thread.sleep(100);

        // when
        Deck deck = deckService.externallyCreateDeck(user1);

        // then
        assertThat(deckRepository.existsById(deck.getDeckId()))
                .isTrue();
    }
}
