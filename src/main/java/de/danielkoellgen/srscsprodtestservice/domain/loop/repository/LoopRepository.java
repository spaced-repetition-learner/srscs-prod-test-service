package de.danielkoellgen.srscsprodtestservice.domain.loop.repository;

import de.danielkoellgen.srscsprodtestservice.domain.loop.domain.Loop;
import org.springframework.data.repository.CrudRepository;

public interface LoopRepository extends CrudRepository<Loop, Integer> {
}
