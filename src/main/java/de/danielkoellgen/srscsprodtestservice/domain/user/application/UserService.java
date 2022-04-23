package de.danielkoellgen.srscsprodtestservice.domain.user.application;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Name;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.userservice.UserClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserClient userClient;

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserClient userClient, UserRepository userRepository) {
        this.userClient = userClient;
        this.userRepository = userRepository;
    }

    public @NotNull User externallyCreateUser(@NotNull Username username, @NotNull MailAddress mailAddress) {
        Name name = Name.newName("anyName");
        Optional<User> optUser = userClient.createUser(username, mailAddress, name, name);
        if (optUser.isEmpty()) {
            throw new RuntimeException("Failed to externally create User.");
        }
        User newUser = optUser.get();
        userRepository.save(newUser);
        return newUser;
    }
}
