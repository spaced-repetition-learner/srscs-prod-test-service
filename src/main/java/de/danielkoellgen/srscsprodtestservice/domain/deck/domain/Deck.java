package de.danielkoellgen.srscsprodtestservice.domain.deck.domain;

import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.dto.DeckResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "decks")
@Getter
@NoArgsConstructor
public class Deck {

    @Id
    @Column(name = "deck_id")
    @Type(type = "uuid-char")
    private @NotNull UUID deckId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private @NotNull User user;

    @Column(name = "is_active")
    private @NotNull Boolean isActive;


    public Deck(@NotNull UUID deckId, @NotNull User user, @NotNull Boolean isActive) {
        this.deckId = deckId;
        this.user = user;
        this.isActive = isActive;
    }

    public static @NotNull Deck makeFromDto(@NotNull DeckResponseDto dto, @NotNull User user) {
        return new Deck(dto.deckId(), user, dto.isActive());
    }

    public void disableDeck() {
        isActive = false;
    }

    public void update(@NotNull DeckResponseDto dto) {
        if (!isActive.equals(dto.isActive())) {
            isActive = dto.isActive();
        }
    }

    @Override
    public String toString() {
        return "Deck{" +
                "deckId=" + deckId +
                ", user=" + user +
                ", isActive=" + isActive +
                '}';
    }
}
