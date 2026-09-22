package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.dto.ComponentTestResponseDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Component;
import uoc.edu.model.ComponentTest;
import uoc.edu.model.RepairCase;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.RepairCaseRepository;

import java.util.List;

@Service
public class ComponentTestService {

    private final ComponentTestRepository componentTestRepository;
    private final RepairCaseRepository repairCaseRepository;
    private final ComponentRepository componentRepository;

    public ComponentTestService(ComponentTestRepository componentTestRepository, RepairCaseRepository repairCaseRepository, ComponentRepository componentRepository) {
        this.componentTestRepository = componentTestRepository;
        this.repairCaseRepository = repairCaseRepository;
        this.componentRepository = componentRepository;
    }

    private ComponentTestResponseDTO mapToResponseDTO(ComponentTest componentTest) {
        return new ComponentTestResponseDTO(
                componentTest.getComponentTestId(),
                componentTest.getRepairCase().getRepairCaseId(),
                componentTest.getComponent().getComponentId(),
                componentTest.getComponent().getName(),
                componentTest.getMeasuredVoltage(),
                componentTest.getMeasuredCurrent(),
                componentTest.getMeasuredResistance(),
                componentTest.getTemperature(),
                componentTest.getContinuity(),
                componentTest.getResult(),
                componentTest.getTestDate(),
                componentTest.getNotes()
        );
    }

    public List<ComponentTestResponseDTO> getAllComponentTests() {
        return componentTestRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ComponentTestResponseDTO getComponentTestById(Long id) {
        ComponentTest componentTest = findComponentTestEntityById(id);
        return mapToResponseDTO(componentTest);
    }

    public List<ComponentTestResponseDTO> getComponentTestsByRepairCaseId(Long repairCaseId) {
        return componentTestRepository.findByRepairCaseRepairCaseId(repairCaseId).stream().map(this::mapToResponseDTO).toList();
    }

    public ComponentTestResponseDTO addComponentTest(ComponentTestRequestDTO componentTestRequestDTO) {

        RepairCase repairCase = findRepairCaseEntityById(componentTestRequestDTO.repairCaseId());
        Component component = findComponentEntityById(componentTestRequestDTO.componentId());

        validateComponentBelongsToRepairCaseModel(repairCase, component);

        ComponentTest componentTest = new ComponentTest();

        componentTest.setRepairCase(repairCase);
        componentTest.setComponent(component);
        componentTest.setMeasuredVoltage(componentTestRequestDTO.measuredVoltage());
        componentTest.setMeasuredCurrent(componentTestRequestDTO.measuredCurrent());
        componentTest.setMeasuredResistance(componentTestRequestDTO.measuredResistance());
        componentTest.setTemperature(componentTestRequestDTO.temperature());
        componentTest.setContinuity(componentTestRequestDTO.continuity());
        componentTest.setNotes(componentTestRequestDTO.notes());
        componentTest.setResult(componentTestRequestDTO.result());

        ComponentTest savedComponentTest = componentTestRepository.save(componentTest);
        return mapToResponseDTO(savedComponentTest);
    }

    public ComponentTestResponseDTO updateComponentTest(Long id, ComponentTestRequestDTO componentTestRequestDTO) {

        ComponentTest componentTest = findComponentTestEntityById(id);

        RepairCase repairCase = findRepairCaseEntityById(componentTestRequestDTO.repairCaseId());
        Component component = findComponentEntityById(componentTestRequestDTO.componentId());

        validateComponentBelongsToRepairCaseModel(repairCase, component);

        componentTest.setRepairCase(repairCase);
        componentTest.setComponent(component);
        componentTest.setMeasuredVoltage(componentTestRequestDTO.measuredVoltage());
        componentTest.setMeasuredCurrent(componentTestRequestDTO.measuredCurrent());
        componentTest.setMeasuredResistance(componentTestRequestDTO.measuredResistance());
        componentTest.setTemperature(componentTestRequestDTO.temperature());
        componentTest.setContinuity(componentTestRequestDTO.continuity());
        componentTest.setResult(componentTestRequestDTO.result());
        componentTest.setNotes(componentTestRequestDTO.notes());

        ComponentTest savedComponentTest = componentTestRepository.save(componentTest);

        return mapToResponseDTO(savedComponentTest);
    }

    public void deleteComponentTest(Long id) {

        ComponentTest componentTest = findComponentTestEntityById(id);
        componentTestRepository.delete(componentTest);
    }

    public List<ComponentTestResponseDTO> getComponentTestsByComponentId(Long componentId) {
        return componentTestRepository.findByComponentComponentId(componentId).stream().map(this::mapToResponseDTO).toList();
    }

    private ComponentTest findComponentTestEntityById(Long componentId) {
        return componentTestRepository.findById(componentId).orElseThrow(() -> new ResourceNotFoundException("Component test not found"));
    }

    private Component findComponentEntityById(Long componentId) {
        return componentRepository.findById(componentId).orElseThrow(() -> new ResourceNotFoundException("Component not found"));
    }

    private RepairCase findRepairCaseEntityById(Long repairCaseId) {
        return repairCaseRepository.findById(repairCaseId).orElseThrow(() -> new ResourceNotFoundException("RepairCase not found"));
    }

    private void validateComponentBelongsToRepairCaseModel(
            RepairCase repairCase,
            Component component
    ) {
        Long repairCaseModelId = repairCase
                .getConsole()
                .getConsoleModel()
                .getConsoleModelId();

        Long componentModelId = component
                .getConsoleModel()
                .getConsoleModelId();

        if (!repairCaseModelId.equals(componentModelId)) {
            throw new InvalidRequestException(
                    "The component does not belong to the console model associated with the repair case"
            );
        }
    }
}
