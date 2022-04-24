package de.danielkoellgen.srscsprodtestservice.domain.loop;

import de.danielkoellgen.srscsprodtestservice.domain.loop.application.LoopStartUpService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoopStartUpServiceIntegrationTest {

    private final LoopStartUpService loopStartUpService;

    @Autowired
    public LoopStartUpServiceIntegrationTest(LoopStartUpService loopStartUpService) {
        this.loopStartUpService = loopStartUpService;
    }

    @AfterEach
    public void cleanUp() {
        loopStartUpService.run();
    }

    @Test
    public void shouldExternallyCreateEntities() {
        // when
        loopStartUpService.run();
    }
}
