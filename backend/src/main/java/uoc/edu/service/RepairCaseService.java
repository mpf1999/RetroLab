package uoc.edu.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.dto.RepairCaseResponseDTO;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.model.Console;
import uoc.edu.model.RepairCase;
import uoc.edu.model.RepairStatus;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.ComponentTestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepairCaseService {

    private final RepairCaseRepository repairCaseRepository;
    private final ConsoleRepository consoleRepository;

    private final ComponentTestRepository componentTestRepository;

    public RepairCaseService(RepairCaseRepository repairCaseRepository, ConsoleRepository consoleRepository, ComponentTestRepository componentTestRepository) {
        this.repairCaseRepository = repairCaseRepository;
        this.consoleRepository = consoleRepository;
        this.componentTestRepository = componentTestRepository;
    }

    public List<RepairCaseResponseDTO> getAllRepairCases() {

        return repairCaseRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public RepairCaseResponseDTO getRepairCaseById(Long id) {
        RepairCase existing = findRepairEntityCaseById(id);
        return mapToResponseDTO(existing);
    }

    public List<RepairCaseResponseDTO> getRepairCasesByConsoleId(Long consoleId) {
        return repairCaseRepository.findByConsoleConsoleId(consoleId).stream().map(this::mapToResponseDTO).toList();
    }

    public RepairCaseResponseDTO addRepairCase(RepairCaseRequestDTO repairCaseRequestDTO) {

        if (repairCaseRequestDTO.status() == RepairStatus.CLOSED) {
            throw new InvalidRequestException("A new repair case cannot be created as CLOSED");
        }

        Console console = findConsoleEntityCaseById(repairCaseRequestDTO.consoleId());
        RepairCase repairCase = new RepairCase();

        repairCase.setTitle(repairCaseRequestDTO.title());
        repairCase.setDescription(repairCaseRequestDTO.description());
        repairCase.setStatus(repairCaseRequestDTO.status());
        repairCase.setStartDate(LocalDateTime.now());
        repairCase.setEndDate(null);
        validateRepairDates(
                repairCase.getStartDate(),
                repairCase.getEndDate()
        );

        repairCase.setConsole(console);

        RepairCase savedRepairCase = repairCaseRepository.save(repairCase);
        return mapToResponseDTO(savedRepairCase);
    }


    public RepairCaseResponseDTO updateRepairCase(
            Long id,
            RepairCaseRequestDTO repairCaseRequestDTO
    ) {
        RepairCase existingRepairCase =
                findRepairEntityCaseById(id);

        if (existingRepairCase.getStatus() == RepairStatus.CLOSED) {
            throw new InvalidRequestException(
                    "Closed repair cases cannot be modified"
            );
        }
        Console console = existingRepairCase.getConsole();

        if (repairCaseRequestDTO.consoleId() != null) {
            console = findConsoleEntityCaseById(
                    repairCaseRequestDTO.consoleId()
            );
        }

        existingRepairCase.setConsole(console);
        existingRepairCase.setTitle(
                repairCaseRequestDTO.title()
        );
        existingRepairCase.setDescription(
                repairCaseRequestDTO.description()
        );
        existingRepairCase.setStatus(
                repairCaseRequestDTO.status()
        );

        if (repairCaseRequestDTO.status() == RepairStatus.CLOSED) {
            existingRepairCase.setEndDate(
                    LocalDateTime.now()
            );
        }

        validateRepairDates(
                existingRepairCase.getStartDate(),
                existingRepairCase.getEndDate()
        );

        RepairCase savedRepairCase =
                repairCaseRepository.save(existingRepairCase);

        return mapToResponseDTO(savedRepairCase);
    }

    @Transactional
    public void deleteRepairCase(Long id) {
        RepairCase repairCase = findRepairEntityCaseById(id);
        repairCaseRepository.delete(repairCase);
    }

    public RepairCase findRepairEntityCaseById(Long id) {
        return repairCaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Repair case not found"));
    }

    private Console findConsoleEntityCaseById(Long id) {
        return consoleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Console not found"));
    }

    private void validateRepairDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidRequestException(
                    "Start date cannot be after end date");
        }
    }

    private RepairCaseResponseDTO mapToResponseDTO(RepairCase repairCase) {
        return new RepairCaseResponseDTO(
                repairCase.getRepairCaseId(),
                repairCase.getConsole().getConsoleId(),
                repairCase.getConsole().getConsoleModel().getConsoleModelName(),
                repairCase.getTitle(),
                repairCase.getDescription(),
                repairCase.getStatus(),
                repairCase.getStartDate(),
                repairCase.getEndDate()
        );
    }
}
