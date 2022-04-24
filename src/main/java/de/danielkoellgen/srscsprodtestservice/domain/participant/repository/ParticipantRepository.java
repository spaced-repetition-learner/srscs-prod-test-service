package de.danielkoellgen.srscsprodtestservice.domain.participant.repository;

import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.ParticipantStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ParticipantRepository extends CrudRepository<Participant, UUID> {

    @NotNull Iterable<Participant> findAllByParticipantStatus(@NotNull ParticipantStatus participantStatus);
}
