package de.danielkoellgen.srscsprodtestservice.domain.deck.domain;

import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "decks")
@NoArgsConstructor
public class Deck {

    @Id
    @Getter
    @Column(name = "deck_id")
    @Type(type = "uuid-char")
    private @NotNull UUID deckId;

    @Getter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private @NotNull User user;

    @Getter
    @Column(name = "is_active")
    private @NotNull Boolean isActive;


    public Deck(@NotNull UUID deckId, @NotNull User user, @NotNull Boolean isActive) {
        this.deckId = deckId;
        this.user = user;
        this.isActive = isActive;
    }
}
