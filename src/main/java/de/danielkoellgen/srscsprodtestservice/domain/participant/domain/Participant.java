package de.danielkoellgen.srscsprodtestservice.domain.participant.domain;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.dto.ParticipantResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "participants")
@Getter
@NoArgsConstructor
public class Participant {

    @Id
    @Column(name = "participant_id")
    @Type(type = "uuid-char")
    private @NotNull UUID participantId;

    @Column(name = "user_id")
    @Type(type = "uuid-char")
    private @NotNull UUID userId;

    @Column(name = "participant_status")
    private @NotNull ParticipantStatus participantStatus;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "deck_id")
    private @Nullable Deck deck;


    public Participant(@NotNull User user, @NotNull ParticipantStatus participantStatus) {
        this.participantId = UUID.randomUUID();
        this.userId = user.getUserId();
        this.participantStatus = participantStatus;
    }

    public Participant(@NotNull UUID userId, @NotNull ParticipantStatus participantStatus, @Nullable Deck deck) {
        this.participantId = UUID.randomUUID();
        this.userId = userId;
        this.participantStatus = participantStatus;
        this.deck = deck;
    }

    public static @NotNull Participant makeFromDto(@NotNull ParticipantResponseDto dto, BiFunction<UUID, UUID, Deck> fetchDeck) {
        @Nullable Deck deck = dto.deck() != null ? fetchDeck.apply(dto.deck().deckId(), dto.userId()) : null;
        return new Participant(dto.userId(), dto.getMappedParticipantStatus(), deck);
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

    @Override
    public String toString() {
        return "Participant{" +
                "participantId=" + participantId +
                ", userId=" + userId +
                ", participantStatus=" + participantStatus +
                ", deck=" + deck +
                '}';
    }
}
