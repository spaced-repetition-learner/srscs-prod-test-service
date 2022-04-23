package de.danielkoellgen.srscsprodtestservice.domain.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "is_active")
    private @NotNull Boolean isActive;


    public User(@NotNull UUID userId, @NotNull Boolean isActive) {
        this.userId = userId;
        this.isActive = isActive;
    }
}
