package de.danielkoellgen.srscsprodtestservice.domain.collaboration.application;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.CollabClient;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.dto.CollaborationResponseDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class CollaborationSynchronizationService {

    private final CollabClient collabClient;

    private final DeckSynchronizationService deckSynchronizationService;

    private final DeckRepository deckRepository;
    private final CollaborationRepository collaborationRepository;

    private static final Logger logger = LoggerFactory
            .getLogger(CollaborationSynchronizationService.class);

    @Autowired
    public CollaborationSynchronizationService(CollabClient collabClient,
            DeckRepository deckRepository, DeckSynchronizationService deckSynchronizationService,
            CollaborationRepository collaborationRepository) {
        this.collabClient = collabClient;
        this.deckSynchronizationService = deckSynchronizationService;
        this.deckRepository = deckRepository;
        this.collaborationRepository = collaborationRepository;
    }

    public Collaboration synchronizeCollaboration(UUID collaborationId) {
        logger.trace("Synchronizing local- w/ remote collaboration '{}'...", collaborationId);
        Optional<Collaboration> optLocalCollaboration = collaborationRepository
                .findById(collaborationId);
        Optional<CollaborationResponseDto> optRemoteCollaboration = collabClient
                .fetchCollaboration(collaborationId);

        if (optLocalCollaboration.isEmpty()) {
            logger.warn("Local collaboration does not exist.");
        }
        if (optRemoteCollaboration.isEmpty()) {
            logger.error("Synchronization failed. Remote collaboration does not exist. " +
                    "[collaborationId={}]", collaborationId);
            throw new RuntimeException("Synchronization failed. Remote collaboration does not exist.");
        }

        Collaboration syncedCollaboration = mergeRemoteAndLocalCollaboration(
                optRemoteCollaboration.get(), optLocalCollaboration);
        collaborationRepository.save(syncedCollaboration);
        return syncedCollaboration;
    }

    private Collaboration mergeRemoteAndLocalCollaboration(
            @NotNull CollaborationResponseDto remoteCollaboration,
            @NotNull Optional<Collaboration> optLocalCollaboration) {
        Function<UUID, Deck> fetchSyncedDeck = deckSynchronizationService::synchronizeDeck;

        if (optLocalCollaboration.isEmpty()) {
            List<Participant> participants = remoteCollaboration.participants()
                    .stream()
                    .map(dto -> Participant.makeFromDto(dto, fetchSyncedDeck))
                    .toList();
            logger.info("Local collaboration created from remote.");
            return new Collaboration(remoteCollaboration.collaborationId(), participants);
        }
        Collaboration collaboration = optLocalCollaboration.get();
        collaboration.update(remoteCollaboration, fetchSyncedDeck);
        logger.info("Local collaboration updated with remote.");
        return collaboration;
    }
}
