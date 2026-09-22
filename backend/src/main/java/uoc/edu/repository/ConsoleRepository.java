package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.Console;
import uoc.edu.model.RepairCase;

import java.util.List;
import java.util.Optional;

public interface ConsoleRepository extends JpaRepository<Console, Long> {
    boolean existsByConsoleModelConsoleModelId(Long consoleModelId);

    Optional<Console> findBySerialNumber(String serialNumber);
    List<Console> findByOwnerId(Long userId);
}
