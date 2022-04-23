package de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain;

import de.danielkoellgen.srscsprodtestservice.domain.participant.Participant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

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
    @OneToMany
    private @NotNull List<Participant> participants;


    public Collaboration(@NotNull UUID collaborationId, @NotNull List<Participant> participants) {
        this.collaborationId = collaborationId;
        this.participants = participants;
    }
}
