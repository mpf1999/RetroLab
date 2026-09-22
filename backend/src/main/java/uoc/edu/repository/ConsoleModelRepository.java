package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.ConsoleModel;

import java.util.Optional;

public interface ConsoleModelRepository extends JpaRepository<ConsoleModel, Long> {
    boolean existsByManufacturerManufacturerId(Long manufacturerId);

    Optional<ConsoleModel> findByConsoleModelName(String consoleModelName);
}
