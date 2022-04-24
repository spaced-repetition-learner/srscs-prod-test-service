package de.danielkoellgen.srscsprodtestservice.domain.card.repository;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.Card;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CardRepository extends CrudRepository<Card, UUID> {

    @NotNull Iterable<Card> findByIsActive(@NotNull Boolean isActive);
}
