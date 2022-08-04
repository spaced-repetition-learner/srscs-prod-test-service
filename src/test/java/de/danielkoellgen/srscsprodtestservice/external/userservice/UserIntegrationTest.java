package de.danielkoellgen.srscsprodtestservice.external.userservice;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Name;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.userservice.UserClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserIntegrationTest {

    private final UserClient userClient;

    private final UserRepository userRepository;

    @Autowired
    public UserIntegrationTest(UserClient userClient, UserRepository userRepository) {
        this.userClient = userClient;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() {}

    @AfterEach
    public void cleanUp() {
        userRepository.deleteAll();
    }

    /*
        Given a username, mail-address and both first- and lastname,
        when a new user with all of those is created,
        then the response should verify that.
     */
    @Test
    public void shouldAllowToExternallyCreateUsers() {
        // given
        Username username = Username.makeRandomUsername();
        MailAddress mailAddress = MailAddress.makeRandomMailAddress();
        Name firstName = Name.newName("anyName");
        Name lastName = Name.newName("anyName");

        // when
        User user = userClient.createUser(username, mailAddress, firstName, lastName).orElseThrow();

        // then
        assertThat(user.getUsername())
                .isEqualTo(username);
        assertThat(user.getIsActive())
                .isTrue();
    }
}
