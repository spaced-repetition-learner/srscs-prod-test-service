package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.repository.CardRepository;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository.CollaborationRepository;
import de.danielkoellgen.srscsprodtestservice.domain.deck.repository.DeckRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;

public interface Repositories {

    UserRepository getUserRepository();

    DeckRepository getDeckRepository();

    CardRepository getCardRepository();

    CollaborationRepository getCollaborationRepository();
}
