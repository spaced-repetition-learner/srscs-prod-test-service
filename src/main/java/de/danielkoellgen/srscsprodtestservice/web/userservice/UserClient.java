package de.danielkoellgen.srscsprodtestservice.web.userservice;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.MailAddress;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Name;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.userservice.dto.UserRequestDto;
import de.danielkoellgen.srscsprodtestservice.web.userservice.dto.UserResponseDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope("singleton")
public class UserClient {

    private final WebClient userClient;

    private final String userServiceAddress;

    private final Logger logger = LoggerFactory.getLogger(UserClient.class);

    @Autowired
    public UserClient(@Value("${app.userService.address}") String userServiceAddress, WebClient webClient) {
        this.userClient = webClient;
        this.userServiceAddress = userServiceAddress;
    }

    public @NotNull Optional<User> createUser(Username username, MailAddress mail, Name firstName, Name lastName) {
        UserRequestDto requestDto = new UserRequestDto(
                username.getUsername(), mail.getMailAddress(), firstName.getName(), lastName.getName());
        logger.debug("Requesting User-Service to create a new User. Address is POST {}",
                userServiceAddress+"/users");
        logger.trace("{}", requestDto);

        try {
            UserResponseDto responseDto = userClient.post().uri(userServiceAddress + "/users")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(UserResponseDto.class)
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. User created.");
            logger.debug("{}", responseDto);

            return Optional.of(
                    new User(responseDto.userId(), username, responseDto.isActive())
            );

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(), e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public @NotNull Optional<UserResponseDto> fetchUser(@NotNull UUID userId) {
        String uri = userServiceAddress + "/users?user-id=" + userId;

        logger.trace("Calling GET {} to fetch a User...", uri);

        try {
            UserResponseDto responseDto = userClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(UserResponseDto.class)
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. User {} fetched.", userId);
            logger.debug("{}", responseDto);

            return Optional.of(responseDto);

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(), e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
