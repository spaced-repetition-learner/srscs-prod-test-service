package de.danielkoellgen.srscsprodtestservice.domain.user.domain;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import de.danielkoellgen.srscsprodtestservice.web.userservice.dto.UserResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

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


    public User(@NotNull UUID userId, @NotNull Username username, @NotNull Boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.isActive = isActive;
    }

    public static @NotNull User makeFromDto(@NotNull UserResponseDto dto) {
        return new User(dto.userId(), dto.getMappedUsername(), dto.isActive());
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
