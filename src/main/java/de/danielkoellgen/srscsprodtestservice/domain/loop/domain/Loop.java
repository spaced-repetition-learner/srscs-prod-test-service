package de.danielkoellgen.srscsprodtestservice.domain.loop.domain;

import de.danielkoellgen.srscsprodtestservice.domain.loop.application.LoopService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "loops")
@NoArgsConstructor
@AllArgsConstructor
public class Loop {

    @Id
    @Column(name = "loop_id")
    private @NotNull Integer loopId;

    @Column(name = "loop_status")
    private @NotNull LoopStatus loopStatus;

    @Column(name = "user_size")
    private @NotNull Integer userSize;

    @Column(name = "sleep_between_requests")
    private @NotNull Long sleepBetweenRequests;

    @Transient
    private static final Logger log = LoggerFactory.getLogger(Loop.class);

    public static Loop makeNew() {
        return new Loop(LoopService.loopId, LoopStatus.STOPPED, 0, 0L);
    }

    public static Loop makeNew(@NotNull Integer userSize, @NotNull Long sleepBetweenRequests) {
        return new Loop(LoopService.loopId, LoopStatus.STARTED, userSize, sleepBetweenRequests);
    }

    public void startLoop(Integer userSize, Long sleepBetweenRequests) {
        this.userSize = userSize;
        this.sleepBetweenRequests = sleepBetweenRequests;
        this.loopStatus = LoopStatus.STARTED;
    }

    public void stoppedLoop() {
        loopStatus = LoopStatus.STOPPED;
    }

    @Override
    public String toString() {
        return "Loop{" +
                "loopId=" + loopId +
                ", loopStatus=" + loopStatus +
                ", userSize=" + userSize +
                ", sleepBetweenRequests=" + sleepBetweenRequests +
                '}';
    }
}
