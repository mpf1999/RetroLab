package uoc.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uoc.edu.model.ComponentTest;

import java.util.List;

public interface ComponentTestRepository extends JpaRepository<ComponentTest, Long> {

    List<ComponentTest> findByRepairCaseRepairCaseId(Long repairCaseId);

    List<ComponentTest> findByComponentComponentId(Long componentId);

    boolean existsByComponentComponentId(Long componentId);

    boolean existsByRepairCaseRepairCaseId(Long repairCaseId);
}
