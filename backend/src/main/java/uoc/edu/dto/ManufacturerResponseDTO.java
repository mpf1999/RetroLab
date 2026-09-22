package uoc.edu.dto;

public record ManufacturerResponseDTO(
        Long manufacturerId,
        String manufacturerName,
        String countryCode
) {
}
