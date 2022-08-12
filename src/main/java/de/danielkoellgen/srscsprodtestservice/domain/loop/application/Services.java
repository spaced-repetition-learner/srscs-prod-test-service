package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserSynchronizationService;

public interface Services {

    UserService getUserService();

    DeckService getDeckService();

    CardService getCardService();

    CollaborationService getCollaborationService();

    UserSynchronizationService getUserSynchronizationService();

    DeckSynchronizationService getDeckSynchronizationService();

    CardSynchronizationService getCardSynchronizationService();

    CollaborationSynchronizationService getCollaborationSynchronizationService();
}
