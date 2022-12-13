package de.danielkoellgen.srscsprodtestservice.controller.loop;

import de.danielkoellgen.srscsprodtestservice.controller.loop.dto.LoopRequestDto;
import de.danielkoellgen.srscsprodtestservice.domain.loop.application.LoopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LoopController {

    private LoopService loopService;

    private static final Logger log = LoggerFactory.getLogger(LoopController.class);

    @Autowired
    public LoopController(LoopService loopService) {
        this.loopService = loopService;
    }

    @PostMapping(value = "/loop")
    public ResponseEntity<HttpStatus> startLoop(@RequestBody LoopRequestDto requestDto) {
        log.info("POST /loops: Start new Loop");

        Integer userSize;
        Long sleepBetweenRequests;
        try {
            userSize = requestDto.userSize();
            sleepBetweenRequests = requestDto.sleepBetweenRequests();
        } catch (Exception e) {
            log.info("Request failed. Input invalid: {}. Responding w/ 400.", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        loopService.startLoop(userSize, sleepBetweenRequests);
//        try {
//            loopService.startLoop(userSize, sleepBetweenRequests);
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.);
//        }

        log.info("Request successful. Loop started. Responding w/ 201.");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/loop")
    public ResponseEntity<HttpStatus> endLoop() {
        log.info("DELETE /loops: End loop");
        try {
            loopService.endLoop();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        log.info("Request successful. Responding w/ 200.");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
