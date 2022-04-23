package de.danielkoellgen.srscsprodtestservice.domain.user.domain;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

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


    public User(@NotNull UUID userId, @NotNull Boolean isActive) {
        this.userId = userId;
        this.isActive = isActive;
    }
}
