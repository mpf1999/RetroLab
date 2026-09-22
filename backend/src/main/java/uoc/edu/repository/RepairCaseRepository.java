package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.RepairCase;
import uoc.edu.model.RepairStatus;

import java.util.List;

public interface RepairCaseRepository extends JpaRepository<RepairCase, Long> {
    List<RepairCase> findByConsoleConsoleId(Long consoleId);

    boolean existsByConsoleConsoleIdAndStatusNot(Long consoleId, RepairStatus status);
}
