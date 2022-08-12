package de.danielkoellgen.srscsprodtestservice.domain.user.repository;

import de.danielkoellgen.srscsprodtestservice.domain.user.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

    List<User> findAllByIsActive(Boolean isActive);
}
