package de.danielkoellgen.srscsprodtestservice.domain.participant.domain;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
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
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private @NotNull User user;

    @Getter
    @Column(name = "participant_status")
    private @NotNull ParticipantStatus participantStatus;

    @Getter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "deck_id")
    private @Nullable Deck deck;


    public Participant(@NotNull User user, @NotNull ParticipantStatus participantStatus) {
        this.participantId = UUID.randomUUID();
        this.user = user;
        this.participantStatus = participantStatus;
    }

    public void acceptCollaborationInvitation() {
        participantStatus = ParticipantStatus.INVITATION_ACCEPTED;
    }

    public void endCollaboration() {
        if (participantStatus.equals(ParticipantStatus.INVITED)) {
            participantStatus = ParticipantStatus.INVITATION_DECLINED;
            return;
        }
        if (participantStatus.equals(ParticipantStatus.INVITATION_ACCEPTED)) {
            participantStatus = ParticipantStatus.TERMINATED;
        }
    }
}
