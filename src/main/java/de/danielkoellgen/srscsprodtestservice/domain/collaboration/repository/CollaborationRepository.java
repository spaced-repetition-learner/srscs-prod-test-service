package de.danielkoellgen.srscsprodtestservice.domain.collaboration.repository;

import de.danielkoellgen.srscsprodtestservice.domain.collaboration.domain.Collaboration;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CollaborationRepository extends CrudRepository<Collaboration, UUID> {
}
