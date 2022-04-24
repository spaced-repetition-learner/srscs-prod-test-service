package de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CollaborationRepository extends CrudRepository<Collaboration, UUID> {

    @Nullable Collaboration findByParticipants_ParticipantId(@NotNull UUID participantId);

}
