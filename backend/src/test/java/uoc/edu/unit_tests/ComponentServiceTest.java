package uoc.edu.unit_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.dto.ComponentResponseDTO;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Component;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.service.ComponentService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComponentServiceTest {

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private ComponentTestRepository componentTestRepository;

    @Mock
    private ConsoleModelRepository consoleModelRepository;

    @InjectMocks
    private ComponentService componentService;

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoyModel;
    private ConsoleModel playStationModel;

    private Component gameBoyScreen;
    private Component gameBoySpeaker;

    @BeforeEach
    void setUp() {

        nintendo = createManufacturer(1L, "Nintendo", "JP");
        sony = createManufacturer(2L, "Sony", "JP");

        gameBoyModel = createConsoleModel(1L, "Game Boy", 1989, nintendo);
        playStationModel = createConsoleModel(2L, "PlayStation", 1994, sony);

        gameBoyScreen = createComponent(1L, "Screen", "Original LCD screen", gameBoyModel);
        gameBoySpeaker = createComponent(2L, "Speaker", "Internal mono speaker", gameBoyModel);
    }

    // get all components test

    @Test
    void getAllComponentsReturnsComponents() {

        when(componentRepository.findAll()).thenReturn(List.of(gameBoyScreen, gameBoySpeaker));

        List<ComponentResponseDTO> result = componentService.getAllComponents();

        assertNotNull(result);
        assertEquals(2, result.size());

        ComponentResponseDTO firstComponent = result.getFirst();
        ComponentResponseDTO secondComponent = result.get(1);

        assertAll(
                () -> assertEquals(1L, firstComponent.componentId()),
                () -> assertEquals(1L, firstComponent.consoleModelId()),
                () -> assertEquals("Screen", firstComponent.name()),
                () -> assertEquals("Original LCD screen", firstComponent.description())
        );

        assertAll(
                () -> assertEquals(2L, secondComponent.componentId()),
                () -> assertEquals(1L, secondComponent.consoleModelId()),
                () -> assertEquals("Speaker", secondComponent.name()),
                () -> assertEquals("Internal mono speaker", secondComponent.description())
        );

        verify(componentRepository).findAll();
        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository);
    }

    @Test
    void getAllComponentsReturnsEmptyList() {

        when(componentRepository.findAll()).thenReturn(List.of());

        List<ComponentResponseDTO> result = componentService.getAllComponents();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(componentRepository).findAll();
        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository);
    }

    // get element by id test

    @Test
    void getComponentByIdReturnsComponent() {

        when(componentRepository.findById(1L)).thenReturn(Optional.of(gameBoyScreen));

        ComponentResponseDTO result = componentService.getComponentById(1L);

        assertNotNull(result);
        assertAll(
                () -> assertEquals(1L, result.componentId()),
                () -> assertEquals(1L, result.consoleModelId()),
                () -> assertEquals("Screen", result.name()),
                () -> assertEquals("Original LCD screen", result.description())
        );

        verify(componentRepository).findById(1L);
        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository);
    }

    @Test
    void getComponentByIdThrowsExceptionWhenComponentDoesNotExist() {

        when(componentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> componentService.getComponentById(99L));

        assertEquals("Component not found", exception.getMessage());

        verify(componentRepository).findById(99L);
        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository);
    }

    // add component test

    @Test
    void addComponentReturnsCreatedComponent() {

        ComponentRequestDTO request = new ComponentRequestDTO(1L, "Power switch", "Console power switch");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(1L, "Power switch")).thenReturn(Optional.empty());
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(componentRepository.save(any(Component.class))).thenAnswer(invocation -> {
            Component component = invocation.getArgument(0);
            component.setComponentId(4L);
            return component;
        });

        ComponentResponseDTO result = componentService.addComponent(request);
        assertNotNull(result);
        assertAll(
                () -> assertEquals(4L, result.componentId()),
                () -> assertEquals(1L, result.consoleModelId()),
                () -> assertEquals("Power switch", result.name()),
                () -> assertEquals("Console power switch", result.description())
        );

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(1L, "Power switch");
        verify(consoleModelRepository).findById(1L);
        verify(componentRepository).save(any(Component.class));

        verifyNoMoreInteractions(componentRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void addComponentSavesCorrectData() {

        ComponentRequestDTO request = new ComponentRequestDTO(1L, "Power regulator", "Regulates the console voltage");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(1L, "Power regulator")).thenReturn(Optional.empty());
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(componentRepository.save(any(Component.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);

        componentService.addComponent(request);

        verify(componentRepository).save(captor.capture());
        Component savedComponent = captor.getValue();

        assertNotNull(savedComponent);
        assertAll(
                () -> assertNull(savedComponent.getComponentId()),
                () -> assertEquals("Power regulator", savedComponent.getName()),
                () -> assertEquals("Regulates the console voltage", savedComponent.getDescription()),
                () -> assertSame(gameBoyModel, savedComponent.getConsoleModel())
        );

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(
                1L,
                "Power regulator"
        );
        verify(consoleModelRepository).findById(1L);

        verifyNoMoreInteractions(componentRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void addComponentThrowsExceptionWhenComponentAlreadyExists() {

        ComponentRequestDTO request = new ComponentRequestDTO(1L, "Screen", "Replacement LCD screen");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(1L, "Screen")).thenReturn(Optional.of(gameBoyScreen));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> componentService.addComponent(request));

        assertEquals("A component with this name already exists", exception.getMessage());

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(1L, "Screen");
        verify(componentRepository, never()).save(any(Component.class));

        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository);
    }

    @Test
    void addComponentThrowsExceptionWhenConsoleModelDoesNotExist() {

        ComponentRequestDTO request = new ComponentRequestDTO(99L, "Screen", "LCD screen");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(99L, "Screen")).thenReturn(Optional.empty());
        when(consoleModelRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> componentService.addComponent(request));

        assertEquals("Console model not found", exception.getMessage());

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(99L, "Screen");
        verify(consoleModelRepository).findById(99L);
        verify(componentRepository, never()).save(any(Component.class));

        verifyNoMoreInteractions(componentRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(componentTestRepository);
    }

    // update component test

    @Test
    void updateComponentReturnsUpdatedComponent() {

        ComponentRequestDTO request = new ComponentRequestDTO(2L, "Optical drive", "Updated optical disc drive");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(2L, "Optical drive")).thenReturn(Optional.empty());
        when(componentRepository.findById(1L)).thenReturn(Optional.of(gameBoyScreen));
        when(consoleModelRepository.findById(2L)).thenReturn(Optional.of(playStationModel));
        when(componentRepository.save(gameBoyScreen)).thenReturn(gameBoyScreen);

        ComponentResponseDTO result = componentService.updateComponent(1L, request);

        assertNotNull(result);
        assertAll(
                () -> assertEquals(1L, result.componentId()),
                () -> assertEquals(2L, result.consoleModelId()),
                () -> assertEquals("Optical drive", result.name()),
                () -> assertEquals("Updated optical disc drive", result.description())
        );

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(2L, "Optical drive");
        verify(componentRepository).findById(1L);
        verify(consoleModelRepository).findById(2L);
        verify(componentRepository).save(gameBoyScreen);

        verifyNoMoreInteractions(componentRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateComponentSavesCorrectData() {

        ComponentRequestDTO request = new ComponentRequestDTO(2L, "Disc reader", "Reads PlayStation game discs");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(2L, "Disc reader")).thenReturn(Optional.empty());
        when(componentRepository.findById(1L)).thenReturn(Optional.of(gameBoyScreen));
        when(consoleModelRepository.findById(2L)).thenReturn(Optional.of(playStationModel));
        when(componentRepository.save(any(Component.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);

        componentService.updateComponent(1L, request);

        verify(componentRepository).save(captor.capture());

        Component updatedComponent = captor.getValue();

        assertNotNull(updatedComponent);
        assertAll(
                () -> assertSame(gameBoyScreen, updatedComponent),
                () -> assertEquals(1L, updatedComponent.getComponentId()),
                () -> assertEquals("Disc reader", updatedComponent.getName()),
                () -> assertEquals("Reads PlayStation game discs", updatedComponent.getDescription()),
                () -> assertSame(playStationModel, updatedComponent.getConsoleModel())
        );

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(2L, "Disc reader");
        verify(componentRepository).findById(1L);
        verify(consoleModelRepository).findById(2L);

        verifyNoMoreInteractions(componentRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateComponentAllowsKeepingCurrentName() {

        ComponentRequestDTO request = new ComponentRequestDTO(1L, "Screen", "Updated LCD screen description");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(1L, "Screen")).thenReturn(Optional.of(gameBoyScreen));
        when(componentRepository.findById(1L)).thenReturn(Optional.of(gameBoyScreen));
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoyModel));
        when(componentRepository.save(gameBoyScreen)).thenReturn(gameBoyScreen);

        ComponentResponseDTO result = componentService.updateComponent(1L, request);

        assertNotNull(result);
        assertAll(
                () -> assertEquals(1L, result.componentId()),
                () -> assertEquals(1L, result.consoleModelId()),
                () -> assertEquals("Screen", result.name()),
                () -> assertEquals("Updated LCD screen description", result.description())
        );

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(1L, "Screen");
        verify(componentRepository).findById(1L);
        verify(consoleModelRepository).findById(1L);
        verify(componentRepository).save(gameBoyScreen);

        verifyNoMoreInteractions(componentRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateComponentThrowsExceptionWhenNameBelongsToAnotherComponent() {

        ComponentRequestDTO request = new ComponentRequestDTO(1L, "Speaker", "Updated speaker");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(1L, "Speaker")).thenReturn(Optional.of(gameBoySpeaker));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> componentService.updateComponent(1L, request));

        assertEquals("A component with this name already exists", exception.getMessage());

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(1L, "Speaker");
        verify(componentRepository, never()).findById(anyLong());
        verify(componentRepository, never()).save(any(Component.class));

        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository);
    }

    @Test
    void updateComponentThrowsExceptionWhenComponentDoesNotExist() {

        ComponentRequestDTO request = new ComponentRequestDTO(1L, "Power switch", "Updated power switch");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(1L, "Power switch")).thenReturn(Optional.empty());
        when(componentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> componentService.updateComponent(99L, request));

        assertEquals("Component not found", exception.getMessage());

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(1L, "Power switch");
        verify(componentRepository).findById(99L);
        verify(componentRepository, never()).save(any(Component.class));

        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository
        );
    }

    @Test
    void updateComponentThrowsExceptionWhenConsoleModelDoesNotExist() {

        ComponentRequestDTO request = new ComponentRequestDTO(99L, "Screen", "Updated screen");

        when(componentRepository.findByConsoleModelConsoleModelIdAndName(99L, "Screen")).thenReturn(Optional.empty());
        when(componentRepository.findById(1L)).thenReturn(Optional.of(gameBoyScreen));
        when(consoleModelRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> componentService.updateComponent(1L, request));

        assertEquals("Console model not found", exception.getMessage());

        verify(componentRepository).findByConsoleModelConsoleModelIdAndName(99L, "Screen");
        verify(componentRepository).findById(1L);
        verify(consoleModelRepository).findById(99L);

        verify(componentRepository, never()).save(any(Component.class));

        verifyNoMoreInteractions(componentRepository);
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(componentTestRepository);
    }

    // delete component test

    @Test
    void deleteComponentDeletesComponent() {

        Component componentToDelete = new Component();
        componentToDelete.setComponentId(1L);
        componentToDelete.setName("Screen");
        componentToDelete.setDescription("Game Boy LCD screen");
        componentToDelete.setConsoleModel(gameBoyModel);

        when(componentRepository.findById(1L))
                .thenReturn(Optional.of(componentToDelete));

        componentService.deleteComponent(1L);

        verify(componentRepository).findById(1L);
        verify(componentRepository).delete(componentToDelete);

        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void deleteComponentThrowsExceptionWhenComponentDoesNotExist() {

        when(componentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> componentService.deleteComponent(99L));

        assertEquals("Component not found", exception.getMessage());

        verify(componentRepository).findById(99L);
        verify(componentRepository, never()).delete(any(Component.class));

        verifyNoMoreInteractions(componentRepository);
        verifyNoInteractions(componentTestRepository, consoleModelRepository);
    }

    // helper methods

    private Manufacturer createManufacturer(Long id, String name, String countryCode
    ) {
        Manufacturer manufacturer = new Manufacturer();

        manufacturer.setManufacturerId(id);
        manufacturer.setManufacturerName(name);
        manufacturer.setCountryCode(countryCode);

        return manufacturer;
    }

    private ConsoleModel createConsoleModel(Long id, String name, Integer releaseYear, Manufacturer manufacturer
    ) {
        ConsoleModel consoleModel = new ConsoleModel();
        consoleModel.setConsoleModelId(id);
        consoleModel.setConsoleModelName(name);
        consoleModel.setReleaseYear(releaseYear);
        consoleModel.setManufacturer(manufacturer);

        return consoleModel;
    }

    private Component createComponent(Long id, String name, String description, ConsoleModel consoleModel
    ) {
        Component component = new Component();

        component.setComponentId(id);
        component.setName(name);
        component.setDescription(description);
        component.setConsoleModel(consoleModel);
        return component;
    }
}
