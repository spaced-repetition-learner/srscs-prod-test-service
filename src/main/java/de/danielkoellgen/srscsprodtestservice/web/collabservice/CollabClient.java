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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope("singleton")
public class CollabClient {

    private final WebClient collabClient;

    private final String collabServiceAddress;

    private final Logger logger = LoggerFactory.getLogger(CollabClient.class);

    @Autowired
    public CollabClient(@Value("${app.collabService.address}") String collabServiceAddress, WebClient webClient) {
        this.collabClient = webClient;
        this.collabServiceAddress = collabServiceAddress;
    }

    public @NotNull Optional<Collaboration> createNewCollaboration(@NotNull List<User> users,
            @NotNull DeckName collaborationName) {
        CollaborationRequestDto requestDto = new CollaborationRequestDto(
                users.stream()
                        .map(x -> x.getUsername().getUsername())
                        .toList(),
                collaborationName.getName()
        );

        logger.debug("Requesting Collab-Service to start a new Collaboration. Address is POST {}",
                collabServiceAddress+"/collaborations");
        logger.trace("{}", requestDto);

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
                    .block();
            assert responseDto != null;
            logger.trace("{}", responseDto);

            List<Participant> participants = responseDto.participants().stream()
                    .map(x -> new Participant(
                            users.stream()
                                    .filter(y -> y.getUserId().equals(x.userId()))
                                    .findFirst().get(),
                            x.getMappedParticipantStatus()
                    )).toList();
            return Optional.of(new Collaboration(responseDto.collaborationId(), participants));

        } catch (WebClientResponseException e) {
            return Optional.empty();

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Boolean acceptCollaboration(@NotNull Collaboration collaboration, @NotNull Participant participant) {
        UUID collabId = collaboration.getCollaborationId();
        UUID participantId = participant.getUser().getUserId();

        logger.debug("Requesting Collaboration-Service to accept the Participation. Address is POST {}",
                collabServiceAddress+"/collaborations/"+collabId+"/participants/"+participantId+"/state");

        try {
            collabClient.post()
                    .uri(collabServiceAddress+"/collaborations/"+collabId+"/participants/"+participantId+"/state")
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.CREATED, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error))
                    .bodyToMono(ResponseEntity.class)
                    .block();

            logger.debug("Request successful.");
            return true;
        } catch (WebClientResponseException e) {
            logger.error("Request failed. {}", e.getMessage());
            return false;

        } catch (Exception e) {
            logger.error("Request failed. {}", e.getMessage());
            return false;
        }
    }

    public Boolean endCollaboration(@NotNull Collaboration collaboration, @NotNull Participant participant) {
        UUID collabId = collaboration.getCollaborationId();
        UUID participantId = participant.getUser().getUserId();
        try {
            collabClient.delete().uri(collabServiceAddress + "/collaborations/"+collabId+"/participants/"+participantId)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse ->
                            clientResponse.createException().flatMap(Mono::error));
            return true;

        } catch (WebClientResponseException e) {
            return false;

        } catch (Exception e) {
            return false;
        }
    }
}
