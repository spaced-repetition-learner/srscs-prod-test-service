package de.danielkoellgen.srscsprodtestservice.domain.user.domain;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @Getter
    @Column(name = "user_id")
    @Type(type = "uuid-char")
    private @NotNull UUID userId;

    @Getter
    @Embedded
    private @NotNull Username username;

    @Getter
    @Column(name = "is_active")
    private @NotNull Boolean isActive;


    public User(@NotNull UUID userId, @NotNull Username username, @NotNull Boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.isActive = isActive;
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
