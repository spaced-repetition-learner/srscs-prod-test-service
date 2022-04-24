package de.danielkoellgen.srscsprodtestservice.domain.card.domain;

import de.danielkoellgen.srscsprodtestservice.domain.deck.domain.Deck;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cards")
@NoArgsConstructor
public class Card {

    @Id
    @Getter
    @Column(name = "card_id")
    @Type(type = "uuid-char")
    private @NotNull UUID cardId;

    @Getter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "deck_id")
    private @NotNull Deck deck;

    @Getter
    @Column(name = "is_active")
    private @NotNull Boolean isActive;

    public Card(@NotNull UUID cardId, @NotNull Deck deck, @NotNull Boolean isActive) {
        this.cardId = cardId;
        this.deck = deck;
        this.isActive = isActive;
    }

    public void disableCard() {
        isActive = false;
    }
}
