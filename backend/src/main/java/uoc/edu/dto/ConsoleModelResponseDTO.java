package uoc.edu.dto;

public record ConsoleModelResponseDTO(Long consoleModelId,
                                      String consoleModelName,
                                      Integer releaseYear,
                                      Long manufacturerId,
                                      String manufacturerName) {
}
