package uoc.edu.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.dto.ConsoleModelRequestDTO;
import uoc.edu.dto.ConsoleModelResponseDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ManufacturerRepository;
import uoc.edu.repository.ConsoleRepository;

import java.time.Year;
import java.util.List;

@Service
public class ConsoleModelService {

    private final ConsoleModelRepository consoleModelRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ConsoleRepository consoleRepository;
    private final ComponentRepository componentRepository;

    public ConsoleModelService(ConsoleModelRepository consoleModelRepository, ManufacturerRepository manufacturerRepository, ConsoleRepository consoleRepository, ComponentRepository componentRepository) {
        this.consoleModelRepository = consoleModelRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.consoleRepository = consoleRepository;
        this.componentRepository = componentRepository;
    }

    public List<ConsoleModelResponseDTO> getAllConsoleModels() {
        return consoleModelRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ConsoleModelResponseDTO getConsoleModelById(Long id) {
        ConsoleModel consoleModel = findConsoleModelEntityById(id);

        return mapToResponseDTO(consoleModel);
    }

    public ConsoleModelResponseDTO addConsoleModel(ConsoleModelRequestDTO consoleModelRequestDTO) {
        validateReleaseYear(consoleModelRequestDTO.releaseYear());

        Manufacturer m = findManufacturerEntityById(consoleModelRequestDTO.manufacturerId());
        ConsoleModel cm = new ConsoleModel();

        consoleModelRepository.findByConsoleModelName(consoleModelRequestDTO.consoleModelName()).ifPresent(existing -> {
            throw new ResourceAlreadyExistsException("Console with name " + consoleModelRequestDTO.consoleModelName() + " already exists");
        });
        cm.setReleaseYear(consoleModelRequestDTO.releaseYear());
        cm.setConsoleModelName(consoleModelRequestDTO.consoleModelName());
        cm.setManufacturer(m);

        ConsoleModel savedConsoleModel = consoleModelRepository.save(cm);
        return mapToResponseDTO(savedConsoleModel);
    }

    public ConsoleModelResponseDTO updateConsoleModel(Long id, ConsoleModelRequestDTO consoleModelRequestDTO) {
        validateReleaseYear(consoleModelRequestDTO.releaseYear());

        consoleModelRepository.findByConsoleModelName(consoleModelRequestDTO.consoleModelName())
                .filter(consoleModel -> !consoleModel.getConsoleModelId().equals(id))
                .ifPresent(consoleModel -> {
                    throw new ResourceAlreadyExistsException(
                            "A console model with name " + consoleModel.getConsoleModelName() + " already exists");
                });
        ConsoleModel existingConsoleModel = findConsoleModelEntityById(id);
        Manufacturer manufacturer = findManufacturerEntityById(consoleModelRequestDTO.manufacturerId());

        existingConsoleModel.setConsoleModelName(consoleModelRequestDTO.consoleModelName());
        existingConsoleModel.setReleaseYear(consoleModelRequestDTO.releaseYear());

        existingConsoleModel.setManufacturer(manufacturer);
        ConsoleModel savedConsoleModel = consoleModelRepository.save(existingConsoleModel);
        return mapToResponseDTO(savedConsoleModel);
    }

    @Transactional
    public void deleteConsoleModel(Long id) {
        ConsoleModel consoleModel = findConsoleModelEntityById(id);
        consoleModelRepository.delete(consoleModel);
    }

    private void validateReleaseYear(Integer releaseYear) {
        if (releaseYear > Year.now().getValue()) {
            throw new InvalidRequestException("Release year cannot be in the future");
        }
    }

    private ConsoleModel findConsoleModelEntityById(Long id) {
        return consoleModelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Console model not found"));
    }

    private Manufacturer findManufacturerEntityById(Long id) {
        return manufacturerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Associated manufacturer not found"));
    }

    private ConsoleModelResponseDTO mapToResponseDTO(ConsoleModel consoleModel) {
        return new ConsoleModelResponseDTO(
                consoleModel.getConsoleModelId(),
                consoleModel.getConsoleModelName(),
                consoleModel.getReleaseYear(),
                consoleModel.getManufacturer().getManufacturerId(),
                consoleModel.getManufacturer().getManufacturerName()
        );
    }
}
