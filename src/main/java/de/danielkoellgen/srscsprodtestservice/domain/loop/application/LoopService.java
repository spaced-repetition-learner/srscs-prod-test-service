package de.danielkoellgen.srscsprodtestservice.domain.loop.application;

import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.Loop;
import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.LoopStatus;
import de.danielkoellgen.srscsprodtestservice.domain.loop.repository.LoopRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoopService {

    private final LoopExecutionService loopExecutionService;

    private final LoopRepository loopRepository;

    public static Integer loopId = 1;

    @Autowired
    public LoopService(LoopExecutionService loopExecutionService, LoopRepository loopRepository) {
        this.loopExecutionService = loopExecutionService;
        this.loopRepository = loopRepository;
    }

    public void startLoop() {
        Loop loop = fetchOrMakeNewLoop();
        if (loop.getLoopStatus().equals(LoopStatus.STARTED)) {
            throw new RuntimeException("Loop already started.");
        }
        loop.startLoop();
        loopRepository.save(loop);

        Thread thread = new Thread(loopExecutionService);
        thread.setDaemon(true);
        thread.start();
    }

    public void endLoop() {
        Loop loop = fetchOrMakeNewLoop();
        loop.stopLoop();
        loopRepository.save(loop);
    }

    private @NotNull Loop fetchOrMakeNewLoop() {
        Optional<Loop> optLoop = loopRepository.findById(loopId);
        if (optLoop.isPresent()) {
            return optLoop.get();
        }
        Loop newLoop =  new Loop(LoopStatus.WAITING);
        loopRepository.save(newLoop);
        return newLoop;
    }
}
