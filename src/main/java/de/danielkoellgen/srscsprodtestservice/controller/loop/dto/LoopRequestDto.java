package de.danielkoellgen.srscsprodtestservice.controller.loop.dto;

public record LoopRequestDto(

    Integer userSize,

    Long sleepBetweenRequests

) {

    public Integer getUserSize() throws Exception {
        if (userSize < 1 || userSize > 10) {
            throw new Exception("UserSize must be between 1 and 10 but is "+userSize+".");
        }
        return userSize;
    }

    public Long getSleepBetweenRequests() throws Exception {
        if (sleepBetweenRequests < 250 || sleepBetweenRequests > 10000) {
            throw new Exception("SleepBetweenRequests must be between 250 and 10000 but is "+sleepBetweenRequests+".");
        }
        return sleepBetweenRequests;
    }
}
