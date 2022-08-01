package de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.dto.CollaborationResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private @NotNull List<Participant> participants;


    public Collaboration(@NotNull UUID collaborationId, @NotNull List<Participant> participants) {
        this.collaborationId = collaborationId;
        this.participants = participants;
    }

    public Optional<Participant> findParticipant(UUID userId) {
        return participants.stream()
                .filter(x -> x.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public String toString() {
        return "Collaboration{" +
                "collaborationId=" + collaborationId +
                ", participants=" + participants +
                '}';
    }
}
