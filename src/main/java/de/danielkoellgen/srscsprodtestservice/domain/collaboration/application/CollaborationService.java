package de.danielkoellgen.srscsprodtestservice.domain.collaboration.application;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.participant.repository.ParticipantRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.CollabClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CollaborationService {

    private final CollabClient collabClient;

    private final UserRepository userRepository;
    private final CollaborationRepository collaborationRepository;
    private final ParticipantRepository participantRepository;

    @Autowired
    public CollaborationService(CollabClient collabClient, UserRepository userRepository,
            CollaborationRepository collaborationRepository, ParticipantRepository participantRepository) {
        this.collabClient = collabClient;
        this.userRepository = userRepository;
        this.collaborationRepository = collaborationRepository;
        this.participantRepository = participantRepository;
    }

    public @NotNull Collaboration externallyStartCollaboration(@NotNull List<UUID> usersIds) {
        Iterable<User> usersIterable = userRepository.findAllById(usersIds);
        List<User> users = new ArrayList<>();
        usersIterable.iterator().forEachRemaining(users::add);
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

    public void externallyAcceptCollaboration(@NotNull UUID collaborationId, @NotNull UUID participantId) {
        Collaboration collaboration = collaborationRepository.findById(collaborationId).get();
        Participant participant = participantRepository.findById(participantId).get();
        Boolean response =  collabClient.acceptCollaboration(collaboration, participant);
        if (!response) {
            throw new RuntimeException("Failed to externally accept the Collaboration.");
        }
        participant.acceptCollaborationInvitation();
        participantRepository.save(participant);
    }
}
