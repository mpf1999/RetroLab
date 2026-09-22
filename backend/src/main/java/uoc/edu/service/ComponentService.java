package uoc.edu.service;

import org.springframework.stereotype.Service;
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.dto.ComponentResponseDTO;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.model.Component;
import uoc.edu.model.ConsoleModel;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleModelRepository;

import java.util.List;

@Service
public class ComponentService {

    private final ComponentRepository componentRepository;
    private final ComponentTestRepository componentTestRepository;
    private final ConsoleModelRepository consoleModelRepository;

    public ComponentService(ComponentRepository componentRepository, ComponentTestRepository componentTestRepository, ConsoleModelRepository consoleModelRepository) {
        this.componentRepository = componentRepository;
        this.componentTestRepository = componentTestRepository;
        this.consoleModelRepository = consoleModelRepository;
    }

    public List<ComponentResponseDTO> getAllComponents() {
        return componentRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    public ComponentResponseDTO getComponentById(Long id) {
        Component component = findComponentEntityById(id);
        return mapToResponseDTO(component);
    }

    public ComponentResponseDTO addComponent(ComponentRequestDTO componentRequestDTO) {

        componentRepository.findByConsoleModelConsoleModelIdAndName(componentRequestDTO.consoleModelId(), componentRequestDTO.name())
                .ifPresent(component -> {
                    throw new ResourceAlreadyExistsException(
                            "A component with this name already exists");
                });


        ConsoleModel consoleModel = findConsoleModelById(componentRequestDTO.consoleModelId());

        Component component = new Component();

        component.setName(componentRequestDTO.name());
        component.setDescription(componentRequestDTO.description());
        component.setConsoleModel(consoleModel);

        Component savedComponent = componentRepository.save(component);
        return mapToResponseDTO(savedComponent);
    }

    public ComponentResponseDTO updateComponent(Long id, ComponentRequestDTO componentRequestDTO) {

        componentRepository.findByConsoleModelConsoleModelIdAndName(componentRequestDTO.consoleModelId(), componentRequestDTO.name()).filter(component -> !component.getComponentId().equals(id)).ifPresent(component -> {
            throw new ResourceAlreadyExistsException(
                    "A component with this name already exists");
        });

        Component existingComponent = findComponentEntityById(id);

        existingComponent.setName(componentRequestDTO.name());
        existingComponent.setDescription(componentRequestDTO.description());
        existingComponent.setConsoleModel(findConsoleModelById(componentRequestDTO.consoleModelId()));
        Component updatedComponent = componentRepository.save(existingComponent);
        return mapToResponseDTO(updatedComponent);
    }

    public void deleteComponent(Long id) {
        Component component = findComponentEntityById(id);
        componentRepository.delete(component);
    }

    private Component findComponentEntityById(Long id) {
        return componentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Component not found"));
    }

    private ConsoleModel findConsoleModelById(Long id) {
        return consoleModelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Console model not found"));
    }

    private ComponentResponseDTO mapToResponseDTO(Component component) {
        return new ComponentResponseDTO(
                component.getComponentId(),
                component.getConsoleModel().getConsoleModelId(),
                component.getName(),
                component.getDescription()
        );
    }
}
