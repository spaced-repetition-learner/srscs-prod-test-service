package de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.dto.CollaborationResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Entity
@Table(name = "collaborations")
@NoArgsConstructor
public class Collaboration {

    @Id
    @Getter
    @Column(name = "collaboration_id")
    @Type(type = "uuid-char")
    private @NotNull UUID collaborationId;

    @Getter
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private @NotNull List<Participant> participants;

    @Transient
    private static final Logger logger = LoggerFactory.getLogger(Collaboration.class);


    public Collaboration(@NotNull UUID collaborationId, @NotNull List<Participant> participants) {
        this.collaborationId = collaborationId;
        this.participants = participants;
    }

    public Optional<Participant> findParticipant(UUID userId) {
        return participants
                .stream()
                .filter(x -> x.getUserId().equals(userId))
                .findFirst();
    }

    public void update(@NotNull CollaborationResponseDto dto, Function<UUID, Deck> fetchDeck) {
        dto.participants().forEach(x -> {
            Optional<Participant> participant = participants
                    .stream()
                    .filter(y -> y.getUserId().equals(x.userId()))
                    .findFirst();
            if (participant.isPresent()) {
                participant.get().update(x, fetchDeck);
            } else {
                Participant newParticipant = Participant.makeFromDto(x, fetchDeck);
                logger.warn("Local- and remote participants out of sync. [local={}, remote={}]",
                        null, newParticipant);
                participants.add(newParticipant);
            }
        });
    }

    @Override
    public String toString() {
        return "Collaboration{" +
                "collaborationId=" + collaborationId +
                ", participants=" + participants +
                '}';
    }
}
