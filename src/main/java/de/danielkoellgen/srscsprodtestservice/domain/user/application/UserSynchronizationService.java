package de.danielkoellgen.srscsprodtestservice.domain.user.application;

import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.domain.user.repository.UserRepository;
import de.danielkoellgen.srscsprodtestservice.web.userservice.UserClient;
import de.danielkoellgen.srscsprodtestservice.web.userservice.dto.UserResponseDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSynchronizationService {

    private final UserClient userClient;

    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserSynchronizationService.class);

    @Autowired
    public UserSynchronizationService(UserClient userClient, UserRepository userRepository) {
        this.userClient = userClient;
        this.userRepository = userRepository;
    }

    public User synchronizeUser(@NotNull UUID userId) {
        logger.trace("Synchronizing local- w/ remote user '{}'...", userId);
        Optional<User> optLocalUser = userRepository.findById(userId);
        Optional<UserResponseDto> optRemoteUser = userClient.fetchUser(userId);

        if (optLocalUser.isEmpty()) {
            logger.warn("Local user does not exist.");
        }
        if (optRemoteUser.isEmpty()) {
            logger.error("Synchronization failed. Remote user does not exist. Local-user: {}",
                    optLocalUser);
            throw new RuntimeException("Expected remote user does not exist. [userId="+userId+"]");
        }
        User syncedUser = mergeRemoteAndLocalUser(optRemoteUser.get(), optLocalUser);
        userRepository.save(syncedUser);
        return syncedUser;
    }

    private User mergeRemoteAndLocalUser(@NotNull UserResponseDto remoteUser,
            @NotNull Optional<User> optLocalUser) {
        if (optLocalUser.isEmpty()) {
            logger.info("Local user created from remote.");
            return User.makeFromDto(remoteUser);
        }
        User localUser = optLocalUser.get();
        localUser.update(remoteUser);
        logger.info("Local user updated from remote.");
        return localUser;
    }
}
