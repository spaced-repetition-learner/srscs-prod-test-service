package de.danielkoellgen.srscsprodtestservice.web.collabservice;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.Participant;
import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.dto.CollaborationRequestDto;
import de.danielkoellgen.srscsprodtestservice.web.collabservice.dto.CollaborationResponseDto;
import de.danielkoellgen.srscsprodtestservice.web.deckservice.DeckClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope("singleton")
public class CollabClient {

    private final WebClient collabClient;

    private final String collabServiceAddress;

    private final Integer retryAttempts;
    private final Integer retryDelay;
    private final Double retryJitter;

    private final Logger logger = LoggerFactory.getLogger(CollabClient.class);

    @Autowired
    public CollabClient(@Value("${app.collabService.address}") String collabServiceAddress,
            @Value("${web.retry.attempts}") Integer retries,
            @Value("${web.retry.delay}") Integer delay,
            @Value("${web.retry.jitter}") Double jitter,
            WebClient webClient) {
        this.collabClient = webClient;
        this.retryAttempts = retries;
        this.retryDelay = delay;
        this.retryJitter = jitter;
        this.collabServiceAddress = collabServiceAddress;
    }

    public @NotNull Optional<Collaboration> createNewCollaboration(@NotNull List<User> users,
            @NotNull DeckName collaborationName) {
        CollaborationRequestDto requestDto = new CollaborationRequestDto(
                users.stream()
                        .map(x -> x.getUsername().getUsername())
                        .toList(),
                collaborationName.getName());
        String uri = collabServiceAddress + "/collaborations";

        logger.trace("Calling POST {} to start a new Collaboration with {} Users...", uri, users.size());
        logger.debug("{}", requestDto);

        try {
            CollaborationResponseDto responseDto = collabClient.post()
                    .uri(collabServiceAddress + "/collaborations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CollaborationResponseDto.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. Collaboration created.");
            logger.debug("{}", responseDto);

            List<Participant> participants = responseDto.participants().stream()
                    .map(x -> new Participant(
                            users.stream()
                                    .filter(y -> y.getUserId().equals(x.userId()))
                                    .findFirst().orElseThrow(),
                            x.getMappedParticipantStatus()))
                    .toList();
            return Optional.of(new Collaboration(responseDto.collaborationId(), participants));

        } catch (WebClientResponseException e) {
            logger.warn("Request failed externally. {}: {}.", e.getStatusCode(), e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Boolean acceptCollaboration(@NotNull Collaboration collaboration,
            @NotNull Participant participant) {
        UUID collaborationId = collaboration.getCollaborationId();
        UUID participantId = participant.getUserId();
        String uri = collabServiceAddress+"/collaborations/"+collaborationId+"/participants/"+participantId+"/state";

        logger.trace("Calling POST {} to accept the Collaboration.", uri);

        try {
            collabClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(ResponseEntity.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .block();

            logger.trace("Request successful. Collaboration accepted.");
            return true;

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);
            return false;

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return false;
        }
    }

    public Boolean endCollaboration(@NotNull Collaboration collaboration,
            @NotNull Participant participant) {
        UUID collaborationId = collaboration.getCollaborationId();
        UUID participantId = participant.getUserId();
        String uri = collabServiceAddress + "/collaborations/"+collaborationId+"/participants/"+participantId;

        logger.trace("Calling DELETE {} to end the Collaboration.", uri);

        try {
            collabClient.delete()
                    .uri(uri)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode() == HttpStatus.OK) {
                            return clientResponse.bodyToMono(HttpStatus.class);
                        } else {
                            return clientResponse.createException().flatMap(Mono::error);
                        }
                    })
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .block();

            logger.trace("Request successful. Collaboration ended.");
            return true;

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);
            return false;

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return false;
        }
    }

    public @NotNull Optional<CollaborationResponseDto> fetchCollaboration(@NotNull UUID collaborationId) {
        String uri = collabServiceAddress + "/collaborations/" + collaborationId;

        logger.trace("Calling GET {}", uri);

        try {
            CollaborationResponseDto responseDto = collabClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(CollaborationResponseDto.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryDelay))
                            .jitter(retryJitter))
                    .block();
            assert responseDto != null;

            logger.trace("Request successful. Collaboration {} fetched.", collaborationId);
            logger.debug("{}", responseDto);

            return Optional.of(responseDto);

        } catch (WebClientResponseException e) {
            logger.error("Request failed externally. {}: {}.", e.getRawStatusCode(),
                    e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            logger.error("Request failed locally. {}.", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
