package de.danielkoellgen.srscsprodtestservice.domain.user;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserService;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserServiceIntegrationTest {

    private final UserService userService;

    private final UserRepository userRepository;

    @Autowired
    public UserServiceIntegrationTest(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() {}

    @AfterEach
    public void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    public void shouldAllowToExternallyCreateUsers() {
        // given
        Username username = Username.makeRandomUsername();
        MailAddress mailAddress = MailAddress.makeRandomMailAddress();

        // when
        User user = userService.externallyCreateUser(username, mailAddress);

        // then
        User fetchedUser = userRepository.findById(user.getUserId()).orElseThrow();
        assertThat(fetchedUser.getUsername())
                .isEqualTo(username);
    }
}
