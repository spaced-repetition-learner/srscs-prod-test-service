package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class LoopStartUpService {

    private final UserService userService;
    private final DeckService deckService;
    private final CardService cardService;
    private final CollaborationService collaborationService;

    private final Integer userCount;
    private final Integer deckCount;
    private final Integer cardCount;
    private final Integer collabCount;
    private final Integer collabSize;

    private final Integer sleepPerAction;

    @Autowired
    public LoopStartUpService(
            UserService userService,
            DeckService deckService,
            CardService cardService,
            CollaborationService collaborationService,
            @Value("${app.loop.start-up.user-count}") Integer userCount,
            @Value("${app.loop.start-up.deck-count}") Integer deckCount,
            @Value("${app.loop.start-up.card-count}") Integer cardCount,
            @Value("${app.loop.start-up.collab-count}") Integer collabCount,
            @Value("${app.loop.collab.user-count}") Integer collabSize,
            @Value("${app.loop.startup.sleep-per-action-in-ms}") Integer sleepPerAction) {
        this.userService = userService;
        this.deckService = deckService;
        this.cardService = cardService;
        this.collaborationService = collaborationService;
        this.userCount = userCount;
        this.deckCount = deckCount;
        this.cardCount = cardCount;
        this.collabCount = collabCount;
        this.collabSize = collabSize;
        this.sleepPerAction = sleepPerAction;
    }

    public void run() {
        try {
            List<User> users = createUsers();
            List<Deck> decks = createDecks(users);
            List<Card> cards = createCards(decks);
            List<Collaboration> collaborations = startCollaborations(users);
            acceptCollaborations(collaborations);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private List<User> createUsers() throws InterruptedException {
        List<User> users = new ArrayList<>();
        for(int i = 0; i < userCount; i++) {
            users.add(userService.externallyCreateUser(Username.makeRandomUsername(),
                    MailAddress.makeRandomMailAddress()));
            Thread.sleep(sleepPerAction);
        }
        return users;
    }

    private List<Deck> createDecks(List<User> users) throws InterruptedException {
        Random random = new Random();
        List<Deck> decks = new ArrayList<>();
        for(int i = 0; i < deckCount; i++) {
            User user = users.get(random.nextInt(users.size() - 1));
            decks.add(deckService.externallyCreateDeck(user.getUserId()));
            Thread.sleep(sleepPerAction);
        }
        return decks;
    }

    private List<Card> createCards(List<Deck> decks) throws InterruptedException {
        Random random = new Random();
        List<Card> cards = new ArrayList<>();
        for(int i = 0; i < cardCount; i++) {
            Deck deck = decks.get(random.nextInt(decks.size() - 1));
            cards.add(cardService.externallyCreateEmptyDefaultCard(deck.getDeckId()));
            Thread.sleep(sleepPerAction);
        }
        return cards;
    }

    private List<Collaboration> startCollaborations(List<User> users) throws InterruptedException {
        Random random = new Random();
        List<Collaboration> collaborations = new ArrayList<>();
        for(int i = 0; i < collabCount; i++) {
            List<User> invitedUsers = new ArrayList<>();
            while(invitedUsers.size() < collabSize) {
                User selectedUser = users.get(random.nextInt(users.size() - 1));
                if (!invitedUsers.contains(selectedUser)) {
                    invitedUsers.add(selectedUser);
                }
            }
            collaborations.add(collaborationService.externallyCreateCollaboration(
                    invitedUsers.stream()
                            .map(User::getUserId)
                            .toList()));
            Thread.sleep(sleepPerAction);
        }
        return collaborations;
    }

    private void acceptCollaborations(List<Collaboration> collaborations) throws InterruptedException {
        Random random = new Random();
        for (Collaboration collaboration : collaborations) {
            for (Participant participant : collaboration.getParticipants()) {
                collaborationService.externallyAcceptCollaboration(
                        collaboration.getCollaborationId(), participant.getParticipantId());
                Thread.sleep(sleepPerAction);
            }
        }
    }
}











