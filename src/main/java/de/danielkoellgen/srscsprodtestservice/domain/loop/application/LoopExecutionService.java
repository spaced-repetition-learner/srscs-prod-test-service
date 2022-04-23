package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.Loop;
import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.LoopStatus;
import de.danielkoellgen.srscsprodtestservice.domain.loop.repository.LoopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LoopExecutionService implements Runnable {

    private final LoopRepository loopRepository;

    private final Logger logger = LoggerFactory.getLogger(LoopExecutionService.class);

    @Autowired
    public LoopExecutionService(LoopRepository loopRepository) {
        this.loopRepository = loopRepository;
    }

    @Override
    public void run() {
        runLoop();
    }

    private void runLoop() {
        executeStartUp();

        if (validateLoopIsStillActive()) {
            executeRandomBehavior();

        } else {
            // TODO: LOG THIS
        }
    }

    private Boolean validateLoopIsStillActive() {
        Optional<Loop> loopOptional = loopRepository.findById(LoopService.loopId);
        if (loopOptional.isEmpty()) {
            return false;
        }
        if (!loopOptional.get().getLoopStatus().equals(LoopStatus.STARTED)) {
            return false;
        }
        return true;
    }

    private void executeStartUp() {
        // TODO
    }

    private void executeRandomBehavior() {
        // TODO
    }

}
