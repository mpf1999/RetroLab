package uoc.edu.dto;

import uoc.edu.model.Condition;
import uoc.edu.model.Status;


public record ConsoleResponseDTO(Long consoleId,
                                 Long ownerId,
                                 String ownerName,
                                 Long consoleModelId,
                                 String consoleModelName,
                                 String manufacturerName,
                                 String serialNumber,
                                 String region,
                                 String color,
                                 Condition condition,
                                 MoneyDTO estimatedValue,
                                 Status status,
                                 String notes,
                                 String imageUrl
) {
}
