package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.Loop;
import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.LoopStatus;
import de.danielkoellgen.srscsprodtestservice.domain.loop.repository.LoopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoopService {

    private final LoopStartUpService loopStartUpService;

    private final LoopRepository loopRepository;

    private static final Logger log = LoggerFactory.getLogger(LoopService.class);

    public static final Integer loopId = 1;

    @Autowired
    public LoopService(LoopStartUpService loopStartUpService, LoopRepository loopRepository) {
        this.loopStartUpService = loopStartUpService;
        this.loopRepository = loopRepository;
    }

    public void startLoop(Integer userSize, Long sleepBetweenRequests) {
        Loop loop = loopRepository.findById(loopId).orElse(Loop.makeNew());
        log.debug("Fetched Loop: {}", loop);

        if (loop.getLoopStatus() == LoopStatus.STARTED) {
            log.info("Failed to start Loop, because an active Loop is already running with {} Users.",
                    loop.getUserSize());
            throw new RuntimeException("Loop is already active.");
        }

        loop.startLoop(userSize, sleepBetweenRequests);
        loopRepository.save(loop);
        log.info("Loop successfully started with {} Users and {}ms sleep between requests.",
                userSize, sleepBetweenRequests);
        log.debug("Started Loop: {}", loop);

        loopStartUpService.startLoop(userSize, sleepBetweenRequests);
    }

    public void endLoop() {
        Optional<Loop> optLoop = loopRepository.findById(loopId);
        if (optLoop.isEmpty()) {
            log.info("Failed to end Loop, because no Loop is currently active.");
            throw new RuntimeException("No Loop is currently active.");
        }
        Loop loop = optLoop.get();
        loop.stoppedLoop();
        loopRepository.save(loop);
        log.info("Loop successfully stopped.");
        log.debug("Stopped Loop: {}", loop);
    }
}
