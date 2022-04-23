package de.danielkoellgen.srscsprodtestservice.domain.participant;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Entity
@Table(name = "participants")
@NoArgsConstructor
public class Participant {

    @Id
    @Getter
    @Column(name = "participant_id")
    @Type(type = "uuid-char")
    private @NotNull UUID participantId;

    @Getter
    @OneToOne
    @JoinColumn(name = "user_id")
    private @NotNull User user;

    @Getter
    @Column(name = "participant_status")
    private @NotNull ParticipantStatus participantStatus;

    @Getter
    @ManyToOne
    @JoinColumn(name = "deck_id")
    private @Nullable Deck deck;


    public Participant(@NotNull User user, @NotNull ParticipantStatus participantStatus) {
        this.participantId = user.getUserId();
        this.user = user;
        this.participantStatus = participantStatus;
    }
}
