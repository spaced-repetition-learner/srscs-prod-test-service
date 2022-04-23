package de.danielkoellgen.srscsprodtestservice.controller;

import de.danielkoellgen.srscsprodtestservice.domain.loop.application.LoopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoopController {

    private final LoopService loopService;

    @Autowired
    public LoopController(LoopService loopService) {
        this.loopService = loopService;
    }

    @PostMapping(name = "/loop")
    public ResponseEntity<?> startLoop() {
        loopService.startLoop();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(name = "/loop")
    public ResponseEntity<?> stopLoop() {
        loopService.endLoop();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
