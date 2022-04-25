package de.danielkoellgen.srscsprodtestservice.domain.collaboration;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class CollaborationServiceIntegrationTest {

    private final UserService userService;
    private final CollaborationService collaborationService;

    @Autowired
    public CollaborationServiceIntegrationTest(UserService userService, CollaborationService collaborationService) {
        this.userService = userService;
        this.collaborationService = collaborationService;
    }

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void cleanUp() {

    }

    @Test
    public void shouldAllowToExternallyCreateCollaborations() {
        // given
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            users.add(userService.externallyCreateUser(
                    Username.makeRandomUsername(), MailAddress.makeRandomMailAddress())
            );
        }

        // when
        collaborationService.externallyStartCollaboration(users.stream()
                .map(User::getUserId)
                .toList()
        );
        collaborationService.externallyStartCollaboration(users.stream()
                .map(User::getUserId)
                .toList()
        );
    }

    @Test
    public void shouldAllowToExternallyAcceptParticipations() throws InterruptedException {
        // given
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            users.add(userService.externallyCreateUser(
                    Username.makeRandomUsername(), MailAddress.makeRandomMailAddress())
            );
        }
        Thread.sleep(50);
        Collaboration collaboration = collaborationService.externallyStartCollaboration(users.stream()
                .map(User::getUserId)
                .toList()
        );
        Thread.sleep(50);

        // when
        collaborationService.externallyAcceptCollaboration(
                collaboration.getCollaborationId(), collaboration.getParticipants().get(0).getParticipantId()
        );
    }
}
