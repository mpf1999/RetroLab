package uoc.edu.dto;

public record ComponentResponseDTO(
        Long componentId,
        Long consoleModelId,
        String name,
        String description
) {
}
