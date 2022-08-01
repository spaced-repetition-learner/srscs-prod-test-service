package de.danielkoellgen.srscsprodtestservice.domain.user.domain;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.domain.user.application.UserSynchronizationService;
import de.danielkoellgen.srscsprodtestservice.web.userservice.dto.UserResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "users")
@Getter
public class User {

    @Id
    @Column(name = "user_id")
    @Type(type = "uuid-char")
    private @NotNull UUID userId;

    @Embedded
    private @NotNull Username username;

    @Column(name = "is_active")
    private @NotNull Boolean isActive;

    @Transient
    private static final Logger log = LoggerFactory.getLogger(User.class);


    public User(@NotNull UUID userId, @NotNull Username username, @NotNull Boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.isActive = isActive;
    }

    public static @NotNull User makeFromDto(@NotNull UserResponseDto dto) {
        return new User(dto.userId(), dto.getMappedUsername(), dto.isActive());
    }

    public void update(@NotNull UserResponseDto dto) {
        if (!username.equals(dto.getMappedUsername())) {
            log.warn("Local- and remote username out of sync. [local={}, remote={}]", this.username,
                    dto.getMappedUsername());
            username = dto.getMappedUsername();
        }
        if (!isActive.equals(dto.isActive())) {
            log.warn("Local- and remote isActive out of sync. [local={}, remote={}]", this.isActive,
                    dto.isActive());
            isActive = dto.isActive();
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username=" + username.getUsername() +
                ", isActive=" + isActive +
                '}';
    }
}
