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
public class LoopExecutionThread implements Runnable {

    private final LoopStartUpService loopStartUpService;
    private final LoopIterationService loopIterationService;

    private final LoopRepository loopRepository;

    private final Logger logger = LoggerFactory.getLogger(LoopExecutionThread.class);

    @Autowired
    public LoopExecutionThread(LoopStartUpService loopStartUpService,
            LoopIterationService loopIterationService, LoopRepository loopRepository) {
        this.loopStartUpService = loopStartUpService;
        this.loopIterationService = loopIterationService;
        this.loopRepository = loopRepository;
    }

    @Override
    public void run() {
        executeStartUp();

        while(validateLoopIsStillActive()) {
            executeIteration();
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
        loopStartUpService.run();
    }

    private void executeIteration() {
        loopIterationService.runIteration();
    }
}
