package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.Component;

import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Long> {
    Optional<Component> findByConsoleModelConsoleModelIdAndName(Long consoleModelId, String name);

    boolean existsByConsoleModelConsoleModelId(Long consoleModelId);
}
