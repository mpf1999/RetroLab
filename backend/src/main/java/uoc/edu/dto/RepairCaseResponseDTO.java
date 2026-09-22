package uoc.edu.dto;

import uoc.edu.model.RepairStatus;

import java.time.LocalDateTime;

public record RepairCaseResponseDTO(
        Long repairCaseId,
        Long consoleId,
        String consoleName,
        String title,
        String description,
        RepairStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
