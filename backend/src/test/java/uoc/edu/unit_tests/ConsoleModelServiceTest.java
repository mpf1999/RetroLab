package uoc.edu.unit_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.ManufacturerRepository;
import uoc.edu.service.ConsoleModelService;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleModelServiceTest {

    @Mock
    private ConsoleModelRepository consoleModelRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ConsoleRepository consoleRepository;

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private ConsoleModelService consoleModelService;

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoy;
    private ConsoleModel playStation;

    @BeforeEach
    void setUp() {
        nintendo = createManufacturer(1L, "Nintendo", "JP");
        sony = createManufacturer(2L, "Sony", "JP");
        gameBoy = createConsoleModel(1L, "Game Boy", 1989, nintendo);
        playStation = createConsoleModel(2L, "PlayStation", 1994, sony);
    }

    //Get all console models test

    @Test
    void getAllConsoleModelsReturnsConsoleModels() {
        when(consoleModelRepository.findAll()).thenReturn(List.of(gameBoy, playStation));

        List<ConsoleModelResponseDTO> result = consoleModelService.getAllConsoleModels();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals(1L, result.getFirst().consoleModelId()),
                () -> assertEquals("Game Boy", result.getFirst().consoleModelName()),
                () -> assertEquals(1989, result.getFirst().releaseYear()),
                () -> assertEquals(1L, result.getFirst().manufacturerId()),
                () -> assertEquals("Nintendo", result.getFirst().manufacturerName())
        );

        assertAll(
                () -> assertEquals(2L, result.get(1).consoleModelId()),
                () -> assertEquals("PlayStation", result.get(1).consoleModelName()),
                () -> assertEquals(1994, result.get(1).releaseYear()),
                () -> assertEquals(2L, result.get(1).manufacturerId()),
                () -> assertEquals("Sony", result.get(1).manufacturerName())
        );

        verify(consoleModelRepository).findAll();
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(manufacturerRepository, consoleRepository, componentRepository);
    }

    @Test
    void getAllConsoleModelsReturnsEmptyList() {
        when(consoleModelRepository.findAll()).thenReturn(List.of());
        List<ConsoleModelResponseDTO> result = consoleModelService.getAllConsoleModels();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(consoleModelRepository).findAll();
        verifyNoMoreInteractions(consoleModelRepository);
        verifyNoInteractions(manufacturerRepository, consoleRepository, componentRepository);
    }

    // get console model by id

    @Test
    void getConsoleModelByIdReturnsConsoleModel() {
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoy));

        ConsoleModelResponseDTO result = consoleModelService.getConsoleModelById(1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(1L, result.consoleModelId()),
                () -> assertEquals("Game Boy", result.consoleModelName()),
                () -> assertEquals(1989, result.releaseYear()),
                () -> assertEquals(1L, result.manufacturerId()),
                () -> assertEquals("Nintendo", result.manufacturerName())
        );

        verify(consoleModelRepository).findById(1L);

        verifyNoMoreInteractions(consoleModelRepository);

        verifyNoInteractions(manufacturerRepository, consoleRepository, componentRepository);
    }

    @Test
    void getConsoleModelByIdThrowsExceptionWhenConsoleModelDoesNotExist() {
        when(consoleModelRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleModelService.getConsoleModelById(99L));

        assertEquals("Console model not found", exception.getMessage());

        verify(consoleModelRepository).findById(99L);

        verifyNoMoreInteractions(consoleModelRepository);

        verifyNoInteractions(manufacturerRepository, consoleRepository, componentRepository);
    }

    // add console model test

    @Test
    void addConsoleModelReturnsCreatedConsoleModel() {
        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Nintendo 64", 1996, 1L);

        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));
        when(consoleModelRepository.findByConsoleModelName("Nintendo 64")).thenReturn(Optional.empty());
        when(consoleModelRepository.save(any(ConsoleModel.class))).thenAnswer(invocation -> {
            ConsoleModel consoleModel = invocation.getArgument(0);
            consoleModel.setConsoleModelId(3L);
            return consoleModel;
        });

        ConsoleModelResponseDTO result = consoleModelService.addConsoleModel(request);

        assertNotNull(result);
        assertAll(
                () -> assertEquals(3L, result.consoleModelId()),
                () -> assertEquals("Nintendo 64", result.consoleModelName()
                ),
                () -> assertEquals(1996, result.releaseYear()),
                () -> assertEquals(1L, result.manufacturerId()),
                () -> assertEquals("Nintendo", result.manufacturerName())
        );

        verify(manufacturerRepository).findById(1L);
        verify(consoleModelRepository).findByConsoleModelName("Nintendo 64");
        verify(consoleModelRepository).save(any(ConsoleModel.class));
        verifyNoInteractions(consoleRepository, componentRepository);
    }

    @Test
    void addConsoleModelSavesCorrectData() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Game Boy Color", 1998, 1L);

        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));
        when(consoleModelRepository.findByConsoleModelName("Game Boy Color")).thenReturn(Optional.empty());
        when(consoleModelRepository.save(any(ConsoleModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<ConsoleModel> captor = ArgumentCaptor.forClass(ConsoleModel.class);

        consoleModelService.addConsoleModel(request);

        verify(consoleModelRepository).save(captor.capture());
        ConsoleModel savedConsoleModel = captor.getValue();

        assertNotNull(savedConsoleModel);
        assertAll(
                () -> assertEquals("Game Boy Color", savedConsoleModel.getConsoleModelName()),
                () -> assertEquals(1998, savedConsoleModel.getReleaseYear()),
                () -> assertSame(nintendo, savedConsoleModel.getManufacturer())
        );

        verify(manufacturerRepository).findById(1L);
        verify(consoleModelRepository).findByConsoleModelName("Game Boy Color");

        verifyNoInteractions(consoleRepository, componentRepository);
    }

    @Test
    void addConsoleModelThrowsExceptionWhenReleaseYearIsInFuture() {

        int futureYear = Year.now().getValue() + 1;

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Future Console", futureYear, 1L);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> consoleModelService.addConsoleModel(request));

        assertEquals("Release year cannot be in the future", exception.getMessage());

        verifyNoInteractions(consoleModelRepository, manufacturerRepository, consoleRepository, componentRepository);
    }

    @Test
    void addConsoleModelThrowsExceptionWhenManufacturerDoesNotExist() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Nintendo 64", 1996, 99L);

        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleModelService.addConsoleModel(request));

        assertEquals("Associated manufacturer not found", exception.getMessage());

        verify(manufacturerRepository).findById(99L);
        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository, consoleRepository, componentRepository);
    }

    @Test
    void addConsoleModelThrowsExceptionWhenNameAlreadyExists() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Game Boy", 1989, 1L);
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));
        when(consoleModelRepository.findByConsoleModelName("Game Boy")).thenReturn(Optional.of(gameBoy));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> consoleModelService.addConsoleModel(request));

        assertEquals("Console with name Game Boy already exists", exception.getMessage());

        verify(manufacturerRepository).findById(1L);
        verify(consoleModelRepository).findByConsoleModelName("Game Boy");
        verify(consoleModelRepository, never()).save(any(ConsoleModel.class));
        verifyNoInteractions(consoleRepository, componentRepository);
    }

    // update console model tests

    @Test
    void updateConsoleModelReturnsUpdatedConsoleModel() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Game Boy Advance", 2001, 2L);

        when(consoleModelRepository.findByConsoleModelName("Game Boy Advance")).thenReturn(Optional.empty());
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(manufacturerRepository.findById(2L)).thenReturn(Optional.of(sony));
        when(consoleModelRepository.save(gameBoy)).thenReturn(gameBoy);

        ConsoleModelResponseDTO result = consoleModelService.updateConsoleModel(1L, request);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(1L, result.consoleModelId()),
                () -> assertEquals("Game Boy Advance", result.consoleModelName()),
                () -> assertEquals(2001, result.releaseYear()),
                () -> assertEquals(2L, result.manufacturerId()),
                () -> assertEquals("Sony", result.manufacturerName())
        );

        verify(consoleModelRepository).findByConsoleModelName("Game Boy Advance");

        verify(consoleModelRepository).findById(1L);
        verify(manufacturerRepository).findById(2L);
        verify(consoleModelRepository).save(gameBoy);

        verifyNoInteractions(consoleRepository, componentRepository);
    }

    @Test
    void updateConsoleModelModifiesExistingEntity() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Game Boy Color", 1998, 2L);

        when(consoleModelRepository.findByConsoleModelName("Game Boy Color")).thenReturn(Optional.empty());
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(manufacturerRepository.findById(2L)).thenReturn(Optional.of(sony));
        when(consoleModelRepository.save(any(ConsoleModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<ConsoleModel> captor = ArgumentCaptor.forClass(ConsoleModel.class);

        consoleModelService.updateConsoleModel(1L, request);
        verify(consoleModelRepository).save(captor.capture());
        ConsoleModel updatedConsoleModel = captor.getValue();
        assertSame(gameBoy, updatedConsoleModel);

        assertAll(
                () -> assertEquals(1L, updatedConsoleModel.getConsoleModelId()),
                () -> assertEquals("Game Boy Color", updatedConsoleModel.getConsoleModelName()),
                () -> assertEquals(1998, updatedConsoleModel.getReleaseYear()),
                () -> assertSame(sony, updatedConsoleModel.getManufacturer())
        );
    }

    @Test
    void updateConsoleModelAllowsKeepingItsCurrentName() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Game Boy", 1990, 1L);

        when(consoleModelRepository.findByConsoleModelName("Game Boy")).thenReturn(Optional.of(gameBoy));
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));
        when(consoleModelRepository.save(gameBoy)).thenReturn(gameBoy);

        ConsoleModelResponseDTO result = consoleModelService.updateConsoleModel(1L, request);

        assertNotNull(result);
        assertEquals("Game Boy", result.consoleModelName());
        assertEquals(1990, result.releaseYear());

        verify(consoleModelRepository).findByConsoleModelName("Game Boy");

        verify(consoleModelRepository).findById(1L);
        verify(manufacturerRepository).findById(1L);
        verify(consoleModelRepository).save(gameBoy);
    }

    @Test
    void updateConsoleModelThrowsExceptionWhenReleaseYearIsInFuture() {
        int futureYear = Year.now().getValue() + 1;
        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Future Console", futureYear, 1L);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> consoleModelService.updateConsoleModel(1L, request));

        assertEquals("Release year cannot be in the future", exception.getMessage());

        verifyNoInteractions(consoleModelRepository, manufacturerRepository, consoleRepository, componentRepository);
    }

    @Test
    void updateConsoleModelThrowsExceptionWhenNameBelongsToAnotherModel() {
        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("PlayStation", 1994, 1L);

        when(consoleModelRepository.findByConsoleModelName("PlayStation")).thenReturn(Optional.of(playStation));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> consoleModelService.updateConsoleModel(1L, request));

        assertEquals("A console model with name PlayStation already exists", exception.getMessage());

        verify(consoleModelRepository).findByConsoleModelName("PlayStation");
        verify(consoleModelRepository, never()).findById(anyLong());
        verify(consoleModelRepository, never()).save(any(ConsoleModel.class));
        verifyNoInteractions(manufacturerRepository, consoleRepository, componentRepository);
    }

    @Test
    void updateConsoleModelThrowsExceptionWhenConsoleModelDoesNotExist() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Nintendo 64", 1996, 1L);

        when(consoleModelRepository.findByConsoleModelName("Nintendo 64")).thenReturn(Optional.empty());
        when(consoleModelRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleModelService.updateConsoleModel(99L, request));

        assertEquals("Console model not found", exception.getMessage());

        verify(consoleModelRepository).findByConsoleModelName("Nintendo 64");
        verify(consoleModelRepository).findById(99L);
        verify(manufacturerRepository, never()).findById(anyLong());

        verify(consoleModelRepository, never()).save(any(ConsoleModel.class));
        verifyNoInteractions(consoleRepository, componentRepository);
    }

    @Test
    void updateConsoleModelThrowsExceptionWhenManufacturerDoesNotExist() {

        ConsoleModelRequestDTO request = new ConsoleModelRequestDTO("Game Boy Color", 1998, 99L);

        when(consoleModelRepository.findByConsoleModelName("Game Boy Color")).thenReturn(Optional.empty());
        when(consoleModelRepository.findById(1L)).thenReturn(Optional.of(gameBoy));
        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleModelService.updateConsoleModel(1L, request));

        assertEquals("Associated manufacturer not found", exception.getMessage());

        verify(consoleModelRepository).findByConsoleModelName("Game Boy Color");
        verify(consoleModelRepository).findById(1L);
        verify(manufacturerRepository).findById(99L);
        verify(consoleModelRepository, never()).save(any(ConsoleModel.class));

        verifyNoInteractions(consoleRepository, componentRepository);
    }

    // delete console model test

    @Test
    void deleteConsoleModelDeletesConsoleModel() {
        Long consoleModelId = 1L;

        ConsoleModel consoleModel = new ConsoleModel();
        consoleModel.setConsoleModelId(consoleModelId);

        when(consoleModelRepository.findById(consoleModelId))
                .thenReturn(Optional.of(consoleModel));

        consoleModelService.deleteConsoleModel(consoleModelId);

        verify(consoleModelRepository).findById(consoleModelId);
        verify(consoleModelRepository).delete(consoleModel);

        verifyNoInteractions(consoleRepository);
    }

    @Test
    void deleteConsoleModelThrowsExceptionWhenConsoleModelDoesNotExist() {

        when(consoleModelRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> consoleModelService.deleteConsoleModel(99L));

        assertEquals("Console model not found", exception.getMessage());

        verify(consoleModelRepository).findById(99L);

        verify(consoleModelRepository, never()).delete(any(ConsoleModel.class));

        verifyNoInteractions(manufacturerRepository, consoleRepository, componentRepository);
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
}