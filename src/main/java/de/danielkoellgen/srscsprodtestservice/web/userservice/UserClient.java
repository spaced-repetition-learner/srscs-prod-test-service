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

import java.util.Optional;

@Component
@Scope("singleton")
public class UserClient {

    private final WebClient userClient;

    private final String userServiceAddress;

    private final Logger logger = LoggerFactory.getLogger(UserClient.class);

    @Autowired
    public UserClient(@Value("${app.userService.address}") String userServiceAddress) {
        this.userClient = WebClient.create();
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
            logger.debug("Request successful.");
            logger.trace("{}", responseDto);
            return Optional.of(
                    new User(responseDto.userId(), username, responseDto.isActive())
            );

        } catch (WebClientResponseException e) {
            logger.error("Request failed. {}", e.getMessage());
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed. {}", e.getMessage());
            return Optional.empty();
        }
    }
}
