package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.dto.ManufacturerResponseDTO;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ManufacturerRepository;
import uoc.edu.repository.ConsoleModelRepository;

import java.util.List;

@Service
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ConsoleModelRepository consoleModelRepository;

    public ManufacturerService(ManufacturerRepository manufacturerRepository, ConsoleModelRepository consoleModelRepository) {
        this.manufacturerRepository = manufacturerRepository;
        this.consoleModelRepository = consoleModelRepository;
    }

    public List<ManufacturerResponseDTO> getAllManufacturers() {
        return manufacturerRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ManufacturerResponseDTO getManufacturerById(Long id) {
        Manufacturer m = findManufacturerEntityById(id);
        return mapToResponseDTO(m);
    }

    public ManufacturerResponseDTO addManufacturer(ManufacturerRequestDTO manufacturerRequestDTO) {
        Manufacturer manufacturer = new Manufacturer();

        manufacturerRepository.findByManufacturerName(manufacturerRequestDTO.manufacturerName())
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException(
                            "A manufacturer with this name already exists");
                });

        manufacturer.setManufacturerName(manufacturerRequestDTO.manufacturerName());
        manufacturer.setCountryCode(manufacturerRequestDTO.countryCode());

        Manufacturer savedManufacturer = manufacturerRepository.save(manufacturer);
        return mapToResponseDTO(savedManufacturer);
    }

    public ManufacturerResponseDTO updateManufacturer(Long id, ManufacturerRequestDTO manufacturerRequestDTO) {
        Manufacturer existingManufacturer = findManufacturerEntityById(id);

        manufacturerRepository.findByManufacturerName(manufacturerRequestDTO.manufacturerName())
                .filter(existing -> !existing.getManufacturerId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException(
                            "A manufacturer with this name already exists");
                });
        existingManufacturer.setManufacturerName(manufacturerRequestDTO.manufacturerName());
        existingManufacturer.setCountryCode(manufacturerRequestDTO.countryCode());
        Manufacturer updatedManufacturer = manufacturerRepository.save(existingManufacturer);
        return mapToResponseDTO(updatedManufacturer);
    }

    public void deleteManufacturer(Long id) {
        Manufacturer manufacturer = findManufacturerEntityById(id);
        manufacturerRepository.delete(manufacturer);
    }

    private ManufacturerResponseDTO mapToResponseDTO(Manufacturer manufacturer) {
        return new ManufacturerResponseDTO(
                manufacturer.getManufacturerId(), manufacturer.getManufacturerName(), manufacturer.getCountryCode()
        );
    }

    public Manufacturer findManufacturerEntityById(Long id) {
        return manufacturerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
    }
}
