package de.danielkoellgen.srscsprodtestservice.domain.participant.domain;

import org.jetbrains.annotations.NotNull;

public enum ParticipantStatus {
    INVITED,
    INVITATION_ACCEPTED,
    INVITATION_DECLINED,
    TERMINATED;

    public static @NotNull String toStringFromEnum(@NotNull ParticipantStatus status) {
        return switch(status) {
            case INVITED                -> "INVITED";
            case INVITATION_ACCEPTED    -> "INVITATION_ACCEPTED";
            case INVITATION_DECLINED    -> "INVITATION_DECLINED";
            case TERMINATED             -> "TERMINATED";
        };
    }

    public static @NotNull ParticipantStatus toEnumFromString(@NotNull String status) {
        return switch(status) {
            case "INVITED"              -> ParticipantStatus.INVITED;
            case "INVITATION_ACCEPTED"  -> ParticipantStatus.INVITATION_ACCEPTED;
            case "INVITATION_DECLINED"  -> ParticipantStatus.INVITATION_DECLINED;
            case "TERMINATED"           -> ParticipantStatus.TERMINATED;
            default -> throw new RuntimeException("Failed to map from Type String to ParticipantStatus.");
        };
    }
}
