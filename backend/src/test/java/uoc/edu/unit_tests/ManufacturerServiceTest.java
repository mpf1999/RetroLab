package uoc.edu.unit_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.dto.ManufacturerResponseDTO;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ManufacturerRepository;
import uoc.edu.service.ManufacturerService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManufacturerServiceTest {

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ConsoleModelRepository consoleModelRepository;

    @InjectMocks
    private ManufacturerService manufacturerService;

    private Manufacturer nintendo;
    private Manufacturer sega;
    private Manufacturer sony;

    @BeforeEach
    void setUp() {
        nintendo = createManufacturer(1L, "Nintendo", "JP");
        sega = createManufacturer(2L, "Sega", "JP");
        sony = createManufacturer(3L, "Sony", "JP");
    }

    /*
     Get all manufacturers test
     */

    @Test
    void getAllManufacturersReturnsManufacturers() {
        when(manufacturerRepository.findAll()).thenReturn(List.of(nintendo, sega));

        List<ManufacturerResponseDTO> result = manufacturerService.getAllManufacturers();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.getFirst().manufacturerId());
        assertEquals("Nintendo", result.get(0).manufacturerName());
        assertEquals("JP", result.get(0).countryCode());

        assertEquals(2L, result.get(1).manufacturerId());
        assertEquals("Sega", result.get(1).manufacturerName());
        assertEquals("JP", result.get(1).countryCode());

        verify(manufacturerRepository).findAll();
        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void getAllManufacturersReturnsEmptyList() {
        when(manufacturerRepository.findAll()).thenReturn(List.of());

        List<ManufacturerResponseDTO> result = manufacturerService.getAllManufacturers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(manufacturerRepository).findAll();
        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    /*
     Get manufacturers by ID
     */

    @Test
    void getManufacturerByIdReturnsManufacturer() {
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));

        ManufacturerResponseDTO result = manufacturerService.getManufacturerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.manufacturerId());
        assertEquals("Nintendo", result.manufacturerName());
        assertEquals("JP", result.countryCode());

        verify(manufacturerRepository).findById(1L);
        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void getManufacturerByIdThrowsExceptionWhenManufacturerDoesNotExist() {
        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> manufacturerService.getManufacturerById(99L)
        );

        assertEquals("Manufacturer not found", exception.getMessage());

        verify(manufacturerRepository).findById(99L);
        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    /*
     * Add manufacturers
     */

    @Test
    void addManufacturerReturnsCreatedManufacturer() {
        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "JP");

        when(manufacturerRepository.findByManufacturerName("Nintendo")).thenReturn(Optional.empty());

        when(manufacturerRepository.save(any(Manufacturer.class))).thenAnswer(invocation -> {
            Manufacturer manufacturer = invocation.getArgument(0);
            manufacturer.setManufacturerId(1L);
            return manufacturer;
        });

        ManufacturerResponseDTO result = manufacturerService.addManufacturer(request);

        assertNotNull(result);
        assertEquals(1L, result.manufacturerId());
        assertEquals("Nintendo", result.manufacturerName());
        assertEquals("JP", result.countryCode());

        verify(manufacturerRepository).findByManufacturerName("Nintendo");
        verify(manufacturerRepository).save(any(Manufacturer.class));

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void addManufacturerSavesCorrectManufacturerData() {
        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Sony", "JP");

        when(manufacturerRepository.findByManufacturerName("Sony")).thenReturn(Optional.empty());

        when(manufacturerRepository.save(any(Manufacturer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        manufacturerService.addManufacturer(request);

        ArgumentCaptor<Manufacturer> c = ArgumentCaptor.forClass(Manufacturer.class);

        verify(manufacturerRepository).save(c.capture());

        Manufacturer savedManufacturer = c.getValue();

        assertNull(savedManufacturer.getManufacturerId());
        assertEquals("Sony", savedManufacturer.getManufacturerName());
        assertEquals("JP", savedManufacturer.getCountryCode());
        assertNotNull(savedManufacturer.getConsoleModels());
        assertTrue(savedManufacturer.getConsoleModels().isEmpty());

        verify(manufacturerRepository).findByManufacturerName("Sony");

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void addManufacturerThrowsExceptionWhenNameAlreadyExists() {
        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "JP");

        when(manufacturerRepository.findByManufacturerName("Nintendo"))
                .thenReturn(Optional.of(nintendo));

        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> manufacturerService.addManufacturer(request)
        );

        assertEquals(
                "A manufacturer with this name already exists",
                exception.getMessage()
        );

        verify(manufacturerRepository).findByManufacturerName("Nintendo");
        verify(manufacturerRepository, never()).save(any(Manufacturer.class));

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    /*
     * Update manufacturer
     */

    @Test
    void updateManufacturerReturnsUpdatedManufacturer() {
        ManufacturerRequestDTO request =
                new ManufacturerRequestDTO("Nintendo Company", "JP");

        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));

        when(manufacturerRepository.findByManufacturerName("Nintendo Company"))
                .thenReturn(Optional.empty());

        when(manufacturerRepository.save(nintendo)).thenReturn(nintendo);

        ManufacturerResponseDTO result =
                manufacturerService.updateManufacturer(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.manufacturerId());
        assertEquals("Nintendo Company", result.manufacturerName());
        assertEquals("JP", result.countryCode());

        assertEquals("Nintendo Company", nintendo.getManufacturerName());
        assertEquals("JP", nintendo.getCountryCode());

        verify(manufacturerRepository).findById(1L);

        verify(manufacturerRepository)
                .findByManufacturerName("Nintendo Company");

        verify(manufacturerRepository).save(nintendo);

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void updateManufacturerAllowsKeepingCurrentName() {
        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "US");

        //findByManufacturerName returns the same manufacturer, since it is the same we update it does not throw exception
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));

        when(manufacturerRepository.findByManufacturerName("Nintendo"))
                .thenReturn(Optional.of(nintendo));

        when(manufacturerRepository.save(nintendo)).thenReturn(nintendo);

        ManufacturerResponseDTO result =
                manufacturerService.updateManufacturer(1L, request);

        assertEquals(1L, result.manufacturerId());
        assertEquals("Nintendo", result.manufacturerName());
        assertEquals("US", result.countryCode());

        verify(manufacturerRepository).findById(1L);
        verify(manufacturerRepository).findByManufacturerName("Nintendo");
        verify(manufacturerRepository).save(nintendo);

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void updateManufacturerThrowsExceptionWhenNameBelongsToAnotherManufacturer() {
        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Sony", "JP");

        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));
        when(manufacturerRepository.findByManufacturerName("Sony")).thenReturn(Optional.of(sony));
        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> manufacturerService.updateManufacturer(1L, request));

        assertEquals("A manufacturer with this name already exists", exception.getMessage());

        assertEquals("Nintendo", nintendo.getManufacturerName());

        verify(manufacturerRepository).findById(1L);
        verify(manufacturerRepository).findByManufacturerName("Sony");
        verify(manufacturerRepository, never()).save(any(Manufacturer.class));

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void updateManufacturerThrowsExceptionWhenManufacturerDoesNotExist() {
        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "JP");

        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> manufacturerService.updateManufacturer(99L, request));

        assertEquals("Manufacturer not found", exception.getMessage());

        verify(manufacturerRepository).findById(99L);
        verify(manufacturerRepository, never()).findByManufacturerName(anyString());
        verify(manufacturerRepository, never()).save(any(Manufacturer.class));

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    //Delete a manufacturer

    @Test
    void deleteManufacturerDeletesManufacturer() {
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));

        manufacturerService.deleteManufacturer(1L);

        verify(manufacturerRepository).findById(1L);
        verify(manufacturerRepository).delete(nintendo);

        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoMoreInteractions(consoleModelRepository);
    }

    @Test
    void deleteManufacturerThrowsExceptionWhenManufacturerDoesNotExist() {
        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> manufacturerService.deleteManufacturer(99L));

        assertEquals("Manufacturer not found", exception.getMessage());

        verify(manufacturerRepository).findById(99L);
        verifyNoInteractions(consoleModelRepository);
        verify(manufacturerRepository, never()).delete(any(Manufacturer.class));
        verifyNoMoreInteractions(manufacturerRepository);
    }

    /*
     Find entity
     */

    @Test
    void findManufacturerEntityByIdReturnsManufacturer() {
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(nintendo));

        Manufacturer result =
                manufacturerService.findManufacturerEntityById(1L);

        assertSame(nintendo, result);

        verify(manufacturerRepository).findById(1L);
        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    @Test
    void findManufacturerEntityByIdThrowsExceptionWhenManufacturerDoesNotExist() {
        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> manufacturerService.findManufacturerEntityById(99L));

        assertEquals("Manufacturer not found", exception.getMessage());

        verify(manufacturerRepository).findById(99L);
        verifyNoMoreInteractions(manufacturerRepository);
        verifyNoInteractions(consoleModelRepository);
    }

    private Manufacturer createManufacturer(Long id, String name, String countryCode) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setManufacturerId(id);
        manufacturer.setManufacturerName(name);
        manufacturer.setCountryCode(countryCode);
        return manufacturer;
    }
}