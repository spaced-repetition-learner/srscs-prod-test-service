package de.danielkoellgen.srscsprodtestservice.domain.collaboration.application;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.CollabClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollaborationService {

    private final CollabClient collabClient;

    private final CollaborationRepository collaborationRepository;

    @Autowired
    public CollaborationService(CollabClient collabClient, CollaborationRepository collaborationRepository) {
        this.collabClient = collabClient;
        this.collaborationRepository = collaborationRepository;
    }

    public @NotNull Collaboration externallyStartCollaboration(@NotNull List<User> users) {
        Optional<Collaboration> optionalCollaboration = collabClient.createNewCollaboration(
                users, DeckName.makeRandomDeckName()
        );
        if (optionalCollaboration.isEmpty()) {
            throw new RuntimeException("Failed to externally start a new Collaboration.");
        }
        Collaboration collab = optionalCollaboration.get();
        collaborationRepository.save(collab);
        return collab;
    }

    public void externallyAcceptCollaboration(@NotNull Collaboration collaboration, @NotNull Participant participant) {
        Boolean response =  collabClient.acceptCollaboration(collaboration, participant);
        if (!response) {
            throw new RuntimeException("Failed to externally accept the Collaboration.");
        }
        //TODO
    }
}
