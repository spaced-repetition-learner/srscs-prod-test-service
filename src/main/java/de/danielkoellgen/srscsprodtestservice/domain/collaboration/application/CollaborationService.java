package de.danielkoellgen.srscsprodtestservice.domain.collaboration.application;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.participant.repository.ParticipantRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.CollabClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CollaborationService {

    private final CollabClient collabClient;

    private final DeckService deckService;

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CollaborationRepository collaborationRepository;
    private final ParticipantRepository participantRepository;

    private final Logger logger = LoggerFactory.getLogger(CollaborationService.class);


    @Autowired
    public CollaborationService(CollabClient collabClient, DeckService deckService,
            UserRepository userRepository, DeckRepository deckRepository,
            CollaborationRepository collaborationRepository,
            ParticipantRepository participantRepository) {
        this.collabClient = collabClient;
        this.deckService = deckService;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.collaborationRepository = collaborationRepository;
        this.participantRepository = participantRepository;
    }

    public @NotNull Collaboration externallyCreateCollaboration(@NotNull List<UUID> usersIds) {
        logger.trace("Externally creating a Collaboration with {} Participants...", usersIds.size());
        logger.trace("Fetching {} Users by ids {}", usersIds.size(), usersIds);
        Iterable<User> usersIterable = userRepository.findAllById(usersIds);
        List<User> users = new ArrayList<>();
        usersIterable.iterator().forEachRemaining(users::add);
        logger.debug("{}", users);

        Optional<Collaboration> optionalCollaboration = collabClient
                .createNewCollaboration(users, DeckName.makeRandomDeckName());
        if (optionalCollaboration.isEmpty()) {
            throw new RuntimeException("Failed to externally start a new Collaboration.");
        }
        Collaboration collaboration = optionalCollaboration.get();
        collaborationRepository.save(collaboration);
        logger.info("Collaboration with {} Users externally created.", users.size());
        logger.debug("{}", collaboration);
        logger.trace("New Collaboration saved.");

        return collaboration;
    }

    public void externallyAcceptCollaboration(@NotNull UUID collaborationId, @NotNull UUID userId) {
        logger.trace("Externally accepting a Participation in a Collaboration...");
        logger.trace("Fetching Collaboration by id {}...", collaborationId);
        Collaboration collaboration = collaborationRepository.findById(collaborationId).orElseThrow();
        logger.debug("{}", collaboration);

        logger.trace("Querying Participants by user-id {}...", userId);
        Participant participant = collaboration.findParticipant(userId).orElseThrow();
        logger.debug("{}", participant);

        Boolean response =  collabClient.acceptCollaboration(collaboration, participant);
        if (!response) {
            throw new RuntimeException("Failed to externally accept the Collaboration.");
        }
        participant.acceptCollaborationInvitation();
        participantRepository.save(participant);
        logger.info("Collaboration accepted.");
        logger.trace("Updated Participant saved.");
        logger.debug("{}", participant);
    }

    public void externallyEndCollaboration(@NotNull UUID collaborationId, @NotNull UUID userId) {
        logger.trace("Externally ending participation in collaboration...");
        Collaboration collaboration = collaborationRepository
                .findById(collaborationId).orElseThrow();
        Participant participant = collaboration
                .findParticipant(userId).orElseThrow();
        Boolean response = collabClient.endCollaboration(collaboration, participant);
        if (!response) {
            throw new RuntimeException("TODO");
        }
        participant.endParticipation();
        participantRepository.save(participant);
        logger.info("Participation in collaboration ended.");
    }

    public void addDeckToParticipant(@NotNull UUID collaborationId, @NotNull UUID userId,
            @NotNull UUID deckId) {
        logger.trace("Updating Participant with Deck...");
        logger.trace("Fetching Collaboration by id {}...", collaborationId);
        Collaboration collaboration = collaborationRepository.findById(collaborationId).orElseThrow();
        logger.debug("{}", collaboration);

        logger.trace("Querying Participants by user-id {}...", userId);
        Participant participant = collaboration.findParticipant(userId).orElseThrow();
        logger.debug("{}", participant);

        logger.trace("Fetching Deck by id {}...", deckId);
        Optional<Deck> optDeck = deckRepository.findById(deckId);
        Deck deck = optDeck.orElseGet(() -> deckService.addExternallyCreatedDeck(deckId, userId));

        participant.addDeck(deck);
        participantRepository.save(participant);
        logger.info("Deck added to Participant.");
        logger.trace("Updated Participant saved.");
        logger.debug("{}", participant);
    }
}
